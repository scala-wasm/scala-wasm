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
import org.scalajs.ir.Trees.WitFunctionName
import org.scalajs.ir.{WitScope, WitTypeDef, WitAliasDef, ResourceOwnership}
import org.scalajs.ir.WasmInterfaceTypes.{ExternType => _, _}

import Components._
import ComponentWorld._

/** Encode one WIT world into componenttype decls. */
private[component] final class WorldEncoder(world: ComponentWorld) {
  private val builder = new ComponentBuilder
  private val typeDefByClass = world.typeDefByClass

  private val instanceIds: Map[WitScope.Interface, InstanceID] = {
    world.imports.collect {
      // We don't pre-allocate instance IDs for inline blocks or funcs, because WIT cannot
      // import types/functions defined in inline interface.
      case ComponentWorld.WorldItem.Interface(iface) =>
        iface -> new InstanceID
    }.toMap
  }

  private val typeIds: Map[(WitScope, String), TypeID] = {
    val buf = mutable.Map.empty[(WitScope, String), TypeID]
    def alloc(key: (WitScope, String)): TypeID =
      buf.getOrElseUpdate(key, new TypeID)

    for (td <- world.imports.collect { case WorldItem.Type(t) => t })
      alloc((td.scope, td.name))
    for (a <- world.imports.collect { case WorldItem.Alias(a) => a })
      alloc((a.scope, a.name))
    for (key <- (world.imports ++ world.exports).flatMap(foreignUses))
      alloc(key)

    buf.toMap
  }

  private val typeIdsByClass: Map[ClassName, TypeID] = {
    typeDefByClass.flatMap { case (className, td) =>
      typeIds.get((td.scope, td.name)).map(className -> _)
    }
  }

  private val emittedAliasExports = mutable.Set.empty[(WitScope, String)]

  def encode(): List[Decl] = {
    // Write each world import, then each world export.
    //
    // When an import/export item uses a type defined in another interface (via `use`),
    // bring the type onto the world first with `(alias export ...)`, then encode
    // the item (which pulls it in via `alias outer`).
    //
    // For example, wasi:io/streams uses a type from wasi:io/error:
    //
    // {{{
    // interface streams {
    //   use wasi:io/error.{error};
    //   write: func(e: error);
    // }
    //
    // (import "wasi:io/error" (instance $error ...))
    // (alias export $error "error" (type $t)) ;; $t = type from $error's exports
    // (import "wasi:io/streams" (instance (type (instance
    //   (alias outer 1 $t (type $error))  ;; $error = $t from 1 outer scope
    //   (export "error" (type $error))
    //   ...))))
    // }}}
    emitImports()
    emitExports()
    builder.decls()
  }

  /** Encode WIT `import` items into world componenttype import decls. */
  private def emitImports(): Unit = {
    for (item <- topologicalSortWorldItems(world.imports)) {
      item match {
        case ComponentWorld.WorldItem.Type(named) =>
          encodeWorldType(named)
        case ComponentWorld.WorldItem.Alias(alias) =>
          aliasExports(item)
          encodeWorldAlias(alias)
        case ComponentWorld.WorldItem.Interface(iface) =>
          // alias export imported types
          // e.g. (alias export $error "error" (type $t))
          aliasExports(item)
          builder.addInstanceImport(iface.witId, instanceType(iface), instanceIds(iface))
          // `(import "name" (instance ...))`
        case ComponentWorld.WorldItem.Inline(inline) =>
          aliasExports(item)
          builder.addInstanceImport(inline.name, instanceType(inline))
          // `(import "f" (func ...))`
        case ComponentWorld.WorldItem.Func(func) =>
          aliasExports(item)
          builder.addImport(WitFunctionName.wasmName(func.name),
              ExternType.Func(bareFunc(func)))
      }
    }
  }

  private def emitExports(): Unit = {
    for (item <- topologicalSortWorldItems(world.exports)) {
      item match {
        // `(export "ns:pkg/iface" (instance ...))`
        case ComponentWorld.WorldItem.Interface(iface) =>
          aliasExports(item)
          builder.addExport(iface.witId, ExternType.Instance(instanceType(iface)))
          // `(export "name" (instance ...))`
        case ComponentWorld.WorldItem.Inline(inline) =>
          aliasExports(item)
          builder.addExport(inline.name, ExternType.Instance(instanceType(inline)))
          // `(export "f" (func ...))`
        case ComponentWorld.WorldItem.Func(func) =>
          aliasExports(item)
          builder.addExport(WitFunctionName.wasmName(func.name),
              ExternType.Func(bareFunc(func)))
        case _:ComponentWorld.WorldItem.Type | _:ComponentWorld.WorldItem.Alias =>
          throw new AssertionError("world types are encoded as imports")
      }
    }
  }

  /** World-level named type: Root typedef or `use iface.{T}`. */
  private def encodeWorldType(named: WitTypeDef): Unit = {
    val id = typeIdsByClass.getOrElse(named.className,
        throw new AssertionError(
            s"missing world type ${WitScope.importModuleName(named.scope)}/${named.name}"))
    named.scope match {
      case WitScope.Root =>
        named match {
          case _: WitTypeDef.Resource =>
            // `(import "$name" (type $id (sub resource)))`
            builder.addImportTypeResource(id, named.name)
          case other =>
            val body = ComponentBuilder.addNamedTypeBody(builder, other, resolve)
            // `(import "$name" (type $id (eq $body)))`
            builder.addImportTypeEq(id, other.name, body)
        }
      case _: WitScope.Interface =>
        encodeWorldUse(named, id)
      case _: WitScope.Inline =>
        throw new AssertionError(
            s"inline type ${named.name} cannot be a world-level type")
    }
  }

  // `(alias export $inst "T" $alias)` then `(import "T" (type $id (eq $alias)))`.
  private def encodeWorldUse(named: WitTypeDef, id: TypeID): Unit = {
    val owner = interfaceOf(named.className).getOrElse(
        throw new AssertionError(
            s"world use ${named.name} is not from a package interface"))
    val inst = instanceIds.getOrElse(owner,
        throw new AssertionError(
            s"missing imported dependency interface ${owner.witId} " +
            s"before aliasing ${named.name}"))
    val aliased = builder.addAliasExport(inst, named.name)
    emittedAliasExports += ((owner, named.name))
    builder.addImportTypeEq(id, named.name, aliased)
  }

  private def encodeWorldAlias(alias: WitAliasDef): Unit = {
    val id = typeIds.getOrElse((alias.scope, alias.name),
        throw new AssertionError(
            s"missing world type ${alias.scope}/${alias.name}"))
    alias.scope match {
      case WitScope.Root =>
        val defId = alias.target match {
          case ResourceType(className, _) =>
            typeIdsByClass.getOrElse(className,
                throw new AssertionError(
                    s"missing foreign resource for WitAlias ${alias.name}"))
          case other =>
            resolve(other) match {
              case ValRef.Type(tid) => tid
              case prim             => builder.addValTypeDef(prim)
            }
        }
        builder.addImportTypeEq(id, alias.name, defId)
      case _:WitScope.Interface | _:WitScope.Inline =>
        throw new AssertionError(
            s"world-level WitAlias ${alias.name} must be Root, got ${alias.scope}")
    }
  }

  private def topologicalSortWorldItems(
      items: List[ComponentWorld.WorldItem]): List[ComponentWorld.WorldItem] = {
    val interfaceItems = items.collect {
      case i @ ComponentWorld.WorldItem.Interface(iface) => iface -> i
    }.toMap
    val namedItems = items.collect {
      case t @ ComponentWorld.WorldItem.Type(named) => (named.scope, named.name) -> t
      case a @ ComponentWorld.WorldItem.Alias(al)   => (al.scope, al.name) -> a
    }.toMap
    val ordered = mutable.LinkedHashSet.empty[ComponentWorld.WorldItem]
    val visiting = mutable.Set.empty[ComponentWorld.WorldItem]

    def visit(item: ComponentWorld.WorldItem): Unit = {
      val self: Option[WitScope] = item match {
        case ComponentWorld.WorldItem.Interface(iface) => Some(iface)
        case ComponentWorld.WorldItem.Inline(inline)   => Some(inline)
        case _                                         => None
      }
      if (ordered.contains(item)) {
        // skip
      } else if (!visiting.add(item)) {
        throw new AssertionError("cyclic world item dependency")
      } else {
        item match {
          case ComponentWorld.WorldItem.Type(named) =>
            named.scope match {
              case iface: WitScope.Interface =>
                interfaceItems.get(iface).foreach { ifaceItem =>
                  if (!visiting.contains(ifaceItem)) visit(ifaceItem)
                }
              case _ =>
            }
          case _ =>
        }
        for ((scope, name) <- namedKeysUsedBy(item)) {
          if (self.forall(_ != scope)) {
            namedItems.get((scope, name)).foreach { dep =>
              if (dep != item) visit(dep)
            }
          }
          scope match {
            case iface: WitScope.Interface if self.forall(_ != iface) =>
              interfaceItems.get(iface).foreach(visit)
            case _ =>
          }
        }
        visiting -= item
        ordered += item
      }
    }

    items.foreach(visit)
    ordered.toList
  }

  // `(alias export $id "T" $inst)` for each first-seen `use ns:pkg/iface.{T}`.
  private def aliasExports(item: ComponentWorld.WorldItem): Unit = {
    for ((scope, name) <- foreignUses(item))
      aliasExport(scope, name)
  }

  private def aliasExport(scope: WitScope, name: String): Unit = {
    if (emittedAliasExports.add((scope, name))) {
      val owner = scope match {
        case iface: WitScope.Interface => iface
        case other                     =>
          throw new AssertionError(
              s"foreign type $name is not from a package interface: $other")
      }
      val inst = instanceIds.getOrElse(owner,
          throw new AssertionError(
              s"missing imported dependency interface ${owner.witId} " +
              s"before aliasing $name"))
      val id = typeIds.getOrElse((scope, name),
          throw new AssertionError(
              s"missing world TypeID for $scope/$name"))
      builder.addAliasExport(id, inst, name)
    }
  }

  private def namedKey(className: ClassName): (WitScope, String) = {
    val td = typeDefByClass(className)
    (td.scope, td.name)
  }

  private def keysFromVal(tpe: ValType): List[(WitScope, String)] =
    valTypeRefs(tpe).map(namedKey) ++ aliasTypeRefs(tpe).map(a => (a.scope, a.name))

  private def namedKeysUsedBy(item: ComponentWorld.WorldItem): List[(WitScope, String)] = {
    val keys = item match {
      case ComponentWorld.WorldItem.Interface(iface) =>
        keysFromScope(iface)
      case ComponentWorld.WorldItem.Inline(inline) =>
        keysFromScope(inline)
      case ComponentWorld.WorldItem.Func(func) =>
        endpointTypeRefs(func).map(namedKey) ++
        endpointAliasRefs(func).map(a => (a.scope, a.name))
      case ComponentWorld.WorldItem.Type(named) =>
        namedTypeRefs(named).map(namedKey) ++
        namedTypeAliasRefs(named).map(a => (a.scope, a.name))
      case ComponentWorld.WorldItem.Alias(alias) =>
        keysFromVal(alias.target)
    }
    keys.distinct
  }

  private def keysFromScope(scope: WitScope): List[(WitScope, String)] = {
    world.typesFor(scope).flatMap { named =>
      namedTypeRefs(named).map(namedKey) ++
      namedTypeAliasRefs(named).map(a => (a.scope, a.name))
    } ++
    world.endpointsFor(scope).flatMap { func =>
      endpointTypeRefs(func).map(namedKey) ++
      endpointAliasRefs(func).map(a => (a.scope, a.name))
    } ++
    world.aliasesFor(scope).flatMap(a => keysFromVal(a.target))
  }

  /** Types/aliases from other interfaces that `item` references (`use`). */
  private def foreignUses(item: ComponentWorld.WorldItem): List[(WitScope, String)] = {
    val self = item match {
      case ComponentWorld.WorldItem.Interface(iface) => iface
      case ComponentWorld.WorldItem.Inline(inline)   => inline
      case ComponentWorld.WorldItem.Func(func)       => func.scope
      case ComponentWorld.WorldItem.Type(named)      => named.scope
      case ComponentWorld.WorldItem.Alias(alias)     => alias.scope
    }
    namedKeysUsedBy(item).filter { case (scope, _) =>
      scope != self && (scope match {
        case _: WitScope.Interface => true
        case _                     => false
      })
    }
  }

  private def interfaceOf(className: ClassName): Option[WitScope.Interface] = {
    typeDefByClass(className).scope match {
      case iface: WitScope.Interface => Some(iface)
      case _                         => None
    }
  }

  private def resolve(tpe: ValType): ValRef = {
    def lookupAlias(scope: WitScope, name: String): ValRef = {
      val id = typeIds.getOrElse((scope, name),
          throw new AssertionError(s"missing world WitAlias for $scope/$name"))
      world.aliasMap.get((scope, name)) match {
        case Some(ResourceType(_, ownership)) =>
          ownership match {
            case ResourceOwnership.Own =>
              ValRef.Type(builder.addOwn(id))
            case ResourceOwnership.Borrow =>
              ValRef.Type(builder.addBorrow(id))
          }
        case Some(target) =>
          resolve(target)
        case None =>
          ValRef.Type(id)
      }
    }
    ComponentBuilder.resolveValRef(builder, tpe, typeIdsByClass, lookupAlias)
  }

  private def bareFunc(func: ComponentWorld.Endpoint): TypeID = {
    val params = func.signature.params.map { p =>
      p.name -> resolve(p.tpe)
    }
    val result = func.signature.resultType.map(resolve)
    builder.addFuncType(params, result)
  }

  private def instanceType(scope: WitScope): TypeID = {
    builder.addInstanceType(ComponentBuilder.encodeInstanceType(
        scope, world.endpointsFor(scope), world.typesFor(scope),
        world.aliasesFor(scope), typeIds, typeDefByClass))
  }
}
