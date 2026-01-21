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
  Position
}

trait GenWitInterop[G <: Global with Singleton] extends SubComponent {
  this: GenJSCode[G] =>

  import global._
  import definitions._
  import jsAddons._
  import jsDefinitions._

  // - annotated with @WitResourceMethod
  // - owner is a companion object of @WitResourceImport annotated trait
  def isWasmWitResourceStaticMethod(sym: Symbol): Boolean = {
    sym.hasAnnotation(WitResourceStaticMethodAnnotation) &&
    sym.owner.isModuleClass &&
    sym.owner.companionClass.hasAnnotation(WitResourceImportAnnotation)
  }

  def isWasmWitResourceConstructor(sym: Symbol): Boolean = {
    sym.hasAnnotation(WitResourceConstructorAnnotation) &&
    sym.owner.isModuleClass &&
    sym.owner.companionClass.hasAnnotation(WitResourceImportAnnotation)
  }

  def isWasmWitRecordClass(sym: Symbol): Boolean =
    sym.hasAnnotation(WitRecordAnnotation) && sym.isFinal

  def isWasmComponentTupleClass(sym: Symbol): Boolean =
    sym.fullName.startsWith("scala.scalajs.wit.Tuple")

  def isWasmWitFlags(sym: Symbol): Boolean =
    sym.hasAnnotation(WitFlagsAnnotation)

  def isWasmWitResourceType(tpe: Type): Boolean =
    isWasmWitResourceType(tpe.typeSymbol)

  def isWasmWitResourceType(sym: Symbol): Boolean =
    sym.hasAnnotation(WitResourceImportAnnotation)

  trait WasmComponentModelInteropPhase { this: JSCodePhase =>

    def genWitNativeMemberCall(method: Symbol, tree: Apply,
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
      js.WitFunctionApply(
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

      val annot = sym.getAnnotation(WitResourceImportAnnotation).get
      val moduleName = annot.stringArg(0).get
      val resourceName = annot.stringArg(1).get

      val flags = js.MemberFlags.empty.withNamespace(js.MemberNamespace.Public)
      val witNativeMembersBuilder = List.newBuilder[js.WitNativeMemberDef]
      for (stat <- cd.impl.body) {
        stat match {
          case dd: DefDef if dd.symbol.hasAnnotation(WitResourceMethodAnnotation) =>
            for {
              annot <- dd.symbol.getAnnotation(WitResourceMethodAnnotation)
              functionName <- annot.stringArg(0)
            } {
              witNativeMembersBuilder +=
                genWitNativeMemberDef(flags, dd, moduleName,
                    js.WitFunctionName.ResourceMethod(functionName, resourceName))
            }

          case dd: DefDef if dd.symbol.hasAnnotation(WitResourceDropAnnotation) =>
            for {
              annot <- dd.symbol.getAnnotation(WitResourceDropAnnotation)
            } {
              witNativeMembersBuilder +=
                genWitNativeMemberDef(flags, dd, moduleName,
                    js.WitFunctionName.ResourceDrop(resourceName))
            }
          case _ =>
        }
      }
      js.ClassDef(classIdent, originalNameOfClass(sym), kind, None, superClass = None,
          interfaces = Nil, None, None,
          Nil, Nil, None, Nil, Nil, witNativeMembersBuilder.result(), Nil)(
          js.OptimizerHints.empty)
    }
  }

  def genWitNativeMemberDef(flags: js.MemberFlags, tree: DefDef, moduleName: String,
      name: js.WitFunctionName): js.WitNativeMemberDef = {
    implicit val pos = tree.pos
    val sym = tree.symbol
    withNewLocalNameScope {
      val funcType = jsInterop.witFunctionTypeOf(sym)
      val baseParams = funcType.params.map(toWIT(_))
      val params = name match {
        case _:js.WitFunctionName.Function |
            _:js.WitFunctionName.ResourceConstructor |
            _:js.WitFunctionName.ResourceStaticMethod => baseParams
        case _:js.WitFunctionName.ResourceMethod |
            _:js.WitFunctionName.ResourceDrop =>
          wit.ResourceType(encodeClassName(sym.owner)) +: baseParams
      }
      val witFuncType = wit.FuncType(
        params,
        toResultWIT(funcType.resultType)
      )
      js.WitNativeMemberDef(flags, moduleName, name,
          encodeMethodSym(sym), witFuncType)
    }
  }

  def genWitResourceStaticMethodDef(tree: DefDef): Option[js.WitNativeMemberDef] = {
    implicit val pos = tree.pos
    val sym = tree.symbol

    val flags = js.MemberFlags.empty.withNamespace(js.MemberNamespace.PublicStatic)
    val funcType = jsInterop.witFunctionTypeOf(sym)

    for {
      methodAnnot <- sym.getAnnotation(WitResourceStaticMethodAnnotation)
      resourceAnnot <- sym.owner.companionClass.getAnnotation(WitResourceImportAnnotation)
      methodName <- methodAnnot.stringArg(0)
      moduleName <- resourceAnnot.stringArg(0)
      resourceName <- resourceAnnot.stringArg(1)
    } yield {
      val name = js.WitFunctionName.ResourceStaticMethod(
          func = methodName, resource = resourceName)
      withNewLocalNameScope {
        val params = funcType.params.map(p => toWIT(p))
        val ft = wit.FuncType(params, toResultWIT(funcType.resultType))
        js.WitNativeMemberDef(flags, moduleName, name, encodeMethodSym(sym), ft)
      }
    }
  }

  def genWitResourceConstructor(tree: DefDef): Option[js.WitNativeMemberDef] = {
    implicit val pos = tree.pos
    val sym = tree.symbol

    val flags = js.MemberFlags.empty.withNamespace(js.MemberNamespace.PublicStatic)
    val funcType = jsInterop.witFunctionTypeOf(sym)

    for {
      methodAnnot <- sym.getAnnotation(WitResourceConstructorAnnotation)
      resourceAnnot <- sym.owner.companionClass.getAnnotation(WitResourceImportAnnotation)
      moduleName <- resourceAnnot.stringArg(0)
      resourceName <- resourceAnnot.stringArg(1)
    } yield {
      val name = js.WitFunctionName.ResourceConstructor(resourceName)
      withNewLocalNameScope {
        val params = funcType.params.map(p => toWIT(p))
        val ft = wit.FuncType(params, toResultWIT(funcType.resultType))
        js.WitNativeMemberDef(flags, moduleName, name, encodeMethodSym(sym), ft)
      }
    }
  }

  def genWitExportDef(info: jsInterop.WitExportInfo,
      methodDef: js.MethodDef): js.WitExportDef = {
    withNewLocalNameScope {
      val signature = wit.FuncType(
        info.signature.params.map(toWIT(_)),
        toResultWIT(info.signature.resultType)
      )
      js.WitExportDef(
        info.moduleName,
        js.WitFunctionName.Function(info.name),
        methodDef,
        signature
      )(methodDef.pos)
    }
  }

  private def toWIT(tpe: Type): wit.ValType = {
    unsigned2WIT.get(tpe.typeSymbolDirect).orElse {
      toWITMaybeArray(tpe.dealiasWiden)
    }.orElse {
      primitiveIRWIT.get(toIRType(tpe.dealiasWiden))
    }.getOrElse {
      tpe.dealiasWiden.typeSymbol match {
        case tsym if isWasmComponentTupleClass(tsym) =>
          wit.TupleType(tpe.typeArgs.map(toWIT(_)))

        case tsym if tsym.hasAnnotation(WitFlagsAnnotation) =>
          // Read numFlags from annotation parameter
          val numFlags = tsym.getAnnotation(WitFlagsAnnotation)
            .flatMap(_.intArg(0))
            .getOrElse {
              throw new AssertionError(s"@WitFlags on $tsym missing numFlags parameter")
            }
          val className = encodeClassName(tsym)
          wit.FlagsType(className, numFlags)

        case tsym if isWasmWitRecordClass(tsym) =>
          // TODO: it needs to be sorted by the order of record in wit definition
          val className = encodeClassName(tsym)
          val fields: List[wit.FieldType] = tsym.info.decls.collect {
            case f if f.isField =>
              val label = encodeFieldSym(f)(f.pos).name
              val fieldType = jsInterop.witVariantValueTypeOf(f)
              val valueType = toWIT(fieldType)
              wit.FieldType(label, valueType)
          }.toList
          wit.RecordType(className, fields)

        case tsym if isWasmWitResourceType(tsym) =>
          wit.ResourceType(encodeClassName(tsym))

        case tsym if tsym.isSubClass(ComponentResultClass) && tsym.isSealed =>
          val List(ok, err) = tpe.typeArgs
          wit.ResultType(toResultWIT(ok), toResultWIT(err))

        case tsym if tsym.fullName == "java.util.Optional" =>
          val List(t) = tpe.dealiasWiden.typeArgs
          wit.OptionType(toWIT(t))

        case tsym if tsym.hasAnnotation(WitVariantAnnotation) && tsym.isSealed =>
          // Sort by declaration order, we need to know which index
          // corresponds to which type.
          // Make sure code generator declare children by index order.
          // assert(tsym.isClass)
          val cases = tsym.sealedChildren.toList.sortBy(_.pos.line) map { child =>
            // assert(child.isFinal)
            // assert(child.isClass)
            val valueType = jsInterop.witVariantValueTypeOf(child)
            val caseTyp = if (toIRType(valueType) == jstpe.VoidType) {
              None
            } else {
              Some(toWIT(valueType))
            }
            wit.CaseType(
              encodeClassName(child),
              caseTyp
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

  private lazy val ScalaJSWitUnsignedPackageModule =
    rootMirror.getPackageObject("scala.scalajs.wit.unsigned")

  private lazy val WitUnsigned_UByte =
    getTypeMember(ScalaJSWitUnsignedPackageModule, newTermName("UByte"))

  private lazy val WitUnsigned_UShort =
    getTypeMember(ScalaJSWitUnsignedPackageModule, newTermName("UShort"))

  private lazy val WitUnsigned_UInt =
    getTypeMember(ScalaJSWitUnsignedPackageModule, newTermName("UInt"))

  private lazy val WitUnsigned_ULong =
    getTypeMember(ScalaJSWitUnsignedPackageModule, newTermName("ULong"))

  private lazy val unsigned2WIT: Map[Symbol, wit.ValType] = Map(
    WitUnsigned_UByte -> wit.U8Type,
    WitUnsigned_UShort -> wit.U16Type,
    WitUnsigned_UInt -> wit.U32Type,
    WitUnsigned_ULong -> wit.U64Type
  )

  private lazy val primitiveIRWIT: Map[jstpe.Type, wit.ValType] = Map(
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
