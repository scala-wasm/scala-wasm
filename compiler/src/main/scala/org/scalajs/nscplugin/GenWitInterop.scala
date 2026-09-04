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

import org.scalajs.ir.{
  Names,
  Trees => js,
  Types => jstpe,
  WasmInterfaceTypes => wit,
  WitScope,
  ResourceOwnership,
  WitTypeDef,
  WitAliasDef,
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
  // - owner is a companion object of @WitResourceImport annotated class
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

  private val ScalaJsWitResultClass = Names.ClassName("scala.scalajs.wit.Result")
  private val ScalaJsWitOkClass = Names.ClassName("scala.scalajs.wit.Ok")
  private val ScalaJsWitErrClass = Names.ClassName("scala.scalajs.wit.Err")
  private val JuInternalWitPackage = "java.util.internal.wit"
  private val JuInternalWitResultClass = Names.ClassName(JuInternalWitPackage + ".Result")
  private val JuInternalWitOkClass = Names.ClassName(JuInternalWitPackage + ".Ok")
  private val JuInternalWitErrClass = Names.ClassName(JuInternalWitPackage + ".Err")

  private def isWasmComponentTupleClass(sym: Symbol): Boolean = {
    val n = encodeClassName(sym).nameString
    n.startsWith("scala.scalajs.wit.Tuple") ||
      n.startsWith(JuInternalWitPackage + ".Tuple")
  }

  private def isJuInternalWitResultType(sym: Symbol): Boolean =
    encodeClassName(sym) == JuInternalWitResultClass && sym.isSealed

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
      val className = encodeClassName(method.owner)
      js.WitFunctionApply(
        receiver.map(genExpr(_)),
        className,
        methodIdent,
        args.map(genExpr(_)) // genActualArgs?
      )(toIRType(tree.tpe))
    }
  }

  def genWitNativeMemberDef(flags: js.MemberFlags, tree: DefDef,
      scope: WitScope, name: js.WitFunctionName): js.WitNativeMemberDef = {
    implicit val pos = tree.pos
    val sym = tree.symbol
    withNewLocalNameScope {
      val (paramInfos, resultType) = witMethodSignatureOf(sym)
      val baseParams = witParamsOf(paramInfos)
      val params = name match {
        case _:js.WitFunctionName.Function |
            _:js.WitFunctionName.ResourceConstructor |
            _:js.WitFunctionName.ResourceStaticMethod => baseParams
        case _:js.WitFunctionName.ResourceMethod |
            _:js.WitFunctionName.ResourceDrop =>
          // WIT resource methods have an implicit self handle, make it explicit in the IR.
          // methods borrow self, while drop consumes an owned self.
          val ownership = name match {
            case _: js.WitFunctionName.ResourceDrop => ResourceOwnership.Own
            case _                                  => ResourceOwnership.Borrow
          }
          wit.ParamType("self", resourceTypeOf(sym.owner, ownership)) +: baseParams
      }
      val witFuncType = wit.FuncType(
        params,
        toResultWIT(resultType)
      )
      js.WitNativeMemberDef(flags, scope, name,
          encodeMethodSym(sym), witFuncType)
    }
  }

  def genWitResourceStaticMethodDef(tree: DefDef): Option[js.WitNativeMemberDef] = {
    implicit val pos = tree.pos
    val sym = tree.symbol

    val flags = js.MemberFlags.empty.withNamespace(js.MemberNamespace.PublicStatic)
    val (paramInfos, resultType) = witMethodSignatureOf(sym)
    for {
      methodAnnot <- sym.getAnnotation(WitResourceStaticMethodAnnotation)
      resourceAnnot <- sym.owner.companionClass.getAnnotation(WitResourceImportAnnotation)
      methodName <- methodAnnot.stringArg(0)
      scope <- jsInterop.witScopeArg(resourceAnnot, 0)
      resourceName <- resourceAnnot.stringArg(1)
    } yield {
      val name = js.WitFunctionName.ResourceStaticMethod(
          func = methodName, resource = resourceName)
      withNewLocalNameScope {
        val ft = wit.FuncType(witParamsOf(paramInfos), toResultWIT(resultType))
        js.WitNativeMemberDef(flags, scope, name, encodeMethodSym(sym), ft)
      }
    }
  }

  def genWitResourceConstructor(tree: DefDef): Option[js.WitNativeMemberDef] = {
    implicit val pos = tree.pos
    val sym = tree.symbol

    val flags = js.MemberFlags.empty.withNamespace(js.MemberNamespace.PublicStatic)
    val (paramInfos, resultType) = witMethodSignatureOf(sym)

    for {
      methodAnnot <- sym.getAnnotation(WitResourceConstructorAnnotation)
      resourceAnnot <- sym.owner.companionClass.getAnnotation(WitResourceImportAnnotation)
      scope <- jsInterop.witScopeArg(resourceAnnot, 0)
      resourceName <- resourceAnnot.stringArg(1)
    } yield {
      val name = js.WitFunctionName.ResourceConstructor(resourceName)
      withNewLocalNameScope {
        val ft = wit.FuncType(witParamsOf(paramInfos), toResultWIT(resultType))
        js.WitNativeMemberDef(flags, scope, name, encodeMethodSym(sym), ft)
      }
    }
  }

  def genWitExportDef(info: jsInterop.WitExportInfo, sym: Symbol,
      methodDef: js.MethodDef): js.WitExportDef = {
    withNewLocalNameScope {
      val (paramInfos, resultType) = witMethodSignatureOf(sym)
      val signature = wit.FuncType(
        witParamsOf(paramInfos),
        toResultWIT(resultType)
      )
      js.WitExportDef(
        info.scope,
        js.WitFunctionName.Function(info.name),
        methodDef,
        signature
      )(methodDef.pos)
    }
  }

  private final class WitParamInfo(val witName: String, val tpe: Type)

  private def witMethodSignatureOf(sym: Symbol): (List[WitParamInfo], Type) = {
    exitingPhase(currentRun.typerPhase) {
      val methodType = sym.tpe
      val params = {
        if (methodType.paramss.isEmpty) Nil
        else {
          methodType.paramss.head.map { p =>
            new WitParamInfo(witNameOf(p), p.tpe)
          }
        }
      }
      (params, methodType.resultType)
    }
  }

  private def witParamsOf(params: List[WitParamInfo]): List[wit.ParamType] = {
    params.map { param =>
      wit.ParamType(param.witName, toWIT(param.tpe))
    }
  }

  def genWitTypeDef(sym: Symbol): Option[WitTypeDef] = {
    val tsym = exitingPhase(currentRun.typerPhase)(sym)
    val className = encodeClassName(tsym)
    if (tsym.hasAnnotation(WitFlagsAnnotation)) {
      val annot = tsym.getAnnotation(WitFlagsAnnotation).get
      val ((scope, name), names) = witFlagsInfo(tsym, annot)
      Some(WitTypeDef.Flags(className, scope, name, names))
    } else if (isWasmWitRecordClass(tsym)) {
      val fields = exitingPhase(currentRun.typerPhase) {
        tsym.primaryConstructor.paramss.flatten.map { param =>
          val scalaName = param.name.dropLocal.toString()
          val witName = witNameOf(param)
          (scalaName, witName, param.tpe)
        }
      }
      val irFields = fields.map { case (scalaName, witName, fieldType) =>
        val label = Names.FieldName(className, Names.SimpleFieldName(scalaName))
        wit.FieldType(label, witName, toWIT(fieldType))
      }
      val (scope, name) = witIdOf(tsym, WitRecordAnnotation)
      Some(WitTypeDef.Record(className, scope, name, irFields))
    } else if (isWasmWitResourceType(tsym)) {
      Some(WitTypeDef.Resource(resourceRefOf(tsym)))
    } else if (tsym.hasAnnotation(WitEnumAnnotation) && tsym.isSealed) {
      val cases = witEnumCasesOf(tsym)
      val (scope, name) = witIdOf(tsym, WitEnumAnnotation)
      Some(WitTypeDef.Enum(className, scope, name, cases))
    } else if (tsym.hasAnnotation(WitVariantAnnotation) && tsym.isSealed) {
      val cases = witVariantCasesOf(tsym)
      val (scope, name) = witIdOf(tsym, WitVariantAnnotation)
      Some(WitTypeDef.Variant(className, scope, name, cases))
    } else if (className == ScalaJsWitResultClass || className == JuInternalWitResultClass) {
      val (okClass, errClass) = {
        if (className == ScalaJsWitResultClass)
          (ScalaJsWitOkClass, ScalaJsWitErrClass)
        else
          (JuInternalWitOkClass, JuInternalWitErrClass)
      }
      Some(WitTypeDef.Result(
          className,
          okClass,
          errClass,
          Names.SimpleFieldName("value")))
    } else if (isWasmComponentTupleClass(tsym)) {
      val fields = tsym.primaryConstructor.paramss.flatten.map { p =>
        Names.SimpleFieldName(p.name.dropLocal.toString())
      }
      Some(WitTypeDef.Tuple(className, fields))
    } else {
      None
    }
  }

  /** Collect `@WitAlias` type aliases owned by this class/module. */
  def genWitAliasDefs(owner: Symbol): List[WitAliasDef] = {
    val aliases = exitingPhase(currentRun.typerPhase) {
      owner.info.decls.toList.collect {
        case tsym if tsym.isAliasType && tsym.hasAnnotation(WitAliasAnnotation) =>
          val (scope, name) = witIdOf(tsym, WitAliasAnnotation)
          (scope, name, tsym.info.dealias)
      }
    }
    aliases.map {
      case (scope, name, targetTpe) =>
        WitAliasDef(scope, name, toWIT(targetTpe))
    }
  }

  private def witVariantValueTypeOf(sym: Symbol): Type = {
    exitingPhase(currentRun.typerPhase) {
      if (sym.isModuleClass) {
        UnitTpe
      } else if (sym.isClass && sym.isFinal && !sym.isTrait) {
        sym.primaryConstructor.paramss.flatten match {
          case Nil =>
            UnitTpe
          case param :: Nil =>
            param.tpe
          case _ =>
            throw new AssertionError(s"Invalid WIT variant case shape for $sym")
        }
      } else {
        throw new AssertionError(s"Invalid WIT variant case symbol $sym")
      }
    }
  }

  // Read `Borrow[T]` as `borrow<resource>`
  private def borrowToWIT(tpe: Type): Option[wit.ValType] = {
    if (tpe.typeSymbolDirect != WitBorrowAlias) {
      None
    } else {
      val arg = tpe.typeArgs.headOption.getOrElse {
        throw new AssertionError(s"Borrow without a type argument in $tpe")
      }
      val resourceTpe = { // dealias @WitAlias type alias
        val argSym = exitingPhase(currentRun.typerPhase)(arg.typeSymbolDirect)
        if (argSym.hasAnnotation(WitAliasAnnotation))
          exitingPhase(currentRun.typerPhase)(argSym.info.dealias)
        else
          arg
      }
      toWIT(resourceTpe) match {
        case res: wit.ResourceType =>
          Some(res.copy(ownership = ResourceOwnership.Borrow))
        case other =>
          throw new AssertionError(
              s"Borrow[_] is not applicable to $other (in $tpe)")
      }
    }
  }

  private def toWIT(tpe: Type): wit.ValType = {
    val directTpe = exitingPhase(currentRun.typerPhase)(tpe)
    val directSym = exitingPhase(currentRun.typerPhase)(directTpe.typeSymbolDirect)
    if (directSym.hasAnnotation(WitAliasAnnotation)) {
      val (scope, name) = witIdOf(directSym, WitAliasAnnotation)
      return wit.AliasTypeRef(scope, name, encodeClassName(directSym.owner))
    }

    val dealiasedTpe = exitingPhase(currentRun.typerPhase)(directTpe.dealias)

    unsigned2WIT.get(directTpe.typeSymbolDirect).orElse {
      borrowToWIT(directTpe)
    }.orElse {
      toWITMaybeArray(dealiasedTpe)
    }.orElse {
      primitiveIRWIT.get(toIRType(dealiasedTpe))
    }.getOrElse {
      dealiasedTpe.typeSymbol match {
        case tsym if isWasmComponentTupleClass(tsym) =>
          wit.TupleType(dealiasedTpe.baseType(tsym).typeArgs.map(toWIT(_)), encodeClassName(tsym))

        case tsym if tsym.hasAnnotation(WitFlagsAnnotation) =>
          val className = encodeClassName(tsym)
          wit.FlagsTypeRef(className)

        case tsym if isWasmWitRecordClass(tsym) =>
          wit.RecordTypeRef(encodeClassName(tsym))

        case tsym if isWasmWitResourceType(tsym) =>
          // A bare WIT resource type is shorthand for `own<resource>`.
          resourceTypeOf(tsym, ResourceOwnership.Own)

        case tsym if tsym.isSubClass(ComponentResultClass) && tsym.isSealed =>
          val List(ok, err) = dealiasedTpe.baseType(ComponentResultClass).typeArgs
          wit.ResultType(toResultWIT(ok), toResultWIT(err), encodeClassName(ComponentResultClass))

        case tsym if isJuInternalWitResultType(tsym) =>
          val List(ok, err) = dealiasedTpe.baseType(tsym).typeArgs
          wit.ResultType(toResultWIT(ok), toResultWIT(err), JuInternalWitResultClass)

        case tsym if tsym.fullName == "java.util.Optional" =>
          val List(t) = dealiasedTpe.baseType(tsym).typeArgs
          wit.OptionType(toWIT(t))

        case tsym if tsym.hasAnnotation(WitEnumAnnotation) && tsym.isSealed =>
          val _ = witEnumCasesOf(tsym) // validates that there are no payload cases
          wit.EnumTypeRef(encodeClassName(tsym))

        case tsym if tsym.hasAnnotation(WitVariantAnnotation) && tsym.isSealed =>
          wit.VariantTypeRef(encodeClassName(tsym))
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

  private def resourceTypeOf(sym: Symbol, ownership: ResourceOwnership): wit.ResourceType =
    wit.ResourceType(encodeClassName(sym), ownership)

  private def resourceRefOf(sym: Symbol): wit.ResourceRef = {
    val (scope, name) = sym.getAnnotation(WitResourceImportAnnotation).flatMap { annotation =>
      for {
        scope <- jsInterop.witScopeArg(annotation, 0)
        name <- annotation.stringArg(1)
      } yield (scope, name)
    }.getOrElse {
      throw new AssertionError(
          s"@WitResourceImport on $sym requires a literal WitScope and a literal name")
    }
    wit.ResourceRef(encodeClassName(sym), scope, name)
  }

  private def witIdOf(tsym: Symbol, annotation: Symbol): (WitScope, String) = {
    val annotationName = "@" + annotation.name
    tsym.getAnnotation(annotation).flatMap { annot =>
      for {
        scope <- jsInterop.witScopeArg(annot, 0)
        name <- annot.stringArg(1)
      } yield (scope, name)
    }.getOrElse {
      throw new AssertionError(
          s"$annotationName on $tsym requires a literal WitScope and a literal name")
    }
  }

  private def witEnumCasesOf(tsym: Symbol): List[wit.CaseType] = {
    // Sort by declaration order, we need to know which index corresponds to which type.
    tsym.sealedChildren.toList.sortBy(_.pos.line) map { child =>
      if (!child.isModuleClass) {
        throw new AssertionError(
            s"@WitEnum on $tsym is not valid: it has payload cases")
      }
      wit.CaseType(
        encodeClassName(child),
        witNameOf(child),
        None
      )
    }
  }

  private def witVariantCasesOf(tsym: Symbol): List[wit.CaseType] = {
    // Sort by declaration order, we need to know which index corresponds to which type.
    tsym.sealedChildren.toList.sortBy(_.pos.line) map { child =>
      val valueType = witVariantValueTypeOf(child)
      val caseTyp =
        if (toIRType(valueType) == jstpe.VoidType) None
        else Some(toWIT(valueType))
      wit.CaseType(
        encodeClassName(child),
        witNameOf(child),
        caseTyp
      )
    }
  }

  private def witNameOf(sym: Symbol): String = {
    sym.getAnnotation(WitNameAnnotation).flatMap(_.stringArg(0)).getOrElse {
      throw new AssertionError(s"missing literal @WitName on $sym")
    }
  }

  private def witFlagsInfo(tsym: Symbol,
      annot: AnnotationInfo): ((WitScope, String), List[String]) = {
    val id = {
      for {
        scope <- jsInterop.witScopeArg(annot, 0)
        name <- annot.stringArg(1)
      } yield (scope, name)
    }.getOrElse {
      throw new AssertionError(
          s"@WitFlags on $tsym requires a literal WitScope and a literal name")
    }
    val names = jsInterop.literalStringArrayArg(annot, 2).getOrElse {
      throw new AssertionError(
          s"@WitFlags on $tsym requires a literal Array of literal strings")
    }
    (id, names)
  }

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
    jstpe.ClassType(Names.ClassName("java.lang.String"), true, false) ->
    wit.StringType
  )

}
