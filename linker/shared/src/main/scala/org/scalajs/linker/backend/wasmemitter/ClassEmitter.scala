/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package org.scalajs.linker.backend.wasmemitter

import scala.collection.mutable

import org.scalajs.ir.{ClassKind, OriginalName, Position, UTF8String}
import org.scalajs.ir.Names._
import org.scalajs.ir.OriginalName.NoOriginalName
import org.scalajs.ir.Trees._
import org.scalajs.ir.Types._
import org.scalajs.ir.WellKnownNames._

import org.scalajs.linker.interface.CheckedBehavior
import org.scalajs.linker.interface.unstable.RuntimeClassNameMapperImpl
import org.scalajs.linker.standard.{CoreSpec, LinkedClass, LinkedGlobalInfo, LinkedTopLevelExport}

import org.scalajs.linker.backend.javascript.{Trees => js}

import org.scalajs.linker.backend.webassembly.FunctionBuilder
import org.scalajs.linker.backend.webassembly.{Instructions => wa}
import org.scalajs.linker.backend.webassembly.{Modules => wamod}
import org.scalajs.linker.backend.webassembly.{Identitities => wanme}
import org.scalajs.linker.backend.webassembly.{Types => watpe}
import org.scalajs.linker.backend.webassembly.component.Flatten

import canonicalabi.ScalaJSToCABI
import EmbeddedConstants._
import SWasmGen._
import VarGen._
import TypeTransformer._
import WasmContext._
import _root_.org.scalajs.linker.backend.wasmemitter.canonicalabi.CABIToScalaJS

class ClassEmitter(coreSpec: CoreSpec) {
  import ClassEmitter._
  import coreSpec.semantics
  import coreSpec.wasmFeatures.targetPureWasm

  def genClassDef(clazz: LinkedClass)(implicit ctx: WasmContext): Unit = {
    val classInfo = ctx.getClassInfo(clazz.className)

    if (classInfo.hasRuntimeTypeInfo && !(clazz.kind.isClass && clazz.hasDirectInstances)) {
      // Gen typeData -- for concrete Scala classes, we do it as part of the vtable generation instead
      val typeDataFieldValues = genTypeDataFieldValues(clazz, Nil)
      genTypeDataGlobal(clazz.className, genTypeID.typeData, typeDataFieldValues, Nil, Nil)
    }

    // Declare static fields
    for {
      field @ FieldDef(flags, name, _, ftpe) <- clazz.fields
      if flags.namespace.isStatic
    } {
      val origName = makeDebugName(ns.StaticField, name.name)
      val global = wamod.Global(
        genGlobalID.forStaticField(name.name),
        origName,
        isMutable = true,
        transformFieldType(ftpe),
        wa.Expr(List(genZeroOf(ftpe)))
      )
      ctx.addGlobal(global)
    }

    // Generate method implementations
    for (method <- clazz.methods) {
      if (method.body.isDefined)
        genMethod(clazz, method)
    }

    if (coreSpec.wasmFeatures.targetPureWasm &&
        coreSpec.wasmFeatures.componentModel) {
      for (member <- clazz.componentNativeMembers) {
        canonicalabi.InteropEmitter.genComponentNativeInterop(clazz, member)
      }
    }

    // maybe better to Component Interface to be an another ClassKind?

    clazz.kind match {
      case ClassKind.Class | ClassKind.ModuleClass =>
        genScalaClass(clazz)
      case ClassKind.Interface =>
        genInterface(clazz)
      case ClassKind.JSClass | ClassKind.JSModuleClass =>
        genJSClass(clazz)
      case ClassKind.HijackedClass | ClassKind.AbstractJSType | ClassKind.NativeJSClass |
          ClassKind.NativeJSModuleClass | ClassKind.NativeWasmComponentResourceClass =>
        () // nothing to do
    }
  }


  /** Generates code for a top-level export.
   *
   *  It is tempting to use Wasm `export`s for top-level exports. However, that
   *  does not work in several situations:
   *
   *  - for values, an `export`ed `global` is visible in JS as an instance of
   *    `WebAssembly.Global`, of which we need to extract the `.value` field anyway
   *  - this in turn causes issues for mutable static fields, since we need to
   *    republish changes
   *  - we cannot distinguish mutable static fields from immutable ones, so we
   *    have to use the same strategy for both
   *  - exported top-level `def`s must be seen by JS as `function` functions,
   *    but `export`ed `func`s are JS arrow functions
   *
   *  Overall, the only things for which `export`s would work are for exported
   *  JS classes and objects.
   *
   *  Instead, we uniformly use the following strategy for all top-level exports:
   *
   *  - the JS code declares a non-initialized `let` for every top-level export, and exports it
   *    from the module with an ECMAScript `export`
   *  - the JS code provides a setter function that we import into a Wasm, which allows to set the
   *    value of that `let`
   *  - the Wasm code "publishes" every update to top-level exports to the JS code via this
   *    setter; this happens once in the `start` function for every kind of top-level export (see
   *    `Emitter.genStartFunction`), and in addition upon each reassignment of a top-level
   *    exported field (see `FunctionEmitter.genAssign`).
   *
   *  This method declares the import of the setter on the Wasm side, for all kinds of top-level
   *  exports. In addition, for exported *methods*, it generates the implementation of the method as
   *  a Wasm function.
   *
   *  The JS code is generated by `Emitter.buildJSFileContent`. Note that for fields, the JS `let`s
   *  are only "mirrors" of the state. The source of truth for the state remains in the Wasm Global
   *  for the static field. This is fine because, by spec of ECMAScript modules, JavaScript code
   *  that *uses* the export cannot mutate it; it can only read it.
   *
   *  The calls to the setters, which actually initialize all the exported `let`s, are performed:
   *
   *  - in the `start` function for all kinds of exports, and
   *  - in addition on every assignment to an exported mutable static field.
   */
  def genTopLevelExport(topLevelExport: LinkedTopLevelExport)(
      implicit ctx: WasmContext): Unit = {
    topLevelExport.tree match {
      case d: WasmComponentExportDef if ctx.coreSpec.wasmFeatures.targetPureWasm =>
        canonicalabi.InteropEmitter.genWasmComponentExportDef(d)
      case d: TopLevelMethodExportDef =>
        genTopLevelExportSetter(topLevelExport.exportName)
        genTopLevelMethodExportDef(d)
      case _ =>
        genTopLevelExportSetter(topLevelExport.exportName)
    }
  }

  private def genIsJSClassInstanceFunction(clazz: LinkedClass)(
      implicit ctx: WasmContext): Option[wanme.FunctionID] = {
    implicit val noPos: Position = Position.NoPosition

    val hasIsJSClassInstance = clazz.kind match {
      case ClassKind.NativeJSClass => clazz.jsNativeLoadSpec.isDefined
      case ClassKind.JSClass       => clazz.jsClassCaptures.isEmpty
      case _                       => false
    }

    if (hasIsJSClassInstance) {
      val className = clazz.className

      val fb = new FunctionBuilder(
        ctx.moduleBuilder,
        genFunctionID.isJSClassInstance(className),
        makeDebugName(ns.IsInstance, className),
        noPos
      )
      val xParam = fb.addParam("x", watpe.RefType.anyref)
      fb.setResultType(watpe.Int32)
      fb.setFunctionType(genTypeID.isJSClassInstanceFuncType)

      if (clazz.kind == ClassKind.JSClass && !clazz.hasInstances) {
        /* We need to constant-fold the instance test, to avoid trying to
         * call $loadJSClass.className, since it will not exist at all.
         */
        fb += wa.I32Const(0) // false
      } else {
        val helperBuilder = new CustomJSHelperBuilder()
        val xRef = helperBuilder.addWasmInput("x", watpe.RefType.anyref) {
          fb += wa.LocalGet(xParam)
        }
        val ctorRef = clazz.jsNativeLoadSpec match {
          case Some(loadSpec) =>
            helperBuilder.genJSNativeLoadSpec(loadSpec)
          case None =>
            // This is a non-native JS class
            helperBuilder.addWasmInput("ctor", watpe.RefType.any) {
              fb += wa.Call(genFunctionID.loadJSClass(className))
            }
        }
        val helperID = helperBuilder.build(BooleanType) {
          js.Return(js.BinaryOp(JSBinaryOp.instanceof, xRef, ctorRef))
        }
        fb += wa.Call(helperID)
      }

      val func = fb.buildAndAddToModule()
      Some(func.id)
    } else {
      None
    }
  }

  private def genTypeDataFieldValues(clazz: LinkedClass,
      reflectiveProxies: List[ConcreteMethodInfo])(
      implicit ctx: WasmContext): List[wa.Instr] = {
    val className = clazz.className
    val classInfo = ctx.getClassInfo(className)

    val nameStr = RuntimeClassNameMapperImpl.map(
      coreSpec.semantics.runtimeClassNameMapper,
      className.nameString
    )
    val nameValue =
      if (targetPureWasm)
        ctx.stringPool.getConstantStringDataInstr(nameStr) :+
            wa.RefNull(watpe.HeapType(genTypeID.wasmString))
      else ctx.stringPool.getConstantStringDataInstr(nameStr)

    val kind = className match {
      case ObjectClass         => KindObject
      case BoxedUnitClass      => KindBoxedUnit
      case BoxedBooleanClass   => KindBoxedBoolean
      case BoxedCharacterClass => KindBoxedCharacter
      case BoxedByteClass      => KindBoxedByte
      case BoxedShortClass     => KindBoxedShort
      case BoxedIntegerClass   => KindBoxedInteger
      case BoxedLongClass      => KindBoxedLong
      case BoxedFloatClass     => KindBoxedFloat
      case BoxedDoubleClass    => KindBoxedDouble
      case BoxedStringClass    => KindBoxedString

      case _ =>
        import ClassKind._

        clazz.kind match {
          case Class | ModuleClass | HijackedClass =>
            KindClass
          case Interface =>
            KindInterface
          case NativeWasmComponentResourceClass =>
            KindClass // TODO
          case JSClass | JSModuleClass | AbstractJSType | NativeJSClass | NativeJSModuleClass =>
            if (clazz.superClass.isDefined)
              KindJSTypeWithSuperClass
            else
              KindJSType
        }
    }

    val strictAncestorsTypeData: List[wa.Instr] = {
      val ancestors = clazz.ancestors

      // By spec, the first element of `ancestors` is always the class itself
      assert(
        ancestors.headOption.contains(className),
        s"The ancestors of ${className.nameString} do not start with itself: $ancestors"
      )
      val strictAncestors0 = ancestors.tail

      // If the class has a super class, move it first for the benefit of Class_superClass
      val strictAncestors = clazz.superClass match {
        case Some(ClassIdent(superClass)) =>
          superClass :: strictAncestors0.filter(_ != superClass)
        case None =>
          strictAncestors0
      }

      val elems = for {
        ancestor <- strictAncestors
        if ctx.getClassInfo(ancestor).hasRuntimeTypeInfo
      } yield {
        wa.GlobalGet(genGlobalID.forVTable(ancestor))
      }
      elems :+ wa.ArrayNewFixed(genTypeID.typeDataArray, elems.size)
    }

    val cloneFunction = {
      // If the class is concrete and implements the `java.lang.Cloneable`,
      // `genCloneFunction` should've generated the clone function
      if (!classInfo.isAbstract && clazz.ancestors.contains(CloneableClass))
        wa.RefFunc(genFunctionID.clone(className))
      else
        wa.RefNull(watpe.HeapType.NoFunc)
    }

    val isJSClassInstance = genIsJSClassInstanceFunction(clazz) match {
      case None         => wa.RefNull(watpe.HeapType.NoFunc)
      case Some(funcID) => wa.RefFunc(funcID)
    }

    val reflectiveProxiesInstrs: List[wa.Instr] = {
      val elemsInstrs: List[wa.Instr] = reflectiveProxies
        .map(proxyInfo => ctx.getReflectiveProxyId(proxyInfo.methodName) -> proxyInfo.tableEntryID)
        .sortBy(_._1) // we will perform a binary search on the ID at run-time
        .flatMap { case (proxyID, tableEntryID) =>
          List(
            wa.I32Const(proxyID),
            wa.RefFunc(tableEntryID),
            wa.StructNew(genTypeID.reflectiveProxy)
          )
        }
      elemsInstrs :+ wa.ArrayNewFixed(genTypeID.reflectiveProxies, reflectiveProxies.size)
    }

    (
      // name
      nameValue :::
      List(
        // kind
        wa.I32Const(kind),
        // specialInstanceTypes
        wa.I32Const(classInfo.specialInstanceTypes)
      ) ::: (
        // strictAncestors
        strictAncestorsTypeData
      ) :::
      List(
        // componentType - always `null` since this method is not used for array types
        wa.RefNull(watpe.HeapType(genTypeID.typeData)),
        // the classOf instance - initially `null`; filled in by the `createClassOf` helper
        wa.RefNull(watpe.HeapType(genTypeID.ClassStruct)),
        // arrayOf, the typeData of an array of this type - initially `null`; filled in by the `arrayTypeData` helper
        wa.RefNull(watpe.HeapType(genTypeID.ObjectVTable)),
        // clonefFunction - will be invoked from `clone()` method invokaion on the class
        cloneFunction,
        // isJSClassInstance - invoked from the `isInstance()` helper for JS types
        isJSClassInstance
      ) :::
      // reflective proxies - used to reflective call on the class at runtime.
      // Generated instructions create an array of reflective proxy structs, where each struct
      // contains the ID of the reflective proxy and a reference to the actual method implementation.
      reflectiveProxiesInstrs
    )
  }

  private def genTypeDataGlobal(className: ClassName, typeDataTypeID: wanme.TypeID,
      typeDataFieldValues: List[wa.Instr], itableSlots: List[wa.Instr],
      vtableElems: List[wa.RefFunc])(
      implicit ctx: WasmContext): Unit = {
    val instrs: List[wa.Instr] =
      typeDataFieldValues ::: itableSlots ::: vtableElems ::: wa.StructNew(typeDataTypeID) :: Nil
    ctx.addGlobal(
      wamod.Global(
        genGlobalID.forVTable(className),
        makeDebugName(ns.TypeData, className),
        isMutable = false,
        watpe.RefType(typeDataTypeID),
        wa.Expr(instrs)
      )
    )
  }

  /** Generates a Scala class or module class. */
  private def genScalaClass(clazz: LinkedClass)(implicit ctx: WasmContext): Unit = {
    val className = clazz.name.name
    val typeRef = ClassRef(className)
    val classInfo = ctx.getClassInfo(className)

    // generate vtable type, this should be done for both abstract and concrete classes
    val vtableTypeID = genVTableType(clazz, classInfo)

    val isAbstractClass = !clazz.hasDirectInstances

    // Generate the vtable for concrete classes
    if (!isAbstractClass) {
      // Generate an actual vtable, which we integrate into the typeData
      val reflectiveProxies =
        classInfo.resolvedMethodInfos.valuesIterator.filter(_.methodName.isReflectiveProxy).toList
      val typeDataFieldValues = genTypeDataFieldValues(clazz, reflectiveProxies)
      val itableSlots = genItableSlots(classInfo, clazz.ancestors)
      val vtableElems = classInfo.tableEntries.map { methodName =>
        wa.RefFunc(classInfo.resolvedMethodInfos(methodName).tableEntryID)
      }
      genTypeDataGlobal(className, vtableTypeID, typeDataFieldValues, itableSlots, vtableElems)
    }

    // Declare the struct type for the class
    val vtableField = watpe.StructField(
      genFieldID.objStruct.vtable,
      vtableOriginalName,
      watpe.RefType(vtableTypeID),
      isMutable = false
    )
    val idHashCodeField = if (targetPureWasm) {
      Some(watpe.StructField(
        genFieldID.objStruct.idHashCode,
        OriginalName(genFieldID.objStruct.idHashCode.toString()),
        watpe.Int32,
        isMutable = true
      ))
    } else None

    val fields = idHashCodeField.toList ::: classInfo.allFieldDefs.map { field =>
      watpe.StructField(
        genFieldID.forClassInstanceField(field.name.name),
        makeDebugName(ns.InstanceField, field.name.name),
        transformFieldType(field.ftpe),
        isMutable = true // initialized by the constructors, so always mutable at the Wasm level
      )
    }
    val jlClassDataField = if (className == ClassClass) {
      // Inject the magic `data` field
      watpe.StructField(
        genFieldID.classData,
        OriginalName("data"),
        watpe.RefType(genTypeID.typeData),
        isMutable = false
      ) :: Nil
    } else {
      Nil
    }
    val structTypeID = genTypeID.forClass(className)
    val superType = clazz.superClass.map(s => genTypeID.forClass(s.name))
    val structType = watpe.StructType(vtableField :: fields ::: jlClassDataField)
    val subType = watpe.SubType(
      structTypeID,
      makeDebugName(ns.ClassInstance, className),
      isFinal = false,
      superType,
      structType
    )
    ctx.mainRecType.addSubType(subType)

    // Define the `new` function and possibly the `clone` function, unless the class is abstract
    if (!isAbstractClass) {
      genNewDefaultFunc(clazz)
      if (clazz.ancestors.contains(CloneableClass))
        genCloneFunction(clazz)
    }

    // Generate cast functions
    if (clazz.hasInstanceTests && semantics.asInstanceOfs != CheckedBehavior.Unchecked) {
      if (className != ObjectClass)
        genClassCastFunction(clazz)
    }

    // Generate the module accessor
    if (clazz.kind == ClassKind.ModuleClass && clazz.hasInstances) {
      val heapType = watpe.HeapType(genTypeID.forClass(clazz.className))

      // global instance
      val global = wamod.Global(
        genGlobalID.forModuleInstance(className),
        makeDebugName(ns.ModuleInstance, className),
        isMutable = true,
        watpe.RefType.nullable(heapType),
        wa.Expr(List(wa.RefNull(heapType)))
      )
      ctx.addGlobal(global)

      if (semantics.moduleInit != CheckedBehavior.Unchecked) {
        val initFlagGlobal = wamod.Global(
          genGlobalID.forModuleInitFlag(className),
          makeDebugName(ns.ModuleInitFlag, className),
          isMutable = true,
          watpe.Int32,
          wa.Expr(List(wa.I32Const(0)))
        )
        ctx.addGlobal(initFlagGlobal)
      }

      genModuleAccessor(clazz)
    }
  }

  private def genVTableType(clazz: LinkedClass, classInfo: ClassInfo)(
      implicit ctx: WasmContext): wanme.TypeID = {
    val className = classInfo.name
    val typeID = genTypeID.forVTable(className)
    val itableSlotFields = (0 until ctx.itablesLength).map { i =>
      watpe.StructField(
        genFieldID.vtableStruct.itableSlot(i),
        OriginalName.NoOriginalName,
        watpe.RefType.nullable(watpe.HeapType.Struct),
        isMutable = false
      )
    }.toList
    val vtableFields =
      classInfo.tableEntries.map { methodName =>
        watpe.StructField(
          genFieldID.forMethodTableEntry(methodName),
          makeDebugName(ns.TableEntry, className, methodName),
          watpe.RefType(ctx.tableFunctionType(methodName)),
          isMutable = false
        )
      }
    val superType = clazz.superClass match {
      case None    => genTypeID.typeData
      case Some(s) => genTypeID.forVTable(s.name)
    }
    val structType = watpe.StructType(ctx.coreLib.typeDataStructFields ::: itableSlotFields ::: vtableFields)
    val subType = watpe.SubType(
      typeID,
      makeDebugName(ns.VTable, className),
      isFinal = false,
      Some(superType),
      structType
    )
    ctx.mainRecType.addSubType(subType)
    typeID
  }

  /** Generate type inclusion test for interfaces.
   *
   *  The expression `isInstanceOf[<interface>]` will be compiled to a CALL to the function
   *  generated by this method.
   */
  private def genInterfaceInstanceTest(clazz: LinkedClass)(
      implicit ctx: WasmContext): Unit = {
    assert(clazz.kind == ClassKind.Interface)

    val className = clazz.className
    val classInfo = ctx.getClassInfo(className)

    val fb = new FunctionBuilder(
      ctx.moduleBuilder,
      genFunctionID.instanceTest(className),
      makeDebugName(ns.IsInstance, className),
      clazz.pos
    )
    val exprParam = fb.addParam("expr", watpe.RefType.anyref)
    fb.setResultType(watpe.Int32)

    if (!clazz.hasInstances) {
      /* Interfaces that do not have instances do not receive an itable index,
       * so the codegen below would not work. Return a constant false instead.
       */
      fb += wa.I32Const(0) // false
    } else {
      fb.block(watpe.RefType.anyref) { testFail =>
        // if expr is not an instance of Object, return false
        fb += wa.LocalGet(exprParam)
        fb += wa.BrOnCastFail(
          testFail,
          watpe.RefType.anyref,
          watpe.RefType(genTypeID.ObjectStruct)
        )

        /* Test whether the itable at the target interface's slot is indeed an
         * instance of that interface's itable struct type.
         */
        fb += wa.StructGet(genTypeID.ObjectStruct, genFieldID.objStruct.vtable)
        fb += wa.StructGet(
          genTypeID.ObjectVTable,
          genFieldID.vtableStruct.itableSlot(classInfo.itableIdx)
        )
        fb += wa.RefTest(watpe.RefType(genTypeID.forITable(className)))
        fb += wa.Return
      } // test fail

      if (classInfo.isAncestorOfHijackedClass) {
        /* It could be a hijacked class instance that implements this interface.
         * Test whether `jsValueType(expr)` is in the `specialInstanceTypes` bitset.
         * In other words, return `((1 << jsValueType(expr)) & specialInstanceTypes) != 0`.
         *
         * For example, if this class is `Comparable`,
         * `specialInstanceTypes == 0b00001111`, since `jl.Boolean`, `jl.String`
         * and `jl.Double` implement `Comparable`, but `jl.Void` does not.
         * If `expr` is a `number`, `jsValueType(expr) == 3`. We then test whether
         * `(1 << 3) & 0b00001111 != 0`, which is true because `(1 << 3) == 0b00001000`.
         * If `expr` is `undefined`, it would be `(1 << 4) == 0b00010000`, which
         * would give `false`.
         */
        val anyRefToVoidSig = watpe.FunctionType(List(watpe.RefType.anyref), Nil)

        val exprNonNullLocal = fb.addLocal("exprNonNull", watpe.RefType.any)

        fb.block(anyRefToVoidSig) { isNullLabel =>
          // exprNonNull := expr; branch to isNullLabel if it is null
          fb += wa.BrOnNull(isNullLabel)
          fb += wa.LocalSet(exprNonNullLocal)

          // Load 1 << jsValueType(expr)
          fb += wa.I32Const(1)
          fb += wa.LocalGet(exprNonNullLocal)
          if (targetPureWasm) fb += wa.Call(genFunctionID.scalaValueType)
          else fb += wa.Call(genFunctionID.jsValueType)
          fb += wa.I32Shl

          // return (... & specialInstanceTypes) != 0
          fb += wa.I32Const(classInfo.specialInstanceTypes)
          fb += wa.I32And
          fb += wa.I32Const(0)
          fb += wa.I32Ne
          fb += wa.Return
        }

        fb += wa.I32Const(0) // false
      } else {
        fb += wa.Drop
        fb += wa.I32Const(0) // false
      }
    }

    fb.buildAndAddToModule()
  }

  /** Generate the cast function for an interface.
   *
   *  When `asInstanceOfs` are checked, the expression `asInstanceOf[<interface>]`
   *  will be compiled to a CALL to the function generated by this method.
   */
  private def genInterfaceCastFunction(clazz: LinkedClass)(
      implicit ctx: WasmContext): Unit = {
    assert(clazz.kind == ClassKind.Interface)

    val className = clazz.className
    val resultType = TypeTransformer.transformClassType(className, nullable = true)

    val fb = new FunctionBuilder(
      ctx.moduleBuilder,
      genFunctionID.asInstance(ClassType(className, nullable = true)),
      makeDebugName(ns.AsInstance, className),
      clazz.pos
    )
    val objParam = fb.addParam("obj", watpe.RefType.anyref)
    fb.setResultType(resultType)

    fb.block() { successLabel =>
      // Succeed if null
      fb += wa.LocalGet(objParam)
      fb += wa.BrOnNull(successLabel)

      // Succeed if the instance test succeeds
      fb += wa.Call(genFunctionID.instanceTest(className))
      fb += wa.BrIf(successLabel)

      // If we get here, it's a CCE
      fb += wa.LocalGet(objParam)
      fb += wa.GlobalGet(genGlobalID.forVTable(className))
      fb += wa.Call(genFunctionID.classCastException)
      SWasmGen.genForwardThrowAlwaysAsReturn(fb, List(wa.RefNull(watpe.HeapType.None)))
    }

    fb += wa.LocalGet(objParam)
    if (resultType != watpe.RefType.anyref)
      fb += wa.RefCast(resultType)

    fb.buildAndAddToModule()
  }

  private def genNewDefaultFunc(clazz: LinkedClass)(implicit ctx: WasmContext): Unit = {
    val className = clazz.name.name
    val classInfo = ctx.getClassInfo(className)
    assert(clazz.hasDirectInstances)

    val structTypeID = genTypeID.forClass(className)
    val fb = new FunctionBuilder(
      ctx.moduleBuilder,
      genFunctionID.newDefault(className),
      makeDebugName(ns.NewDefault, className),
      clazz.pos
    )
    val dataParamOpt =
      if (className == ClassClass) Some(fb.addParam("data", watpe.RefType(genTypeID.typeData)))
      else None
    fb.setResultType(watpe.RefType(structTypeID))

    fb += wa.GlobalGet(genGlobalID.forVTable(className))

    // idHashCode
    if (targetPureWasm) fb += wa.I32Const(0)

    classInfo.allFieldDefs.foreach { f =>
      fb += genZeroOf(f.ftpe)
    }
    for (dataParam <- dataParamOpt)
      fb += wa.LocalGet(dataParam)
    fb += wa.StructNew(structTypeID)

    fb.buildAndAddToModule()
  }

  /** Generates the clone function for the given class, if it is concrete and
   *  implements the Cloneable interface.
   *
   *  The generated clone function will be registered in the typeData of the class (which
   *  resides in the vtable of the class), and will be invoked for a `Clone` IR tree on
   *  the class instance.
   */
  private def genCloneFunction(clazz: LinkedClass)(implicit ctx: WasmContext): Unit = {
    val className = clazz.className
    val info = ctx.getClassInfo(className)

    val fb = new FunctionBuilder(
      ctx.moduleBuilder,
      genFunctionID.clone(className),
      makeDebugName(ns.Clone, className),
      clazz.pos
    )
    val fromParam = fb.addParam("from", watpe.RefType(genTypeID.ObjectStruct))
    fb.setResultType(watpe.RefType(genTypeID.ObjectStruct))
    fb.setFunctionType(genTypeID.cloneFunctionType)

    val structTypeID = genTypeID.forClass(className)
    val structRefType = watpe.RefType(structTypeID)

    val fromTypedLocal = fb.addLocal("fromTyped", structRefType)

    // Downcast fromParam to fromTyped
    fb += wa.LocalGet(fromParam)
    fb += wa.RefCast(structRefType)
    fb += wa.LocalSet(fromTypedLocal)

    // Push the vtable on the stack
    fb += wa.GlobalGet(genGlobalID.forVTable(className))

    if (targetPureWasm) fb += wa.I32Const(0)

    // Push every field of `fromTyped` on the stack
    info.allFieldDefs.foreach { field =>
      fb += wa.LocalGet(fromTypedLocal)
      fb += wa.StructGet(structTypeID, genFieldID.forClassInstanceField(field.name.name))
    }

    // Create the result
    fb += wa.StructNew(structTypeID)

    fb.buildAndAddToModule()
  }

  /** Generate the cast function for a class.
   *
   *  When `asInstanceOfs` are checked, the expression `asInstanceOf[<class>]`
   *  will be compiled to a CALL to the function generated by this method.
   */
  private def genClassCastFunction(clazz: LinkedClass)(implicit ctx: WasmContext): Unit = {
    val className = clazz.className

    val resultType = TypeTransformer.transformClassType(className, nullable = true)

    val fb = new FunctionBuilder(
      ctx.moduleBuilder,
      genFunctionID.asInstance(ClassType(clazz.className, nullable = true)),
      makeDebugName(ns.AsInstance, className),
      clazz.pos
    )
    val objParam = fb.addParam("obj", watpe.RefType.anyref)
    fb.setResultType(resultType)

    fb.block(resultType) { successLabel =>
      fb += wa.LocalGet(objParam)

      if (className == SpecialNames.JLNumberClass) {
        /* jl.Number is special, because it is the only non-Object *class*
         * that is an  ancestor of a hijacked class.
         */
        fb += wa.BrOnCast(successLabel, watpe.RefType.anyref,
            watpe.RefType.nullable(genTypeID.forClass(SpecialNames.JLNumberClass)))

        /* The `obj` still on the stack will be used for:
         * a) the result in the true case
         * b) consistency with non-Number in the false case
         */

        fb += wa.LocalGet(objParam)
        fb += wa.Call(genFunctionID.typeTest(DoubleRef))
        fb += wa.BrIf(successLabel)
      } else {
        fb += wa.BrOnCast(successLabel, watpe.RefType.anyref, resultType)
      }

      // If we get here, it's a CCE -- `obj` is still on the stack
      fb += wa.GlobalGet(genGlobalID.forVTable(className))
      fb += wa.Call(genFunctionID.classCastException)
      SWasmGen.genForwardThrowAlwaysAsReturn(fb, List(wa.RefNull(watpe.HeapType.None)))
    }

    fb.buildAndAddToModule()
  }

  private def genModuleAccessor(clazz: LinkedClass)(implicit ctx: WasmContext): Unit = {
    assert(clazz.kind == ClassKind.ModuleClass)

    val className = clazz.className
    val globalInstanceID = genGlobalID.forModuleInstance(className)
    val ctorID =
      genFunctionID.forMethod(MemberNamespace.Constructor, className, NoArgConstructorName)
    val resultType = watpe.RefType(genTypeID.forClass(className))

    val fb = new FunctionBuilder(
      ctx.moduleBuilder,
      genFunctionID.loadModule(clazz.className),
      makeDebugName(ns.ModuleAccessor, className),
      clazz.pos
    )
    if (semantics.moduleInit == CheckedBehavior.Compliant)
      fb.setResultType(resultType.toNullable)
    else
      fb.setResultType(resultType)

    val instanceLocal = fb.addLocal("instance", resultType)

    fb.block(resultType) { nonNullLabel =>
      // load global, return if not null
      fb += wa.GlobalGet(globalInstanceID)
      fb += wa.BrOnNonNull(nonNullLabel)

      // check ongoing initialization
      if (semantics.moduleInit != CheckedBehavior.Unchecked) {
        val initFlagID = genGlobalID.forModuleInitFlag(className)

        // if being initialized
        fb += wa.GlobalGet(initFlagID)
        fb.ifThen() {
          if (semantics.moduleInit == CheckedBehavior.Compliant) {
            // then, return null
            fb += wa.RefNull(watpe.HeapType.None)
            fb += wa.Return
          } else {
            // then, throw
            fb += wa.GlobalGet(genGlobalID.forVTable(className))
            fb += wa.Call(genFunctionID.throwModuleInitError)

            // the fake result is particularly awkward here
            SWasmGen.genForwardThrowAlwaysAsReturn(fb, List(wa.Call(genFunctionID.newDefault(className))))
          }
        }

        // mark as being initialized
        fb += wa.I32Const(1)
        fb += wa.GlobalSet(initFlagID)
      }

      // create an instance and call its constructor
      fb += wa.Call(genFunctionID.newDefault(className))
      fb += wa.LocalTee(instanceLocal)
      fb += wa.Call(ctorID)
      SWasmGen.genForwardThrowAsReturn(fb, List(wa.LocalGet(instanceLocal)))

      // store it in the global
      fb += wa.LocalGet(instanceLocal)
      fb += wa.GlobalSet(globalInstanceID)

      // return it
      fb += wa.LocalGet(instanceLocal)
    }

    fb.buildAndAddToModule()
  }

  private def genInterface(clazz: LinkedClass)(implicit ctx: WasmContext): Unit = {
    assert(clazz.kind == ClassKind.Interface)
    // gen itable type
    val className = clazz.name.name
    val classInfo = ctx.getClassInfo(clazz.className)
    val itableTypeID = genTypeID.forITable(className)
    val itableType = watpe.StructType(
      classInfo.tableEntries.map { methodName =>
        watpe.StructField(
          genFieldID.forMethodTableEntry(methodName),
          makeDebugName(ns.TableEntry, className, methodName),
          watpe.RefType(ctx.tableFunctionType(methodName)),
          isMutable = false
        )
      }
    )
    ctx.mainRecType.addSubType(
      itableTypeID,
      makeDebugName(ns.ITable, className),
      itableType
    )

    if (clazz.hasInstanceTests) {
      genInterfaceInstanceTest(clazz)
      if (semantics.asInstanceOfs != CheckedBehavior.Unchecked)
        genInterfaceCastFunction(clazz)
    }
  }

  private def genJSClass(clazz: LinkedClass)(implicit ctx: WasmContext): Unit = {
    assert(clazz.kind.isJSClass)

    // Define the globals holding the Symbols of private fields
    for (fieldDef <- clazz.fields) {
      fieldDef match {
        case FieldDef(flags, name, _, _) if !flags.namespace.isStatic =>
          ctx.addGlobal(
            wamod.Global(
              genGlobalID.forJSPrivateField(name.name),
              makeDebugName(ns.PrivateJSField, name.name),
              isMutable = true,
              watpe.RefType.anyref,
              wa.Expr(List(wa.RefNull(watpe.HeapType.Any)))
            )
          )
        case _ =>
          ()
      }
    }

    if (clazz.hasInstances) {
      genCreateJSClassFunction(clazz)

      if (clazz.jsClassCaptures.isEmpty)
        genLoadJSClassFunction(clazz)

      if (clazz.kind == ClassKind.JSModuleClass)
        genLoadJSModuleFunction(clazz)
    }
  }

  private def genCreateJSClassFunction(clazz: LinkedClass)(implicit ctx: WasmContext): Unit = {
    implicit val pos: Position = Position.NoPosition

    val className = clazz.className
    val jsClassCaptures = clazz.jsClassCaptures.getOrElse(Nil)

    /* We need to decompose the body of the constructor into 3 closures.
     * Given an IR constructor of the form
     *   constructor(...params) {
     *     preSuperStats;
     *     super(...superArgs);
     *     postSuperStats;
     *   }
     * We will create closures for `preSuperStats`, `superArgs` and `postSuperStats`.
     *
     * There is one huge catch: `preSuperStats` can declare `VarDef`s at its top-level,
     * and those vars are still visible inside `superArgs` and `postSuperStats`.
     * The `preSuperStats` must therefore return a struct with the values of its
     * declared vars, which will be given as an additional argument to `superArgs`
     * and `postSuperStats`. We call that struct the `preSuperEnv`.
     *
     * In the future, we should optimize `preSuperEnv` to only store locals that
     * are still used by `superArgs` and/or `postSuperArgs`.
     */

    val preSuperStatsFunctionID = genFunctionID.preSuperStats(className)
    val superArgsFunctionID = genFunctionID.superArgs(className)
    val postSuperStatsFunctionID = genFunctionID.postSuperStats(className)
    val ctor = clazz.jsConstructorDef.get

    FunctionEmitter.emitJSConstructorFunctions(
      preSuperStatsFunctionID,
      superArgsFunctionID,
      postSuperStatsFunctionID,
      className,
      jsClassCaptures,
      ctor
    )

    val fb = new FunctionBuilder(
      ctx.moduleBuilder,
      genFunctionID.createJSClassOf(className),
      makeDebugName(ns.CreateJSClass, className),
      clazz.pos
    )
    val classCaptureParams = jsClassCaptures.map { cc =>
      fb.addParam("cc." + cc.name.name.nameString, transformParamType(cc.ptpe))
    }
    fb.setResultType(watpe.RefType.any)

    val dataStructTypeID = ctx.getClosureDataStructType(jsClassCaptures.map(_.ptpe))

    val dataStructLocal = fb.addLocal("classCaptures", watpe.RefType(dataStructTypeID))
    val jsClassLocal = fb.addLocal("jsClass", watpe.RefType.any)

    // Build the actual `createJSClass` helper
    val createJSClassHelperID: wanme.FunctionID = {
      // --- Actual start of instructions of `createJSClass`

      // Bundle class captures in a capture data struct
      for (classCaptureParam <- classCaptureParams)
        fb += wa.LocalGet(classCaptureParam)
      fb += wa.StructNew(dataStructTypeID)
      fb += wa.LocalSet(dataStructLocal)

      val classCaptureParamsOfTypeAny: Map[LocalName, wanme.LocalID] = {
        jsClassCaptures
          .zip(classCaptureParams)
          .collect { case (ParamDef(ident, _, AnyType, _), param) =>
            ident.name -> param
          }
          .toMap
      }

      val helperBuilder = new CustomJSHelperBuilder.WithTreeEval() {
        protected def evalTreeAtCallSite(tree: Tree, expectedType: Type): Unit = tree match {
          case VarRef(localName) if classCaptureParamsOfTypeAny.contains(localName) =>
            /* Common shape for the `jsSuperClass` value
             * We can only deal with class captures of type `AnyType` in this way,
             * since otherwise we might need `adapt` to box the values.
             */
            fb += wa.LocalGet(classCaptureParamsOfTypeAny(localName))

          case _ =>
            // For everything else, put the tree in its own function and call it
            val closureFuncID = new JSClassClosureFunctionID(className)
            FunctionEmitter.emitFunction(
              closureFuncID,
              NoOriginalName,
              enclosingClassName = None,
              Some(jsClassCaptures),
              receiverType = None,
              paramDefs = Nil,
              restParam = None,
              tree,
              AnyType
            )
            fb += wa.LocalGet(dataStructLocal)
            fb += wa.Call(closureFuncID)
        }
      }

      /* Get a Tree for the super constructor; specified by
       * https://lampwww.epfl.ch/~doeraene/sjsir-semantics/#sec-sjsir-classdef-runtime-semantics-evaluation
       * - if `jsSuperClass` is defined, evaluate it;
       * - otherwise load the JS constructor of the declared superClass,
       *   as if by `LoadJSConstructor`.
       */
      val jsSuperClassTree = clazz.jsSuperClass.getOrElse {
        LoadJSConstructor(clazz.superClass.get.name)
      }

      val dataRef = helperBuilder.addWasmInput("data", watpe.RefType(dataStructTypeID)) {
        fb += wa.LocalGet(dataStructLocal)
      }
      val preSuperStatsFunctionRef = helperBuilder.addWasmInput("preSuperStats", watpe.RefType.func) {
        fb += ctx.refFuncWithDeclaration(preSuperStatsFunctionID)
      }
      val superArgsFunctionRef = helperBuilder.addWasmInput("superArgs", watpe.RefType.func) {
        fb += ctx.refFuncWithDeclaration(superArgsFunctionID)
      }
      val postSuperStatsFunctionRef = helperBuilder.addWasmInput("postSuperStats", watpe.RefType.func) {
        fb += ctx.refFuncWithDeclaration(postSuperStatsFunctionID)
      }

      def genDefineProperty(obj: js.Tree, name: js.Tree, value: js.Tree): js.Tree = {
        js.Apply(
          js.DotSelect(js.VarRef(js.Ident("Object")), js.Ident("defineProperty")),
          List(
            obj,
            name,
            js.ObjectConstr(
              List(
                js.Ident("configurable") -> js.BooleanLiteral(true),
                js.Ident("enumerable") -> js.BooleanLiteral(true),
                js.Ident("writable") -> js.BooleanLiteral(true),
                js.Ident("value") -> value
              )
            )
          )
        )
      }

      def toJSPropertyName(tree: js.Tree): js.PropertyName = tree match {
        case js.StringLiteral("constructor")                                  => js.ComputedName(tree)
        case js.StringLiteral(name) if js.Ident.isValidJSIdentifierName(name) => js.Ident(name)
        case tree: js.StringLiteral                                           => tree
        case _                                                                => js.ComputedName(tree)
      }

      val jsClassIdent = helperBuilder.newLocalIdent("cls")

      val jsCtorDef: js.MethodDef = {
        val JSConstructorDef(_, params, restParam, body) = ctor
        val (paramDefs, restParamDef) = helperBuilder.genJSParamDefs(params, restParam)
        val allParamRefs = (paramDefs ::: restParamDef.toList).map(_.ref)
        js.MethodDef(static = false, js.Ident("constructor"), paramDefs, restParamDef, {
          val preSuperEnv = helperBuilder.newLocalIdent("preSuperEnv")
          js.Block(
            // var preSuperEnv = preSuperStats(data, new.target, ...allParamRefs);
            js.VarDef(preSuperEnv, Some(js.Apply(preSuperStatsFunctionRef,
                dataRef :: js.NewTarget() :: allParamRefs))),
            // super(...superArgs(data, preSuperEnv, new.target, ...args));
            js.Apply(
              js.Super(),
              List(
                js.Spread(
                  js.Apply(
                    superArgsFunctionRef,
                    dataRef :: js.VarRef(preSuperEnv) :: js.NewTarget() :: allParamRefs
                  )
                )
              )
            ),
            // Initialize fields to the (boxed) zero of their type
            js.Block(for (fieldDef <- clazz.fields if !fieldDef.flags.namespace.isStatic) yield {
              val nameRef = fieldDef match {
                case FieldDef(_, name, _, _) =>
                  helperBuilder.addWasmInput("name", watpe.RefType.anyref) {
                    fb += wa.GlobalGet(genGlobalID.forJSPrivateField(name.name))
                  }
                case JSFieldDef(_, nameTree, _) =>
                  helperBuilder.addInput(nameTree)
              }
              val valueRef = helperBuilder.addInput(zeroOf(fieldDef.ftpe))
              genDefineProperty(js.This(), nameRef, valueRef)
            }),
            // postSuperStats(data, preSuperEnv, new.target, this, ...args);
            js.Apply(postSuperStatsFunctionRef,
                dataRef :: js.VarRef(preSuperEnv) :: js.NewTarget() :: js.This() :: allParamRefs)
          )
        })
      }

      // Methods and properties
      val jsMethodProps: List[js.Tree] = clazz.exportedMembers.flatMap { methodOrProp =>
        val isStatic = methodOrProp.flags.namespace.isStatic
        val jsThisUnlessStatic = if (isStatic) Nil else List(js.This())

        val receiverType = if (isStatic) None else Some(watpe.RefType.anyref)

        methodOrProp match {
          case JSMethodDef(flags, nameTree, params, restParam, body) =>
            val nameRef = toJSPropertyName(helperBuilder.addInput(nameTree))

            val closureFuncID = new JSClassClosureFunctionID(className)
            FunctionEmitter.emitFunction(
              closureFuncID,
              NoOriginalName, // TODO Come up with something here?
              Some(className),
              Some(jsClassCaptures),
              receiverType,
              params,
              restParam,
              body,
              AnyType
            )
            val fRef = helperBuilder.addWasmInput("f", watpe.RefType.func) {
              fb += ctx.refFuncWithDeclaration(closureFuncID)
            }

            val (argsParamDefs, restParamDef) = helperBuilder.genJSParamDefs(params, restParam)
            val jsMethodDef = js.MethodDef(isStatic, nameRef, argsParamDefs, restParamDef, {
              js.Return(js.Apply(
                  fRef,
                  dataRef ::
                  jsThisUnlessStatic :::
                  argsParamDefs.map(_.ref) :::
                  restParamDef.map(_.ref).toList
              ))
            })

            List(jsMethodDef)

          case JSPropertyDef(flags, nameTree, optGetter, optSetter) =>
            val nameRef = toJSPropertyName(helperBuilder.addInput(nameTree))

            val jsGetter = optGetter.map { getterBody =>
              val closureFuncID = new JSClassClosureFunctionID(className)
              FunctionEmitter.emitFunction(
                closureFuncID,
                NoOriginalName, // TODO Come up with something here?
                Some(className),
                Some(jsClassCaptures),
                receiverType,
                paramDefs = Nil,
                restParam = None,
                getterBody,
                resultType = AnyType
              )
              val getterRef = helperBuilder.addWasmInput("get", watpe.RefType.func) {
                fb += ctx.refFuncWithDeclaration(closureFuncID)
              }
              js.GetterDef(isStatic, nameRef, {
                js.Return(js.Apply(getterRef, dataRef :: jsThisUnlessStatic))
              })
            }

            val jsSetter = optSetter.map { setter =>
              val (setterParamDef, setterBody) = setter
              val closureFuncID = new JSClassClosureFunctionID(className)
              FunctionEmitter.emitFunction(
                closureFuncID,
                NoOriginalName, // TODO Come up with something here?
                Some(className),
                Some(jsClassCaptures),
                receiverType,
                setterParamDef :: Nil,
                restParam = None,
                setterBody,
                resultType = VoidType
              )
              val setterRef = helperBuilder.addWasmInput("set", watpe.RefType.func) {
                fb += ctx.refFuncWithDeclaration(closureFuncID)
              }
              val jsSetterParamDef = helperBuilder.genJSParamDef(setterParamDef)
              js.SetterDef(isStatic, nameRef, jsSetterParamDef, {
                js.Apply(setterRef, dataRef :: jsThisUnlessStatic ::: jsSetterParamDef.ref :: Nil)
              })
            }

            jsGetter.toList ::: jsSetter.toList
        }
      }

      val jsSuperClass = helperBuilder.addInput(jsSuperClassTree)
      val jsClassDef = js.ClassDef(Some(jsClassIdent), Some(jsSuperClass),
          jsCtorDef :: jsMethodProps)

      // Static fields
      val jsInitStaticFields = for {
        fieldDef <- clazz.fields if fieldDef.flags.namespace.isStatic
      } yield {
        // Name
        val nameRef = fieldDef match {
          case FieldDef(_, name, _, _) =>
            throw new AssertionError(
              s"Unexpected private static field ${name.name.nameString} "
                + s"in JS class ${className.nameString}"
            )
          case JSFieldDef(_, nameTree, _) =>
            helperBuilder.addInput(nameTree)
        }

        // Generate boxed representation of the zero of the field
        val valueRef = helperBuilder.addInput(zeroOf(fieldDef.ftpe))

        genDefineProperty(js.VarRef(jsClassIdent), nameRef, valueRef)
      }

      // Complete the helper
      helperBuilder.build(AnyNotNullType) {
        js.Block(
          jsClassDef ::
          jsInitStaticFields :::
          js.Return(js.VarRef(jsClassIdent)) ::
          Nil
        )
      }
    }

    // Call the helper to produce the JS class value
    fb += wa.Call(createJSClassHelperID)

    // Store the result, locally in `jsClass` and possibly in the global cache
    if (clazz.jsClassCaptures.isEmpty) {
      /* Static JS class with a global cache. We must fill the global cache
       * before we call the class initializer, later in the current function.
       */
      fb += wa.LocalTee(jsClassLocal)
      fb += wa.GlobalSet(genGlobalID.forJSClassValue(className))
    } else {
      // Local or inner JS class, which is new every time
      fb += wa.LocalSet(jsClassLocal)
    }

    // Class initializer
    if (clazz.methods.exists(_.methodName.isClassInitializer)) {
      assert(
        clazz.jsClassCaptures.isEmpty,
        s"Illegal class initializer in non-static class ${className.nameString}"
      )
      val namespace = MemberNamespace.StaticConstructor
      fb += wa.Call(
        genFunctionID.forMethod(namespace, className, ClassInitializerName)
      )
    }

    // Final result
    fb += wa.LocalGet(jsClassLocal)

    fb.buildAndAddToModule()
  }

  private def genLoadJSClassFunction(clazz: LinkedClass)(implicit ctx: WasmContext): Unit = {
    require(clazz.jsClassCaptures.isEmpty)

    val className = clazz.className

    val cachedJSClassGlobal = wamod.Global(
      genGlobalID.forJSClassValue(className),
      makeDebugName(ns.JSClassValueCache, className),
      isMutable = true,
      watpe.RefType.anyref,
      wa.Expr(List(wa.RefNull(watpe.HeapType.Any)))
    )
    ctx.addGlobal(cachedJSClassGlobal)

    val fb = new FunctionBuilder(
      ctx.moduleBuilder,
      genFunctionID.loadJSClass(className),
      makeDebugName(ns.JSClassAccessor, className),
      clazz.pos
    )
    fb.setResultType(watpe.RefType.any)

    fb.block(watpe.RefType.any) { doneLabel =>
      // Load cached JS class, return if non-null
      fb += wa.GlobalGet(cachedJSClassGlobal.id)
      fb += wa.BrOnNonNull(doneLabel)
      // Otherwise, call createJSClass -- it will also store the class in the cache
      fb += wa.Call(genFunctionID.createJSClassOf(className))
    }

    fb.buildAndAddToModule()
  }

  private def genLoadJSModuleFunction(clazz: LinkedClass)(implicit ctx: WasmContext): Unit = {
    val className = clazz.className
    val cacheGlobalID = genGlobalID.forModuleInstance(className)

    ctx.addGlobal(
      wamod.Global(
        cacheGlobalID,
        makeDebugName(ns.ModuleInstance, className),
        isMutable = true,
        watpe.RefType.anyref,
        wa.Expr(List(wa.RefNull(watpe.HeapType.Any)))
      )
    )

    val fb = new FunctionBuilder(
      ctx.moduleBuilder,
      genFunctionID.loadModule(className),
      makeDebugName(ns.ModuleAccessor, className),
      clazz.pos
    )
    fb.setResultType(watpe.RefType.anyref)

    fb.block(watpe.RefType.anyref) { doneLabel =>
      // Load cached instance; return if non-null
      fb += wa.GlobalGet(cacheGlobalID)
      fb += wa.BrOnNonNull(doneLabel)

      // Get the JS class and instantiate it
      fb += wa.Call(genFunctionID.loadJSClass(className))
      fb += wa.Call(genFunctionID.jsNewNoArg)

      // Store and return the result
      fb += wa.GlobalSet(cacheGlobalID)
      fb += wa.GlobalGet(cacheGlobalID)
    }

    fb.buildAndAddToModule()
  }

  /** Generates the function import for a top-level export setter. */
  private def genTopLevelExportSetter(exportedName: String)(implicit ctx: WasmContext): Unit = {
    val functionID = genFunctionID.forTopLevelExportSetter(exportedName)
    val functionSig = watpe.FunctionType(List(watpe.RefType.anyref), Nil)
    val functionType = ctx.moduleBuilder.functionTypeToTypeID(functionSig)

    ctx.moduleBuilder.addImport(
      wamod.Import(
        ExportSettersModule,
        exportedName,
        wamod.ImportDesc.Func(
          functionID,
          makeDebugName(ns.TopLevelExportSetter, exportedName),
          functionType
        )
      )
    )
  }

  private def genTopLevelMethodExportDef(exportDef: TopLevelMethodExportDef)(
      implicit ctx: WasmContext): Unit = {
    implicit val pos = exportDef.pos

    val method = exportDef.methodDef
    val exportedName = exportDef.topLevelExportName
    val functionID = genFunctionID.forExport(exportedName)

    FunctionEmitter.emitFunction(
      functionID,
      makeDebugName(ns.TopLevelExport, exportedName),
      enclosingClassName = None,
      captureParamDefs = None,
      receiverType = None,
      method.args,
      method.restParam,
      method.body,
      resultType = AnyType
    )
  }

  private def genMethod(clazz: LinkedClass, method: MethodDef)(
      implicit ctx: WasmContext): Unit = {
    implicit val pos = method.pos

    val namespace = method.flags.namespace
    val className = clazz.className
    val methodName = method.methodName

    val functionID = genFunctionID.forMethod(namespace, className, methodName)

    val namespaceUTF8String = namespace match {
      case MemberNamespace.Public            => ns.Public
      case MemberNamespace.PublicStatic      => ns.PublicStatic
      case MemberNamespace.Private           => ns.Private
      case MemberNamespace.PrivateStatic     => ns.PrivateStatic
      case MemberNamespace.Constructor       => ns.Constructor
      case MemberNamespace.StaticConstructor => ns.StaticConstructor
    }
    val originalName = makeDebugName(namespaceUTF8String, className, methodName)

    val isHijackedClass = clazz.kind == ClassKind.HijackedClass

    val receiverType =
      if (namespace.isStatic)
        None
      else if (isHijackedClass)
        Some(transformPrimType(BoxedClassToPrimType(className)))
      else
        Some(transformClassType(className, nullable = false))

    val body = method.body.getOrElse(throw new Exception("abstract method cannot be transformed"))

    // Emit the function
    if (!ctx.coreSpec.wasmFeatures.componentModel &&
        className == SpecialNames.WasmSystemClass &&
        namespace == MemberNamespace.Public && !methodName.isReflectiveProxy) {
      emitSpecialMethod(
        functionID,
        originalName,
        className,
        methodName,
        receiverType.get,
        method.args,
        method.resultType
      )
    } else {
      FunctionEmitter.emitFunction(
        functionID,
        originalName,
        Some(className),
        captureParamDefs = None,
        receiverType,
        method.args,
        restParam = None,
        body,
        method.resultType
      )
    }

    if (namespace == MemberNamespace.Public && !isHijackedClass) {
      /* Also generate the bridge that is stored in the table entries. In table
       * entries, the receiver type is always `(ref any)`.
       *
       * TODO: generate this only when the method is actually referred to from
       * at least one table.
       */

      val fb = new FunctionBuilder(
        ctx.moduleBuilder,
        genFunctionID.forTableEntry(className, methodName),
        makeDebugName(ns.TableEntry, className, methodName),
        pos
      )
      val receiverParam = fb.addParam(thisOriginalName, watpe.RefType.any)
      val argParams = method.args.map { arg =>
        val origName = arg.originalName.orElse(arg.name.name)
        fb.addParam(origName, TypeTransformer.transformParamType(arg.ptpe))
      }
      fb.setResultTypes(TypeTransformer.transformResultType(method.resultType))
      fb.setFunctionType(ctx.tableFunctionType(methodName))

      // Load and cast down the receiver
      fb += wa.LocalGet(receiverParam)
      receiverType match {
        case Some(watpe.RefType(_, watpe.HeapType.Any)) =>
          () // no cast necessary
        case Some(receiverType: watpe.RefType) =>
          fb += wa.RefCast(receiverType)
        case _ =>
          throw new AssertionError(s"Unexpected receiver type $receiverType")
      }

      // Load the other parameters
      for (argParam <- argParams)
        fb += wa.LocalGet(argParam)

      // Call the statically resolved method
      fb += wa.ReturnCall(functionID)

      fb.buildAndAddToModule()
    }
  }

  private def emitSpecialMethod(
      functionID: wanme.FunctionID,
      originalName: OriginalName,
      enclosingClassName: ClassName,
      methodName: MethodName,
      receiverType: watpe.Type,
      paramDefs: List[ParamDef],
      resultType: Type
  )(implicit ctx: WasmContext, pos: Position): Unit = {
    val fb = new FunctionBuilder(ctx.moduleBuilder, functionID, originalName, pos)
    val receiverParam = fb.addParam("this", receiverType)
    val paramLocals = paramDefs.map { paramDef =>
      fb.addParam(paramDef.originalName.orElse(paramDef.name.name),
          transformParamType(paramDef.ptpe))
    }
    fb.setResultTypes(transformResultType(resultType))

    methodName.simpleName.nameString match {
      case "print" =>
        fb += wa.LocalGet(paramLocals(0))
        fb += wa.RefAsNonNull
        fb += wa.Call(genFunctionID.wasmString.getWholeChars)
        fb += wa.Call(genFunctionID.wasmEssentials.print)

      case "nanoTime" =>
        fb += wa.Call(genFunctionID.wasmEssentials.nanoTime)
        fb += wa.I64TruncSatF64S

      case "currentTimeMillis" =>
        fb += wa.Call(genFunctionID.wasmEssentials.currentTimeMillis)
        fb += wa.I64TruncSatF64S

      case "random" =>
        fb += wa.Call(genFunctionID.wasmEssentials.random)

      case _ =>
        throw new AssertionError(s"Unknown WasmSystem method ${methodName.nameString}")
    }

    fb.buildAndAddToModule()
  }

  private def makeDebugName(namespace: UTF8String, exportedName: String): OriginalName =
    OriginalName(namespace ++ UTF8String(exportedName))

  private def makeDebugName(namespace: UTF8String, className: ClassName): OriginalName =
    OriginalName(namespace ++ className.encoded)

  private def makeDebugName(namespace: UTF8String, fieldName: FieldName): OriginalName = {
    OriginalName(
      namespace ++ fieldName.className.encoded ++ dotUTF8String ++ fieldName.simpleName.encoded
    )
  }

  private def makeDebugName(
      namespace: UTF8String,
      className: ClassName,
      methodName: MethodName
  ): OriginalName = {
    // TODO Opt: directly encode the MethodName rather than using nameString
    val methodNameUTF8 = UTF8String(methodName.nameString)
    OriginalName(namespace ++ className.encoded ++ dotUTF8String ++ methodNameUTF8)
  }
}

object ClassEmitter {
  private final class JSClassClosureFunctionID(classNameDebug: ClassName) extends wanme.FunctionID {
    override def toString(): String =
      s"JSClassClosureFunctionID(${classNameDebug.nameString})"
  }

  private val dotUTF8String: UTF8String = UTF8String(".")

  // These particular names are the same as in the JS backend
  private object ns {
    // Shared with JS backend -- className + methodName
    val Public = UTF8String("f.")
    val PublicStatic = UTF8String("s.")
    val Private = UTF8String("p.")
    val PrivateStatic = UTF8String("ps.")
    val Constructor = UTF8String("ct.")
    val StaticConstructor = UTF8String("sct.")

    // Shared with JS backend -- fieldName
    val StaticField = UTF8String("t.")
    val PrivateJSField = UTF8String("r.")

    // Shared with JS backend -- className
    val ModuleAccessor = UTF8String("m.")
    val ModuleInstance = UTF8String("n.")
    val ModuleInitFlag = UTF8String("ni.")
    val JSClassAccessor = UTF8String("a.")
    val JSClassValueCache = UTF8String("b.")
    val TypeData = UTF8String("d.")
    val IsInstance = UTF8String("is.")
    val AsInstance = UTF8String("as.")

    // Shared with JS backend -- string
    val TopLevelExport = UTF8String("e.")
    val TopLevelExportSetter = UTF8String("u.")

    // Wasm only -- className + methodName
    val TableEntry = UTF8String("m.")

    // Wasm only -- fieldName
    val InstanceField = UTF8String("f.")

    // Wasm only -- className
    val ClassInstance = UTF8String("c.")
    val CreateJSClass = UTF8String("c.")
    val VTable = UTF8String("v.")
    val ITable = UTF8String("it.")
    val Clone = UTF8String("clone.")
    val NewDefault = UTF8String("new.")
  }

  private val thisOriginalName: OriginalName = OriginalName("this")
  private val vtableOriginalName: OriginalName = OriginalName("vtable")

  /** Generates the itable slots of a class.
   *
   *  @param classInfoForResolving
   *    The `ClassInfo` from which to resolve methods. This is normally the
   *    class info of the class for which we are generating the itable slots.
   *    For the itable slots of array classes, it must be the info of `jl.Object`.
   *  @param ancestors
   *    The list of ancestors of the target class.
   */
  def genItableSlots(classInfoForResolving: WasmContext.ClassInfo,
      ancestors: List[ClassName])(
      implicit ctx: WasmContext): List[wa.Instr] = {
    val itablesInit = Array.fill[List[wa.Instr]](ctx.itablesLength) {
      List(wa.RefNull(watpe.HeapType.Struct))
    }
    val resolvedMethodInfos = classInfoForResolving.resolvedMethodInfos

    for {
      ancestor <- ancestors
      // Use getClassInfoOption in case the reachability analysis got rid of those interfaces
      interfaceInfo <- ctx.getClassInfoOption(ancestor)
      if interfaceInfo.isInterface
    } {
      val init = interfaceInfo.tableEntries.map { method =>
        wa.RefFunc(resolvedMethodInfos(method).tableEntryID)
      } :+ wa.StructNew(genTypeID.forITable(ancestor))
      itablesInit(interfaceInfo.itableIdx) = init
    }

    itablesInit.flatten.toList
  }
}
