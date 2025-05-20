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

package org.scalajs.nscplugin

import scala.tools.nsc._

import scala.collection.mutable

import org.scalajs.ir.{
  Names,
  Trees => js,
  Types => jstpe,
  WasmInterfaceTypes => wit,
  ClassKind,
  Position,
}

trait GenWasmComponentModelInterop[G <: Global with Singleton] extends SubComponent {
  this: GenJSCode[G] =>

  import global._
  import definitions._
  import jsAddons._
  import jsDefinitions._

  // - annotated with @ComponentResourceMethod
  // - owner is a companion object of @ComponentResourceImport annotated trait
  def isWasmComponentResourceStaticMethod(sym: Symbol): Boolean =
    sym.hasAnnotation(ComponentResourceStaticMethodAnnotation) &&
        sym.owner.isModuleClass &&
        sym.owner.companionClass.hasAnnotation(ComponentResourceImportAnnotation)

  def isWasmComponentResourceConstructor(sym: Symbol): Boolean =
    sym.hasAnnotation(ComponentResourceConstructorAnnotation) &&
        sym.owner.isModuleClass &&
        sym.owner.companionClass.hasAnnotation(ComponentResourceImportAnnotation)

  def isWasmComponentRecordClass(sym: Symbol): Boolean =
    sym.hasAnnotation(ComponentRecordAnnotation) && sym.isFinal

  def isWasmComponentFlags(sym: Symbol): Boolean =
    sym.hasAnnotation(ComponentFlagsAnnotation)

  def isWasmComponentResourceType(tpe: Type): Boolean =
    isWasmComponentResourceType(tpe.typeSymbol)

  def isWasmComponentResourceType(sym: Symbol): Boolean =
    sym.hasAnnotation(ComponentResourceImportAnnotation)

  trait WasmComponentModelInteropPhase { this: JSCodePhase =>

    def genComponentNativeMemberCall(method: Symbol, tree: Apply,
        receiver: Option[Tree], isStat: Boolean): js.Tree = {
      val sym = tree.symbol
      val Apply(Select(qual, _), args) = tree
      implicit val pos = tree.pos
      val methodIdent = encodeMethodSym(method)

      // Not using encodeClassName(method.owner)
      // The method.owner of `close` methods will be `component.Resource` instead of the specific resource class that extends the Resource trait.
      // `component.Resource` defines a `final def close`, preventing users from overriding the close implementation.
      // However, the actual `close` methods to be called are automatically generated for all resource classes.
      // val className = encodeClassName(qual.symbol.tpe.typeSymbol)

      val className = encodeClassName(method.owner)
      js.ComponentFunctionApply(
        receiver.map(genExpr(_)),
        className,
        methodIdent,
        args.map(genExpr(_)) // genActualArgs?
      )(toIRType(tree.tpe))
    }

    def genWasmComponentResourceClassData(cd: ClassDef): js.ClassDef = {
      val sym = cd.symbol
      implicit val pos = sym.pos

      val classIdent = encodeClassNameIdent(sym)
      val kind = ClassKind.NativeWasmComponentResourceClass

      val annot = sym.getAnnotation(ComponentResourceImportAnnotation).get
      val resourceName = annot.stringArg(0).get
      val moduleName = annot.stringArg(1).get

      val flags = js.MemberFlags.empty.withNamespace(js.MemberNamespace.Public)
      val componentNativeMembersBuilder = List.newBuilder[js.ComponentNativeMemberDef]
      for (stat <- cd.impl.body) {
        stat match {
          case dd: DefDef if dd.symbol.hasAnnotation(ComponentResourceMethodAnnotation) =>
            for {
              annot <- dd.symbol.getAnnotation(ComponentResourceMethodAnnotation)
              functionName <- annot.stringArg(0)
            } {
              componentNativeMembersBuilder +=
                genComponentNativeMemberDef(flags, dd, moduleName,
                    js.WasmComponentFunctionName.ResourceMethod(functionName, resourceName))
            }

          case dd: DefDef if dd.symbol.hasAnnotation(ComponentResourceDropAnnotation) =>
            for {
              annot <- dd.symbol.getAnnotation(ComponentResourceDropAnnotation)
            } {
              componentNativeMembersBuilder +=
                genComponentNativeMemberDef(flags, dd, moduleName,
                    js.WasmComponentFunctionName.ResourceDrop(resourceName))
            }
          case _ =>
        }
      }
      js.ClassDef(classIdent, originalNameOfClass(sym), kind, None, superClass= None,
          interfaces = Nil, None, None,
          Nil, Nil, None, Nil, Nil, componentNativeMembersBuilder.result(), Nil)(
          js.OptimizerHints.empty)
    }
  }

  def genComponentNativeMemberDef(flags: js.MemberFlags, tree: DefDef, moduleName: String,
      name: js.WasmComponentFunctionName): js.ComponentNativeMemberDef = {
    implicit val pos = tree.pos
    val sym = tree.symbol
    withNewLocalNameScope {
      val funcType = jsInterop.componentFunctionTypeOf(sym)
      val baseParams = funcType.params.map(toWIT(_))
      val params = name match {
        case _:js.WasmComponentFunctionName.Function |
             _:js.WasmComponentFunctionName.ResourceConstructor |
             _:js.WasmComponentFunctionName.ResourceStaticMethod => baseParams
        case _:js.WasmComponentFunctionName.ResourceMethod |
             _:js.WasmComponentFunctionName.ResourceDrop =>
          wit.ResourceType(encodeClassName(sym.owner)) +: baseParams
      }
      val witFuncType = wit.FuncType(
        params,
        toResultWIT(funcType.resultType)
      )
      js.ComponentNativeMemberDef(flags, moduleName, name,
          encodeMethodSym(sym), witFuncType)
    }
  }

  def genComponentResourceStaticMethodDef(tree: DefDef): Option[js.ComponentNativeMemberDef] = {
    implicit val pos = tree.pos
    val sym = tree.symbol

    val flags = js.MemberFlags.empty.withNamespace(js.MemberNamespace.PublicStatic)
    val funcType = jsInterop.componentFunctionTypeOf(sym)

    for {
      methodAnnot <- sym.getAnnotation(ComponentResourceStaticMethodAnnotation)
      resourceAnnot <- sym.owner.companionClass.getAnnotation(ComponentResourceImportAnnotation)
      methodName <- methodAnnot.stringArg(0)
      resourceName <- resourceAnnot.stringArg(0)
      moduleName <- resourceAnnot.stringArg(1)
    } yield {
      println(methodName)
      val name = js.WasmComponentFunctionName.ResourceStaticMethod(
          func = methodName, resource = resourceName)
      withNewLocalNameScope {
        val params = funcType.params.map { p => toWIT(p) }
        val ft = wit.FuncType(params, toResultWIT(funcType.resultType))
        js.ComponentNativeMemberDef(flags, moduleName, name, encodeMethodSym(sym), ft)
      }
    }
  }

  def genComponentResourceConstructor(tree: DefDef): Option[js.ComponentNativeMemberDef] = {
    implicit val pos = tree.pos
    val sym = tree.symbol

    val flags = js.MemberFlags.empty.withNamespace(js.MemberNamespace.PublicStatic)
    val funcType = jsInterop.componentFunctionTypeOf(sym)

    for {
      methodAnnot <- sym.getAnnotation(ComponentResourceConstructorAnnotation)
      resourceAnnot <- sym.owner.companionClass.getAnnotation(ComponentResourceImportAnnotation)
      resourceName <- resourceAnnot.stringArg(0)
      moduleName <- resourceAnnot.stringArg(1)
    } yield {
      val name = js.WasmComponentFunctionName.ResourceConstructor(resourceName)
      withNewLocalNameScope {
        val params = funcType.params.map { p => toWIT(p) }
        val ft = wit.FuncType(params, toResultWIT(funcType.resultType))
        js.ComponentNativeMemberDef(flags, moduleName, name, encodeMethodSym(sym), ft)
      }
    }
  }


  def genWasmComponentExportDef(info: jsInterop.WasmComponentExportInfo,
      methodDef: js.MethodDef): js.WasmComponentExportDef = {
    withNewLocalNameScope {
      val signature = wit.FuncType(
        info.signature.params.map(toWIT(_)),
        toResultWIT(info.signature.resultType)
      )
      js.WasmComponentExportDef(
        info.moduleName,
        js.WasmComponentFunctionName.Function(info.name),
        methodDef,
        signature
      )(methodDef.pos)
    }
  }

  private def toFlagTypeOpt(tpe: Type): Option[wit.FlagsType] = {
    for {
      ann <- tpe.typeSymbolDirect.getAnnotation(ComponentFlagsAnnotation)
      numFlags <- ann.intArg(0)
      if toIRType(tpe) == jstpe.IntType
    } yield wit.FlagsType(numFlags)
  }

  private def toWIT(tpe: Type): wit.ValType = {
    toFlagTypeOpt(tpe).orElse {
      unsigned2WIT.get(tpe.typeSymbolDirect)
    }.orElse {
      toWITMaybeArray(tpe.dealiasWiden)
    }.orElse {
      primitiveIRWIT.get(toIRType(tpe.dealiasWiden))
    }.getOrElse {
      tpe.dealiasWiden.typeSymbol match {
        case tsym if tsym.fullName.startsWith("scala.Tuple") =>
          wit.TupleType(tpe.typeArgs.map(toWIT(_)))

        case tsym if isWasmComponentRecordClass(tsym) =>
          // TODO: it needs to be sorted by the order of record in wit definition
          val className = encodeClassName(tsym)
          val fields: List[wit.FieldType] = tsym.info.decls.collect {
            case f if f.isField =>
              val label = encodeFieldSym(f)(f.pos).name
              val fieldType = jsInterop.componentVariantValueTypeOf(f)
              val valueType = toWIT(fieldType)
              wit.FieldType(label, valueType)
          }.toList
          wit.RecordType(className, fields)

        case tsym if isWasmComponentResourceType(tsym) =>
          wit.ResourceType(encodeClassName(tsym))

        case tsym if tsym.isSubClass(ComponentResultClass) && tsym.isSealed =>
          val List(ok, err) = tpe.typeArgs
          wit.ResultType(toWIT(ok), toWIT(err))

        case tsym if tsym.fullName == "java.util.Optional" =>
          val List(t) = tpe.dealiasWiden.typeArgs
          wit.OptionType(toWIT(t))

        case tsym if tsym.isSubClass(ComponentVariantClass) && tsym.isSealed =>
          // Sort by declaration order, we need to know which index
          // corresponds to which type.
          // Make sure code generator declare children by index order.
          // assert(tsym.isClass)
          val cases = tsym.sealedChildren.toList.sortBy(_.pos.line) map { child =>
            // assert(child.isFinal)
            // assert(child.isClass)
            val valueType = jsInterop.componentVariantValueTypeOf(child)
            wit.CaseType(
              encodeClassName(child),
              toWIT(valueType)
            )
          }
          wit.VariantType(
            encodeClassName(tsym),
            cases
          )
        case _ => throw new AssertionError(s"invalid tpe: $tpe")
      }
    }
  }

  private def toResultWIT(tpe: Type): Option[wit.ValType] = {
    if (toIRType(tpe) == jstpe.VoidType) None
    else Some(toWIT(tpe))
  }

  private def toWITMaybeArray(tpe: Type): Option[wit.ValType] = {
    tpe match {
      case TypeRef(_, ArrayClass, targs) =>
        Some(wit.ListType(toWIT(targs.head), None))
      case _ => None
    }
  }

  private lazy val ScalaJSComponentUnsignedPackageModule = rootMirror.getPackageObject("scala.scalajs.component.unsigned")
    private lazy val ComponentUnsigned_UByte = getTypeMember(ScalaJSComponentUnsignedPackageModule, newTermName("UByte"))
    private lazy val ComponentUnsigned_UShort = getTypeMember(ScalaJSComponentUnsignedPackageModule, newTermName("UShort"))
    private lazy val ComponentUnsigned_UInt = getTypeMember(ScalaJSComponentUnsignedPackageModule, newTermName("UInt"))
    private lazy val ComponentUnsigned_ULong = getTypeMember(ScalaJSComponentUnsignedPackageModule, newTermName("ULong"))

  private lazy val unsigned2WIT: Map[Symbol, wit.ValType] = Map(
    ComponentUnsigned_UByte  -> wit.U8Type,
    ComponentUnsigned_UShort -> wit.U16Type,
    ComponentUnsigned_UInt   -> wit.U32Type,
    ComponentUnsigned_ULong  -> wit.U64Type
  )

  private lazy val primitiveIRWIT: Map[jstpe.Type, wit.ValType] = Map(
    jstpe.VoidType -> wit.VoidType,
    jstpe.BooleanType -> wit.BoolType,
    jstpe.ByteType -> wit.S8Type,
    jstpe.ShortType -> wit.S16Type,
    jstpe.IntType -> wit.S32Type,
    jstpe.LongType -> wit.S64Type,
    jstpe.FloatType -> wit.F32Type,
    jstpe.DoubleType -> wit.F64Type,
    jstpe.CharType -> wit.CharType,
    jstpe.StringType -> wit.StringType,
    jstpe.ClassType(Names.ClassName("java.lang.String"), true) ->
        wit.StringType
  )

}