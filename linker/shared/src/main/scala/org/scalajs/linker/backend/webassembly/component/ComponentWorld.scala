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

package org.scalajs.linker.backend.webassembly.component

import scala.collection.mutable

import org.scalajs.ir.Names.ClassName
import org.scalajs.ir.Trees.{WitExportDef, WitFunctionName}
import org.scalajs.ir.{WitScope, WitTypeDef, WitAliasDef}
import org.scalajs.ir.WasmInterfaceTypes._
import org.scalajs.linker.interface.WasmComponentModuleInitializerExport
import org.scalajs.linker.standard.ModuleSet

/** Reachable WIT world metadata collected from linked IR. */
private[backend] final case class ComponentWorld(
    packageName: String,
    worldName: String,
    imports: List[ComponentWorld.WorldItem],
    exports: List[ComponentWorld.WorldItem],
    endpoints: List[ComponentWorld.Endpoint],
    typeDefs: List[WitTypeDef],
    aliases: List[WitAliasDef]
) {
  def endpointsFor(scope: WitScope): List[ComponentWorld.Endpoint] =
    endpoints.filter(_.scope == scope)

  def typesFor(scope: WitScope): List[WitTypeDef] =
    typeDefs.filter(_.scope == scope)

  def aliasesFor(scope: WitScope): List[WitAliasDef] =
    aliases.filter(_.scope == scope)

  def typeDefByClass: Map[ClassName, WitTypeDef] =
    typeDefs.map(td => td.className -> td).toMap

  def aliasMap: Map[(WitScope, String), ValType] =
    aliases.map(a => (a.scope, a.name) -> a.target).toMap
}

private[backend] object ComponentWorld {
  private final val WorldName = "synthetic"
  private final val RootPackage = "scala-wasm:generated"

  sealed abstract class Direction

  object Direction {
    case object Import extends Direction
    case object Export extends Direction
  }

  /** World-level import or export. */
  sealed abstract class WorldItem

  object WorldItem {
    final case class Interface(scope: WitScope.Interface) extends WorldItem
    final case class Inline(scope: WitScope.Inline) extends WorldItem
    final case class Func(func: Endpoint) extends WorldItem

    /** World-level named type: Root typedef or `use iface.{T}`. */
    final case class Type(named: WitTypeDef) extends WorldItem

    /** World-level `type foo = ...` or `use iface.{T as Z}`. */
    final case class Alias(alias: WitAliasDef) extends WorldItem
  }

  final case class Endpoint(
      direction: Direction,
      scope: WitScope,
      name: WitFunctionName,
      signature: FuncType
  )

  def fromModule(module: ModuleSet.Module,
      moduleInitializerExport: Option[WasmComponentModuleInitializerExport]): ComponentWorld = {
    // correct all Wasm Component imports/exports and type definitions from IR
    val allEndpoints: List[Endpoint] = collectEndpoints(module, moduleInitializerExport)
    val allTypeDefs: List[WitTypeDef] = collectTypeDefs(module)
    val allAliases: List[WitAliasDef] = collectAliasDefs(module)

    val importInterfaces = mutable.LinkedHashSet.empty[WitScope.Interface]
    val importInlines = mutable.LinkedHashSet.empty[WitScope.Inline]
    val importRoots = mutable.LinkedHashSet.empty[Endpoint]
    val exportRoots = mutable.LinkedHashSet.empty[Endpoint]
    val exportInlines = mutable.LinkedHashSet.empty[WitScope.Inline]
    val exportInterfaces = mutable.LinkedHashSet.empty[WitScope.Interface]

    allEndpoints.foreach { ep =>
      ep.scope match {
        case s: WitScope.Interface =>
          if (ep.direction == Direction.Import) importInterfaces += s
          else exportInterfaces += s
        case s: WitScope.Inline =>
          if (ep.direction == Direction.Import) importInlines += s
          else exportInlines += s
        case WitScope.Root =>
          if (ep.direction == Direction.Import) importRoots += ep
          else exportRoots += ep
      }
      ()
    }
    val typesByScope = allTypeDefs.groupBy(_.scope)
    val endpointsByScope = allEndpoints.groupBy(_.scope)
    val aliasesByScope = allAliases.groupBy(_.scope)
    val typeDefByClass = allTypeDefs.map(td => td.className -> td).toMap

    // World-level named types: Root typedefs and `use` of types referenced by Root funcs.
    val worldTypes = mutable.LinkedHashMap.empty[ClassName, WitTypeDef]
    def addWorldType(td: WitTypeDef): Unit = {
      td.scope match {
        case WitScope.Root =>
          if (!worldTypes.contains(td.className)) {
            worldTypes(td.className) = td
            for (className <- namedTypeRefs(td))
              addWorldType(typeDefByClass(className))
          }
        case _: WitScope.Interface =>
          worldTypes.getOrElseUpdate(td.className, td)
        case _ =>
      }
    }
    typesByScope.getOrElse(WitScope.Root, Nil).foreach(addWorldType)
    for {
      ep <- importRoots.iterator ++ exportRoots.iterator
      className <- endpointTypeRefs(ep)
    } {
      typeDefByClass.get(className).foreach(addWorldType)
    }
    // Root `use other.{T}` aliases (no ClassName on the alias itself).
    for {
      alias <- aliasesByScope.getOrElse(WitScope.Root, Nil)
      className <- valTypeRefs(alias.target)
    } {
      typeDefByClass.get(className).foreach(addWorldType)
    }

    // Interfaces referenced only from Root bare funcs still need a world import
    // so `(alias export ...)` can bring those types into scope.
    val rootIfaceDeps = mutable.LinkedHashSet.empty[WitScope.Interface]
    worldTypes.values.foreach { td =>
      td.scope match {
        case iface: WitScope.Interface => rootIfaceDeps += iface
        case _                         =>
      }
    }

    val interfacesToImport = {
      transitiveInterfaceDeps(
          importInterfaces ++ exportInterfaces ++ rootIfaceDeps, endpointsByScope,
          typesByScope, aliasesByScope, typeDefByClass) ++ importInterfaces ++ rootIfaceDeps
    }

    val imports: List[WorldItem] = {
      val b = List.newBuilder[WorldItem]
      interfacesToImport.foreach(i => b += WorldItem.Interface(i))
      worldTypes.values.foreach(td => b += WorldItem.Type(td))
      aliasesByScope.getOrElse(WitScope.Root, Nil).foreach(a => b += WorldItem.Alias(a))
      importInlines.foreach(i => b += WorldItem.Inline(i))
      importRoots.foreach(i => b += WorldItem.Func(i))
      b.result()
    }
    val exports: List[WorldItem] = {
      val b = List.newBuilder[WorldItem]
      exportRoots.foreach(ep => b += WorldItem.Func(ep))
      exportInlines.foreach(inline => b += WorldItem.Inline(inline))
      exportInterfaces.foreach(iface => b += WorldItem.Interface(iface))
      b.result()
    }

    ComponentWorld(RootPackage, WorldName, imports, exports, allEndpoints, allTypeDefs,
        allAliases)
  }

  private def collectEndpoints(module: ModuleSet.Module,
      moduleInitializerExport: Option[WasmComponentModuleInitializerExport]): List[Endpoint] = {
    val imported: List[Endpoint] = (for {
      classDef <- module.classDefs
      member <- classDef.witNativeMembers
    } yield {
      Endpoint(Direction.Import, member.scope, member.name, member.signature)
    }).toList

    val exported: List[Endpoint] = module.topLevelExports.flatMap { linked =>
      linked.tree match {
        case witExport: WitExportDef =>
          Some(Endpoint(Direction.Export, witExport.scope, witExport.name,
              witExport.signature))
        case _ =>
          None
      }
    }

    val initializer: List[Endpoint] = moduleInitializerExport.map { init =>
      val resultType = init.resultType match {
        case WasmComponentModuleInitializerExport.ResultType.Unit =>
          None
        case WasmComponentModuleInitializerExport.ResultType.ResultUnitUnit =>
          Some(ResultType(None, None))
      }
      Endpoint(Direction.Export, init.scope,
          WitFunctionName.Function(init.functionName),
          FuncType(Nil, resultType))
    }.toList

    imported ::: exported ::: initializer
  }

  private def collectTypeDefs(module: ModuleSet.Module): List[WitTypeDef] = {
    val byKey = mutable.LinkedHashMap.empty[(WitScope, String), WitTypeDef]
    for (td <- module.classDefs.iterator.flatMap(_.witTypeDef))
      byKey.getOrElseUpdate((td.scope, td.name), td)
    byKey.values.toList
  }

  private def collectAliasDefs(module: ModuleSet.Module): List[WitAliasDef] = {
    val byKey = mutable.Map.empty[(WitScope, String), WitAliasDef]
    for {
      classDef <- module.classDefs
      alias <- classDef.witAliases
    } {
      byKey.getOrElseUpdate((alias.scope, alias.name), alias)
    }
    byKey.values.toList
  }

  /** Interfaces transitively referenced by `roots` via `use`. */
  private def transitiveInterfaceDeps(
      roots: Iterable[WitScope.Interface],
      endpointsByScope: Map[WitScope, List[Endpoint]],
      typesByScope: Map[WitScope, List[WitTypeDef]],
      aliasesByScope: Map[WitScope, List[WitAliasDef]],
      typeDefByClass: Map[ClassName, WitTypeDef]): mutable.LinkedHashSet[WitScope.Interface] = {
    def depsOf(iface: WitScope.Interface): Iterable[WitScope.Interface] = {
      val deps = mutable.HashSet.empty[WitScope.Interface]
      def addIfForeign(scope: WitScope): Unit = scope match {
        case dep: WitScope.Interface if dep != iface => deps += dep
        case _                                       =>
      }
      def visit(tpe: ValType): Unit = {
        for (className <- valTypeRefs(tpe))
          typeDefByClass.get(className).foreach(td => addIfForeign(td.scope))
        for (a <- aliasTypeRefs(tpe))
          addIfForeign(a.scope)
      }

      for (named <- typesByScope.getOrElse(iface, Nil)) {
        named match {
          case WitTypeDef.Record(_, _, _, fields) =>
            fields.foreach(f => visit(f.tpe))
          case WitTypeDef.Variant(_, _, _, cases) =>
            cases.foreach(_.tpe.foreach(visit))
          case _ =>
        }
      }
      for (ep <- endpointsByScope.getOrElse(iface, Nil)) {
        ep.signature.params.foreach(p => visit(p.tpe))
        ep.signature.resultType.foreach(visit)
      }
      aliasesByScope.getOrElse(iface, Nil).foreach(a => visit(a.target))
      deps
    }

    val seen = mutable.LinkedHashSet.empty[WitScope.Interface]
    def visit(iface: WitScope.Interface): Unit = {
      for (dep <- depsOf(iface) if seen.add(dep))
        visit(dep)
    }

    roots.foreach(visit)
    seen
  }

  /** Collect ClassNames referenced from an endpoint signature. */
  private[component] def endpointTypeRefs(func: Endpoint): List[ClassName] = {
    val tpes = func.signature.params.map(_.tpe) ++ func.signature.resultType
    tpes.flatMap(valTypeRefs)
  }

  /** Collect ClassNames referenced from the given named type definition. */
  private[component] def namedTypeRefs(named: WitTypeDef): List[ClassName] = {
    named match {
      case WitTypeDef.Record(_, _, _, fields) =>
        fields.flatMap(f => valTypeRefs(f.tpe))
      case WitTypeDef.Variant(_, _, _, cases) =>
        cases.flatMap(_.tpe.toList.flatMap(valTypeRefs(_)))
      case _ =>
        Nil
    }
  }

  /** Collect ClassNames of named types referenced from the given value type. */
  private[component] def valTypeRefs(tpe: ValType): List[ClassName] = {
    tpe match {
      case ListType(elem, _) =>
        valTypeRefs(elem)
      case RecordTypeRef(className) =>
        className :: Nil
      case VariantTypeRef(className) =>
        className :: Nil
      case EnumTypeRef(className) =>
        className :: Nil
      case FlagsTypeRef(className) =>
        className :: Nil
      case ResourceType(className, _) =>
        className :: Nil
      case AliasTypeRef(_, _, _) =>
        Nil // named types come from WitAliasDef targets
      case TupleType(ts) =>
        ts.flatMap(valTypeRefs(_))
      case ResultType(ok, err) =>
        ok.toList.flatMap(valTypeRefs(_)) ++
        err.toList.flatMap(valTypeRefs(_))
      case OptionType(inner) =>
        valTypeRefs(inner)
      case _: PrimValType =>
        Nil
    }
  }

  /** Collect named WIT aliases referenced from a value type. */
  private[component] def aliasTypeRefs(tpe: ValType): List[AliasTypeRef] = {
    tpe match {
      case a @ AliasTypeRef(_, _, _) =>
        a :: Nil
      case ListType(elem, _) =>
        aliasTypeRefs(elem)
      case TupleType(ts) =>
        ts.flatMap(aliasTypeRefs(_))
      case ResultType(ok, err) =>
        ok.toList.flatMap(aliasTypeRefs(_)) ++
        err.toList.flatMap(aliasTypeRefs(_))
      case OptionType(inner) =>
        aliasTypeRefs(inner)
      case _ =>
        Nil
    }
  }

  private[component] def endpointAliasRefs(func: Endpoint): List[AliasTypeRef] = {
    val tpes = func.signature.params.map(_.tpe) ++ func.signature.resultType
    tpes.flatMap(aliasTypeRefs)
  }

  private[component] def namedTypeAliasRefs(named: WitTypeDef): List[AliasTypeRef] = {
    named match {
      case WitTypeDef.Record(_, _, _, fields) =>
        fields.flatMap(f => aliasTypeRefs(f.tpe))
      case WitTypeDef.Variant(_, _, _, cases) =>
        cases.flatMap(_.tpe.toList.flatMap(aliasTypeRefs(_)))
      case _ =>
        Nil
    }
  }

  private[component] def collectAliasesForScope(scope: WitScope,
      defined: List[WitAliasDef],
      endpoints: List[Endpoint], namedTypes: List[WitTypeDef]): List[WitAliasDef] = {
    val byKey = mutable.Map.empty[(WitScope, String), WitAliasDef]
    for (a <- defined if a.scope == scope)
      byKey.getOrElseUpdate((a.scope, a.name), a)

    for (ref <- endpoints.flatMap(endpointAliasRefs) ++ namedTypes.flatMap(namedTypeAliasRefs)
        if ref.scope == scope) {
      if (!byKey.contains((ref.scope, ref.name))) {
        throw new AssertionError(
            s"missing WitAliasDef for ${ref.scope}/${ref.name}")
      }
    }
    byKey.values.toList
  }
}
