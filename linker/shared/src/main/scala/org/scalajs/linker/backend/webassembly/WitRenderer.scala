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

package org.scalajs.linker.backend.webassembly

import scala.collection.mutable

import org.scalajs.ir.Trees.{WitExportDef, WitFunctionName}
import org.scalajs.ir.{ResourceOwnership, WitScope, WitTypeDef}
import org.scalajs.ir.WasmInterfaceTypes._
import org.scalajs.linker.interface.WasmComponentModuleInitializerExport
import org.scalajs.linker.standard.ModuleSet

private[backend] object WitRenderer {
  val WorldName = "synthetic"
  private val RootPackage = "scala-wasm:generated"

  private val WitKeywords = Set(
      "use", "import", "export", "include", "package", "world", "interface",
      "type", "resource", "func", "variant", "record", "flags", "enum",
      "option", "result", "list", "tuple", "future", "stream", "borrow", "own",
      "static", "constructor", "string", "bool", "u8", "u16", "u32", "u64",
      "s8", "s16", "s32", "s64", "f32", "f64", "char", "default", "implements",
      "where", "with", "as", "from", "async", "yield"
  )

  private def escapeIdent(name: String): String =
    if (WitKeywords(name)) "%" + name else name

  private sealed abstract class Direction(val keyword: String)

  private object Direction {
    case object Import extends Direction("import")
    case object Export extends Direction("export")
  }

  /** A reachable WIT `func` either import or export. */
  private final case class Func(
      direction: Direction,
      scope: WitScope,
      name: WitFunctionName,
      signature: FuncType
  )

  private final case class Declarations(
      endpointsByScope: Map[WitScope, List[Func]],
      typesByScope: Map[WitScope, List[WitTypeDef]]
  ) {
    def endpoints(scope: WitScope): List[Func] =
      endpointsByScope.getOrElse(scope, Nil)

    def types(scope: WitScope): List[WitTypeDef] =
      typesByScope.getOrElse(scope, Nil)

    def scopes: List[WitScope] =
      (endpointsByScope.keySet ++ typesByScope.keySet).toList
  }

  private final case class TypeImports(
      uses: List[String],
      visibleTypeNames: Map[(WitScope, String), String]
  )

  def render(module: ModuleSet.Module,
      moduleInitializerExport: Option[WasmComponentModuleInitializerExport]): String = {
    val declarations = collectDeclarations(module, moduleInitializerExport)

    val interfaceScopes = declarations.scopes.collect {
      case iface: WitScope.Interface => iface
    }
    val inlineScopes = declarations.scopes.collect {
      case inline: WitScope.Inline => inline
    }

    val packageBlocks = interfaceScopes
      .groupBy(iface => (iface.namespace, iface.packageName, iface.version))
      .toList
      .map { case (packageKey, ifaces) =>
        val interfaces = ifaces.map { iface =>
          renderNamedInterface(iface, declarations)
        }
        val packageName = packageKey._1 + ":" + packageKey._2 + packageKey._3.fold("")("@" + _)
        s"package $packageName {${interfaces.mkString(" ")}}"
      }

    val rootContent =
      renderRoot(interfaceScopes, inlineScopes, declarations)

    (rootContent :: packageBlocks).mkString("\n")
  }

  private def collectDeclarations(module: ModuleSet.Module,
      moduleInitializerExport: Option[WasmComponentModuleInitializerExport]): Declarations = {
    val imports = for {
      classDef <- module.classDefs
      member <- classDef.witNativeMembers
    } yield {
      Func(Direction.Import, member.scope, member.name, member.signature)
    }

    val exports = module.topLevelExports.flatMap { linked =>
      linked.tree match {
        case witExport: WitExportDef =>
          Some(Func(Direction.Export, witExport.scope, witExport.name,
              witExport.signature))
        case _ => None
      }
    }

    val endpoints = imports ::: exports ::: moduleInitializerExport.map {
      initializer =>
        val resultType = initializer.resultType match {
          case WasmComponentModuleInitializerExport.ResultType.Unit =>
            None
          case WasmComponentModuleInitializerExport.ResultType.ResultUnitUnit =>
            Some(ResultType(None, None))
        }
        Func(Direction.Export, initializer.scope,
            WitFunctionName.Function(initializer.functionName),
            FuncType(Nil, resultType))
    }.toList
    val typeDefs = mutable.Map.empty[(WitScope, String), WitTypeDef]
    // dedup typeDefs
    module.classDefs.flatMap(_.witTypeDef).foreach { typeDef =>
      typeDefs.getOrElseUpdate((typeDef.scope, typeDef.name), typeDef)
    }

    // dedup functions
    val seen = mutable.Set.empty[(Direction, WitScope, WitFunctionName)]
    val uniqEndpoints = endpoints.filter { endpoint =>
      seen.add((endpoint.direction, endpoint.scope, endpoint.name))
    }

    Declarations(
        uniqEndpoints.groupBy(_.scope),
        typeDefs.valuesIterator.toList.groupBy(_.scope))
  }

  private def renderRoot(interfaceScopes: List[WitScope.Interface],
      inlineScopes: List[WitScope.Inline],
      declarations: Declarations): String = {
    val items = List.newBuilder[String]

    items += renderScopeContent(WitScope.Root,
        declarations.endpoints(WitScope.Root),
        declarations.types(WitScope.Root),
        renderWorldFunction)

    for {
      iface <- interfaceScopes
      (direction, _) <- declarations.endpoints(iface).groupBy(_.direction)
    } {
      items += s"${direction.keyword} ${iface.witId};"
    }

    for {
      inline <- inlineScopes
      endpointsByDirection = declarations.endpoints(inline).groupBy(_.direction)
      (direction, endpoints) <- endpointsByDirection
    } {
      val content = {
        renderScopeContent(inline, endpoints, declarations.types(inline),
            renderOrdinaryFunction)
      }
      items +=
        s"${direction.keyword} ${inline.name}: interface {$content}"
    }

    s"package $RootPackage; world $WorldName {${items.result().mkString(" ")}}"
  }

  private def renderNamedInterface(iface: WitScope.Interface, decls: Declarations): String = {
    // Importing and exporting the same interface still declares its functions
    // only once in the interface body.
    val seen = mutable.Set.empty[WitFunctionName]
    val endpoints = decls.endpoints(iface).filter(
        endpoint =>
          seen.add(endpoint.name))
    val content = {
      renderScopeContent(iface, endpoints, decls.types(iface),
          renderOrdinaryFunction)
    }
    s"interface ${iface.name} {$content}"
  }

  private def renderScopeContent(scope: WitScope,
      endpoints: List[Func],
      namedTypes: List[WitTypeDef],
      renderOrdinaryFunction: (Func, TypeImports) => String): String = {

    // types referenced by function
    def endpointTypes(endpoint: Func): List[ValType] =
      endpoint.signature.params.map(_.tpe) ::: endpoint.signature.resultType.toList

    // types referenced by the body of named types
    def namedTypeBodyTypes(namedType: WitTypeDef): List[ValType] = {
      namedType match {
        case WitTypeDef.Record(_, _, _, fields) =>
          fields.map(_.tpe)
        case WitTypeDef.Variant(_, _, _, cases) =>
          cases.flatMap(_.tpe)
        case _:WitTypeDef.Enum | _:WitTypeDef.Flags | _:WitTypeDef.Resource =>
          Nil
      }
    }

    val referencedTypes =
      endpoints.flatMap(endpointTypes) ::: namedTypes.flatMap(namedTypeBodyTypes)
    implicit val typeImports: TypeImports =
      resolveTypeImports(referencedTypes, namedTypes, scope)

    val resourceEndpoints = endpoints.collect {
      case endpoint @ Func(_, _, name, _)
          if !name.isInstanceOf[WitFunctionName.Function] =>
        endpoint
    }.groupBy(resourceName)

    val renderedTypes = namedTypes.map { namedType =>
      renderNamedType(namedType,
          resourceEndpoints.getOrElse(namedType.name, Nil))
    }

    val functions = endpoints.collect {
      case endpoint @ Func(_, _, _: WitFunctionName.Function, _) =>
        endpoint
    }

    val renderedFunctions =
      functions.map(renderOrdinaryFunction(_, typeImports))

    (typeImports.uses ::: renderedTypes ::: renderedFunctions).mkString(" ")
  }

  private def renderNamedType(namedType: WitTypeDef,
      resourceEndpoints: List[Func])(
      implicit typeImports: TypeImports): String = {
    val name = namedType.name
    namedType match {
      case WitTypeDef.Record(_, _, _, fields) =>
        s"record $name {${fields.map { field =>
            s"${escapeIdent(field.name)}: ${renderType(field.tpe)}"
          }.mkString(", ")}}"

      case WitTypeDef.Variant(_, _, _, cases) =>
        s"variant $name {${cases.map { caze =>
            val payload =
              caze.tpe.fold("")(tpe => s"(${renderType(tpe)})")
            s"${escapeIdent(caze.name)}$payload"
          }.mkString(", ")}}"

      case WitTypeDef.Enum(_, _, _, cases) =>
        s"enum $name {${cases.map(c => escapeIdent(c.name)).mkString(", ")}}"

      case WitTypeDef.Flags(_, _, _, names) =>
        s"flags $name {${names.map(escapeIdent).mkString(", ")}}"

      case _: WitTypeDef.Resource =>
        // resource.drop dosen't appear on WIT
        val members = resourceEndpoints
          .filterNot(_.name.isInstanceOf[WitFunctionName.ResourceDrop])
          .map(renderResourceMember)
        if (members.isEmpty) {
          s"resource $name;"
        } else {
          renderBlock(s"resource $name", members)
        }
    }
  }

  private def renderBlock(header: String, members: List[String]): String =
    s"$header {${members.mkString(" ")}}"

  private def renderWorldFunction(endpoint: Func, typeImports: TypeImports): String = {
    implicit val implicitTypeImports: TypeImports = typeImports
    s"${endpoint.direction.keyword} ${renderFunction(endpoint)}"
  }

  private def renderOrdinaryFunction(endpoint: Func, typeImports: TypeImports): String = {
    implicit val implicitTypeImports: TypeImports = typeImports
    renderFunction(endpoint)
  }

  private def renderFunction(endpoint: Func)(
      implicit typeImports: TypeImports): String = {
    val WitFunctionName.Function(func) = endpoint.name: @unchecked
    s"${escapeIdent(func)}: ${renderFuncType(endpoint.signature)};"
  }

  private def renderResourceMember(endpoint: Func)(
      implicit typeImports: TypeImports): String = {
    endpoint.name match {
      case WitFunctionName.ResourceConstructor(_) =>
        s"constructor${renderParams(endpoint.signature.params)};"

      case WitFunctionName.ResourceMethod(func, _) =>
        val signature = endpoint.signature
        s"${escapeIdent(func)}: ${renderFuncType(signature.copy(params = signature.params.drop(1)))};"

      case WitFunctionName.ResourceStaticMethod(func, _) =>
        s"${escapeIdent(func)}: static ${renderFuncType(endpoint.signature)};"

      case _: WitFunctionName.ResourceDrop =>
        throw new AssertionError("resource drops are implicit in WIT")

      case _: WitFunctionName.Function =>
        throw new AssertionError("ordinary function rendered as a resource member")
    }
  }

  private def renderFuncType(signature: FuncType)(
      implicit typeImports: TypeImports): String = {
    val result = signature.resultType.fold("")(
        tpe =>
          s" -> ${renderType(tpe)}")
    s"func${renderParams(signature.params)}$result"
  }

  private def renderParams(params: List[ParamType])(
      implicit typeImports: TypeImports): String = {
    params.map(
        param =>
          s"${escapeIdent(param.name)}: ${renderType(param.tpe)}")
      .mkString("(", ", ", ")")
  }

  private def renderType(tpe: ValType)(
      implicit typeImports: TypeImports): String = tpe match {
    case BoolType                 => "bool"
    case U8Type                   => "u8"
    case U16Type                  => "u16"
    case U32Type                  => "u32"
    case U64Type                  => "u64"
    case S8Type                   => "s8"
    case S16Type                  => "s16"
    case S32Type                  => "s32"
    case S64Type                  => "s64"
    case F32Type                  => "f32"
    case F64Type                  => "f64"
    case CharType                 => "char"
    case StringType               => "string"
    case ListType(elemType, None) =>
      s"list<${renderType(elemType)}>"
    case ListType(elemType, Some(length)) =>
      s"list<${renderType(elemType)}, $length>"
    case RecordTypeRef(_, scope, name) =>
      renderTypeName(scope, name)
    case VariantTypeRef(_, scope, name) =>
      renderTypeName(scope, name)
    case EnumTypeRef(_, scope, name) =>
      renderTypeName(scope, name)
    case FlagsTypeRef(_, scope, name) =>
      renderTypeName(scope, name)
    case ResourceType(resource, ResourceOwnership.Own) =>
      s"own<${renderTypeName(resource.scope, resource.name)}>"
    case ResourceType(resource, ResourceOwnership.Borrow) =>
      s"borrow<${renderTypeName(resource.scope, resource.name)}>"
    case TupleType(ts) =>
      ts.map(renderType).mkString("tuple<", ", ", ">")
    case ResultType(None, None) =>
      "result"
    case ResultType(Some(ok), None) =>
      s"result<${renderType(ok)}>"
    case ResultType(None, Some(err)) =>
      s"result<_, ${renderType(err)}>"
    case ResultType(Some(ok), Some(err)) =>
      s"result<${renderType(ok)}, ${renderType(err)}>"
    case OptionType(valueType) =>
      s"option<${renderType(valueType)}>"
  }

  private def renderTypeName(scope: WitScope, name: String)(
      implicit typeImports: TypeImports): String = {
    val alias = typeImports.visibleTypeNames.getOrElse((scope, name),
        throw new AssertionError(
            s"missing local name for type ${scopeDisplay(scope)}/$name"))
    alias
  }

  /** Computes the `use` declarations and local names needed to render type refs.
   *
   *  Types defined in `currentScope` keep their own names. Types from other
   *  interfaces are imported with `use`; if their name conflicts with a local
   *  type name or an earlier import, they get a suffixed alias.
   */
  private def resolveTypeImports(referencedTypes: List[ValType],
      localTypes: List[WitTypeDef], currentScope: WitScope): TypeImports = {
    val localTypeNames = localTypes.map(
        namedType =>
          (namedType.scope, namedType.name) -> namedType.name).toMap

    // Collect every named type reference in this scope's function and type bodies.
    val referencedTypeNames = mutable.Set.empty[(WitScope, String)]
    referencedTypes.foreach(collectTypeRefs(_, referencedTypeNames))

    val externalTypeRefs = referencedTypeNames.filter(_._1 != currentScope).toList
    val usedLocalNames = mutable.Set.empty[String] ++ localTypeNames.values

    // Names visible types in this scope. Local refs map to themselves,
    // and imported refs may map to aliases by `use ... as ...`
    val visibleTypeNames =
      mutable.Map.empty[(WitScope, String), String] ++ localTypeNames

    def freshLocalName(name: String): String = {
      if (!usedLocalNames.contains(name)) {
        usedLocalNames += name
        name
      } else {
        var suffix = 1
        var localName = s"$name-$suffix"
        while (usedLocalNames.contains(localName)) {
          suffix += 1
          localName = s"$name-$suffix"
        }
        usedLocalNames += localName
        localName
      }
    }

    for (ref @ (_, typeName) <- externalTypeRefs) {
      visibleTypeNames(ref) = freshLocalName(typeName)
    }

    def usePath(refScope: WitScope): String = refScope match {
      case refIface: WitScope.Interface =>
        currentScope match {
          case currentIface: WitScope.Interface
              if currentIface.namespace == refIface.namespace &&
                currentIface.packageName == refIface.packageName &&
                currentIface.version == refIface.version =>
            refIface.name
          case _ =>
            refIface.witId
        }
      case other =>
        throw new AssertionError(
            "cross-interface type reference to non-canonical scope " +
            scopeDisplay(other))
    }

    val uses = externalTypeRefs.groupBy(_._1).map {
      case (refScope, refsInScope) =>
        val names = refsInScope.map { ref =>
          val typeName = ref._2
          val localName = visibleTypeNames(ref)
          if (localName == typeName) typeName
          else s"$typeName as $localName"
        }
        s"use ${usePath(refScope)}.{${names.mkString(", ")}};"
    }.toList
    TypeImports(uses, visibleTypeNames.toMap)
  }

  private def collectTypeRefs(tpe: ValType,
      refs: mutable.Set[(WitScope, String)]): Unit = {
    tpe match {
      case ListType(elemType, _) =>
        collectTypeRefs(elemType, refs)
      case RecordTypeRef(_, scope, name) =>
        refs += ((scope, name))
      case VariantTypeRef(_, scope, name) =>
        refs += ((scope, name))
      case EnumTypeRef(_, scope, name) =>
        refs += ((scope, name))
      case FlagsTypeRef(_, scope, name) =>
        refs += ((scope, name))
      case ResourceType(resource, _) =>
        refs += ((resource.scope, resource.name))
      case TupleType(ts) =>
        ts.foreach(collectTypeRefs(_, refs))
      case ResultType(ok, err) =>
        ok.foreach(collectTypeRefs(_, refs))
        err.foreach(collectTypeRefs(_, refs))
      case OptionType(valueType) =>
        collectTypeRefs(valueType, refs)
      case _: PrimValType =>
        ()
    }
  }

  private def resourceName(endpoint: Func): String = {
    endpoint.name match {
      case WitFunctionName.ResourceMethod(_, resource)       => resource
      case WitFunctionName.ResourceStaticMethod(_, resource) => resource
      case WitFunctionName.ResourceConstructor(resource)     => resource
      case WitFunctionName.ResourceDrop(resource)            => resource
      case _: WitFunctionName.Function                       =>
        throw new AssertionError("ordinary function has no resource name")
    }
  }

  private def scopeDisplay(scope: WitScope): String = scope match {
    case iface: WitScope.Interface => iface.witId
    case WitScope.Inline(name)     => name
    case WitScope.Root             => "<world>"
  }

}
