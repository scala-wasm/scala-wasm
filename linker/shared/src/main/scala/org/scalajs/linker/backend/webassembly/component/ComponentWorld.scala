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
import org.scalajs.ir.{WitScope, WitTypeDef}
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
    typeDefs: List[WitTypeDef]
) {
  def endpointsFor(scope: WitScope): List[ComponentWorld.Endpoint] =
    endpoints.filter(_.scope == scope)

  def typesFor(scope: WitScope): List[WitTypeDef] =
    typeDefs.filter(_.scope == scope)

  def typeDefByClass: Map[ClassName, WitTypeDef] =
    typeDefs.map(td => td.className -> td).toMap
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
    val typeDefByClass = allTypeDefs.map(td => td.className -> td).toMap

    // Dependency interfaces needed so imports/exports can `use` their types
    val interfacesToImport = {
      dependencyInterfaces(
          importInterfaces ++ exportInterfaces, endpointsByScope, typesByScope,
          typeDefByClass) ++ importInterfaces
    }

    val imports: List[WorldItem] = {
      val b = List.newBuilder[WorldItem]
      interfacesToImport.foreach(i => b += WorldItem.Interface(i))
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

    ComponentWorld(RootPackage, WorldName, imports, exports, allEndpoints, allTypeDefs)
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

  /** Interfaces that `roots` depend on via `use`, in topological order. */
  private def dependencyInterfaces(
      roots: Iterable[WitScope.Interface],
      endpointsByScope: Map[WitScope, List[Endpoint]],
      typesByScope: Map[WitScope, List[WitTypeDef]],
      typeDefByClass: Map[ClassName, WitTypeDef]): mutable.LinkedHashSet[WitScope.Interface] = {
    def depsOf(iface: WitScope.Interface): Iterable[WitScope.Interface] = {
      val deps = mutable.HashSet.empty[WitScope.Interface]
      val refsByTypes = for {
        named <- typesByScope.getOrElse(iface, Nil)
        ref <- namedTypeRefs(named, typeDefByClass)
      } yield ref
      val refsByEndpoints = for {
        func <- endpointsByScope.getOrElse(iface, Nil)
        tpe <- func.signature.params.map(_.tpe) ++ func.signature.resultType
        ref <- valTypeRefs(tpe, typeDefByClass)
      } yield ref
      (refsByTypes ++ refsByEndpoints).foreach {
        // Only named package interfaces need a world import for `use`
        case (dep: WitScope.Interface, _) if dep != iface => deps += dep
        case _                                            =>
      }
      deps
    }

    val ordered = mutable.LinkedHashSet.empty[WitScope.Interface]
    def visit(iface: WitScope.Interface): Unit = {
      for (dep <- depsOf(iface) if !ordered.contains(dep)) {
        visit(dep)
        ordered += dep
      }
    }

    roots.foreach(visit)
    ordered
  }

  /** Collect types referenced from the given named type definition. */
  private[component] def namedTypeRefs(named: WitTypeDef,
      typeDefByClass: Map[ClassName, WitTypeDef]): List[(WitScope, String)] = {
    named match {
      case WitTypeDef.Record(_, _, _, fields) =>
        fields.flatMap(f => valTypeRefs(f.tpe, typeDefByClass))
      case WitTypeDef.Variant(_, _, _, cases) =>
        cases.flatMap(_.tpe.toList.flatMap(valTypeRefs(_, typeDefByClass)))
      case _ =>
        Nil
    }
  }

  /** Collect types referenced from the given value type. */
  private[component] def valTypeRefs(tpe: ValType,
      typeDefByClass: Map[ClassName, WitTypeDef]): List[(WitScope, String)] = {
    def namedRef(className: ClassName): List[(WitScope, String)] = {
      val td = typeDefByClass.getOrElse(className,
          throw new AssertionError(
              s"missing WitTypeDef for ${className.nameString}"))
      (td.scope, td.name) :: Nil
    }

    tpe match {
      case ListType(elem, _) =>
        valTypeRefs(elem, typeDefByClass)
      case RecordTypeRef(className) =>
        namedRef(className)
      case VariantTypeRef(className) =>
        namedRef(className)
      case EnumTypeRef(className) =>
        namedRef(className)
      case FlagsTypeRef(className) =>
        namedRef(className)
      case ResourceType(className, _) =>
        namedRef(className)
      case TupleType(ts) =>
        ts.flatMap(valTypeRefs(_, typeDefByClass))
      case ResultType(ok, err) =>
        ok.toList.flatMap(valTypeRefs(_, typeDefByClass)) ++
        err.toList.flatMap(valTypeRefs(_, typeDefByClass))
      case OptionType(inner) =>
        valTypeRefs(inner, typeDefByClass)
      case _: PrimValType =>
        Nil
    }
  }
}
