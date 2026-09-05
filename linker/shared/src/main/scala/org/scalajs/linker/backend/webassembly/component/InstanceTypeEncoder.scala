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
import org.scalajs.ir.{WitScope, WitTypeDef, WitNamedTypeDef, WitAliasDef}
import org.scalajs.ir.WasmInterfaceTypes.{ExternType => _, _}

import Components._
import ComponentWorld._

private[component] final class InstanceTypeEncoder(
    scope: WitScope,
    endpoints: List[ComponentWorld.Endpoint],
    namedTypes: List[WitNamedTypeDef],
    definedAliases: List[WitAliasDef],
    worldTypeIds: Map[(WitScope, String), TypeID],
    typeDefByClass: Map[ClassName, WitNamedTypeDef]
) {
  private val builder = new ComponentBuilder
  private var localTypeIdx = Map.empty[ClassName, TypeID]
  private var aliasIdx = Map.empty[String, TypeID]
  private val resourceAliasNames = mutable.Set.empty[String]

  private val localAliases =
    collectAliasesForScope(scope, definedAliases, endpoints, namedTypes)

  private val byAliasName = localAliases.map(a => a.name -> a).toMap
  private val visitingAliases = mutable.Set.empty[String]

  def encode(): List[Decl] = {
    // Step 1: `use other.{T}` (named type or alias). `alias outer` the
    // world's TypeID, then export unless a local WitAlias covers the name.
    //   (alias outer 1 $t (type $error))
    //   (export "error" (type $error))
    importForeignUses()

    // Step 2: Export local named types and aliases. Named types first
    // then remaining aliases (`type foo = ...` only used by funcs).
    //   record r1 { a: u8 }
    //     (type $r1 (record (field "a" u8)))
    //     (export "r1" (type $r1))
    //   type option-u32 = option<u32>
    //     (type $t (option u32))
    //     (export "option-u32" (type $t))
    exportLocalTypes()

    // Step 3: Export funcs.
    //   f: func(a: option-u32) -> option-u32
    //     (export "f" (func (param "a" (type $t)) (result (type $t))))
    exportFuncs()
    builder.decls()
  }

  private def resolve(tpe: ValType): ValRef = {
    // Alias ref -> alias definition (not dealias).
    def lookupAlias(scope: WitScope, name: String): ValRef = {
      val id = aliasIdx.getOrElse(name,
          emitAlias(byAliasName.getOrElse(name,
              throw new AssertionError(s"missing local WitAlias export `$name`"))))
      if (resourceAliasNames.contains(name))
        ValRef.Type(builder.addOwn(id))
      else
        ValRef.Type(id)
    }
    ComponentBuilder.resolveValRef(builder, tpe, localTypeIdx, lookupAlias)
  }

  private def importForeignUses(): Unit = {
    def aliasOuter(owner: WitScope, name: String, exportType: Boolean): TypeID = {
      val outerId = worldTypeIds.getOrElse((owner, name),
          throw new AssertionError(
              s"missing aliased foreign type for $owner/$name"))
      val aliased = builder.addAliasOuter(outerId)
      if (exportType) builder.addExportTypeEq(name, aliased) else aliased
    }

    val usedFromOtherIfaces = (
      namedTypes.flatMap(namedTypeRefs) ++ endpoints.flatMap(endpointTypeRefs) ++
        localAliases.flatMap(a => valTypeRefs(a.target))
    ).distinct.filter { className =>
      typeDefByClass(className).scope != scope
    }
    for (className <- usedFromOtherIfaces) {
      val td = typeDefByClass(className)
      val covered = localAliases.exists { a =>
        a.name == td.name || valTypeRefs(a.target).contains(className)
      }
      localTypeIdx += className -> aliasOuter(td.scope, td.name, exportType = !covered)
    }

    val foreignAliases = (
      endpoints.flatMap(endpointAliasRefs) ++
        namedTypes.flatMap(namedTypeAliasRefs) ++
        localAliases.flatMap(a => aliasTypeRefs(a.target))
    ).filter(_.scope != scope).distinct
    for (a <- foreignAliases)
      aliasIdx += a.name -> aliasOuter(a.scope, a.name, exportType = true)
  }

  private def exportLocalTypes(): Unit = {
    // Topo-sort so field typeidxs exist when bodies are encoded.
    for (named <- ComponentBuilder.topologicalSortNamedTypes(namedTypes, localAliases)) {
      named match {
        case _: WitTypeDef.Resource =>
          localTypeIdx += named.className -> builder.addExportResource(named.name)
        case other =>
          val defId = ComponentBuilder.addNamedTypeBody(builder, other, resolve)
          localTypeIdx += other.className -> builder.addExportTypeEq(other.name, defId)
      }
    }
    localAliases.foreach(emitAlias)
  }

  private def emitAlias(alias: WitAliasDef): TypeID = {
    aliasIdx.get(alias.name) match {
      case Some(id) => id
      case None     =>
        if (!visitingAliases.add(alias.name))
          throw new AssertionError(s"cyclic WitAlias dependency ${alias.name}")
        val defId = alias.target match {
          case ResourceType(className, _) =>
            resourceAliasNames += alias.name
            localTypeIdx.getOrElse(className,
                throw new AssertionError(
                    s"missing foreign resource for WitAlias ${alias.name}"))
          case other =>
            resolve(other) match {
              case ValRef.Type(id) => id
              case prim            => builder.addValTypeDef(prim)
            }
        }
        visitingAliases -= alias.name
        val id = builder.addExportTypeEq(alias.name, defId)
        aliasIdx += alias.name -> id
        id
    }
  }

  private def exportFuncs(): Unit = {
    val resourceOrder = namedTypes.collect { case r: WitTypeDef.Resource => r.name }
    val resourceFuncs = endpoints.filter(_.name.isResourceMethod).sortBy { func =>
      val ord = resourceOrder.indexOf(func.name.resourceName.get)
      if (ord < 0) resourceOrder.size else ord
    }
    val plainFuncs = endpoints.filter(_.name.isFunction)

    val seenNames = mutable.Set.empty[String]
    for (func <- resourceFuncs ::: plainFuncs) {
      val exportName = WitFunctionName.wasmName(func.name)
      if (seenNames.add(exportName)) {
        val params = func.signature.params.map { p =>
          p.name -> resolve(p.tpe)
        }
        val result = func.signature.resultType.map(resolve)
        val ty = builder.addFuncType(params, result)
        builder.addExport(exportName, ExternType.Func(ty))
      }
    }
  }
}
