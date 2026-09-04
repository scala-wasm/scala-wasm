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
import org.scalajs.ir.ResourceOwnership
import org.scalajs.ir.Trees.WitFunctionName
import org.scalajs.ir.{WitScope, WitTypeDef, WitNamedTypeDef, WitAliasDef}
import org.scalajs.ir.WasmInterfaceTypes.{ExternType => _, _}

import Components._
import ComponentWorld._

final class ComponentBuilder {
  private val declBuf = mutable.ListBuffer.empty[Decl]

  private def addType(mk: TypeID => TypeDecl): TypeID = {
    val id = new TypeID
    declBuf += mk(id)
    id
  }

  def addComponentType(nested: List[Decl]): TypeID =
    addType(Decl.ComponentType(_, nested))

  def addInstanceType(nested: List[Decl]): TypeID =
    addType(Decl.InstanceType(_, nested))

  def addFuncType(params: List[(String, ValRef)],
      result: scala.Option[ValRef]): TypeID = {
    addType(Decl.FuncType(_, params, result))
  }

  def addRecord(fields: List[(String, ValRef)]): TypeID =
    addType(Decl.Record(_, fields))

  def addVariant(cases: List[(String, scala.Option[ValRef])]): TypeID =
    addType(Decl.Variant(_, cases))

  def addEnum(cases: List[String]): TypeID =
    addType(Decl.Enum(_, cases))

  def addFlags(names: List[String]): TypeID =
    addType(Decl.Flags(_, names))

  def addList(elem: ValRef): TypeID =
    addType(Decl.ListTy(_, elem))

  def addFixedList(elem: ValRef, len: Int): TypeID =
    addType(Decl.FixedList(_, elem, len))

  def addTuple(elems: List[ValRef]): TypeID =
    addType(Decl.Tuple(_, elems))

  def addOption(inner: ValRef): TypeID =
    addType(Decl.OptionTy(_, inner))

  def addResult(ok: scala.Option[ValRef], err: scala.Option[ValRef]): TypeID =
    addType(Decl.ResultTy(_, ok, err))

  def addOwn(resource: TypeID): TypeID =
    addType(Decl.Own(_, resource))

  def addBorrow(resource: TypeID): TypeID =
    addType(Decl.Borrow(_, resource))

  def addValTypeDef(v: ValRef): TypeID =
    addType(Decl.ValTypeDef(_, v))

  def addAliasExport(instance: InstanceID, name: String): TypeID =
    addType(Decl.AliasExport(_, instance, name))

  def addAliasExport(id: TypeID, instance: InstanceID, name: String): Unit =
    declBuf += Decl.AliasExport(id, instance, name)

  def addAliasOuter(outer: TypeID): TypeID =
    addType(Decl.AliasOuter(_, outer))

  def addExportResource(name: String): TypeID =
    addType(Decl.ExportResource(_, name))

  def addExportTypeEq(name: String, ty: TypeID): TypeID =
    addType(Decl.ExportTypeEq(_, name, ty))

  def addImportTypeEq(id: TypeID, name: String, ty: TypeID): Unit =
    declBuf += Decl.ImportTypeEq(id, name, ty)

  def addImportTypeResource(id: TypeID, name: String): Unit =
    declBuf += Decl.ImportTypeResource(id, name)

  def addInstanceImport(name: String, ty: TypeID, id: InstanceID): Unit =
    declBuf += Decl.Import(name, ExternType.Instance(ty), Some(id))

  def addInstanceImport(name: String, ty: TypeID): InstanceID = {
    val id = new InstanceID
    addInstanceImport(name, ty, id)
    id
  }

  def addImport(name: String, externtype: ExternType): Unit = {
    externtype match {
      case ExternType.Instance(_) =>
        throw new AssertionError("instance import needs InstanceID. use addInstanceImport")
      case _ =>
        declBuf += Decl.Import(name, externtype, None)
    }
  }

  def addExport(name: String, externtype: ExternType): Unit =
    declBuf += Decl.Export(name, externtype)

  def build(packageName: String, worldName: String): Component =
    Component(packageName, worldName, decls())

  private[component] def decls(): List[Decl] = declBuf.toList
}

object ComponentBuilder {

  /** Encode wit-component metadata from a reachable WIT world.
   *
   *  Currently we generate only the subset that `wit-component` /
   *  `wasm-tools component new` reads from a core-module custom section
   *  whose name starts with `component-type`.
   *
   *  > //! Currently the encoding of this custom section is itself a component. The
   *  > //! component has a single export which is a component type which represents the
   *  > //! `world` that was bound during bindings generation. This single export is
   *  > //! used to decode back into a `Resolve` with a WIT representation.
   *  > //! Currently the component additionally has a custom section named
   *  > //! `wit-component-encoding` (see `CUSTOM_SECTION_NAME`). This section is
   *  > //! currently defined as 2 bytes:
   *  > [[https://github.com/bytecodealliance/wasm-tools/blob/a2b006b52915754823c01495c34f4ec247efc9b4/crates/wit-component/src/metadata.rs#L1-L42]]
   *
   *  So, the component binary in the custom section contains only 3 sections:
   *  - type section: contains a single `componenttype` that represents world.
   *  - export section: export that world type
   *  - `wit-component-encoding` custom section: version + string encoding
   *
   *  {{{
   *  (component
   *    (type $0 (component <world decls>))
   *    (export "pkg/world" (component (type $0))))
   *    (custom section ...)
   *  }}}
   *
   *  @see
   *    [[https://github.com/bytecodealliance/wasm-tools/blob/ef6cb3863030c582f0e21fa9391c435ea2569c0a/crates/wit-component/README.md?plain=1#L100-L180]]
   *
   *  @see
   *    [[https://github.com/bytecodealliance/wasm-tools/blob/a2b006b52915754823c01495c34f4ec247efc9b4/crates/wit-component/src/metadata.rs#L1-L42]]
   */
  def fromWorld(world: ComponentWorld): Component = {
    val root = new ComponentBuilder
    val worldTy = root.addComponentType(encodeWorld(world))
    root.addExport(s"${world.packageName}/${world.worldName}",
        ExternType.Component(worldTy))
    // custom section is written in ComponentBinaryBuilder
    root.build(world.packageName, world.worldName)
  }

  /** Encode world-level imports, then exports. */
  private def encodeWorld(world: ComponentWorld): List[Decl] =
    new WorldEncoder(world).encode()

  /** Build decls for one WIT interface/inline as a component instance type.
   *
   *  Emits, in order:
   *   1. `use` of types/aliases from other interfaces: `(alias outer ...)` + `(export "T" (type ...))`
   *   2. Export local named types and aliases
   *   3. Exported funcs.
   *
   *  @param scope This interface or inline scope
   *  @param endpoints Functions belong to this `scope`
   *  @param namedTypes Named types defined in `scope`
   *  @param worldTypeIds World-referencable types/aliases keyed by `(scope, name)`
   *  @param typeDefByClass All named types in the world, for resolving type refs
   */
  private[component] def encodeInstanceType(scope: WitScope,
      endpoints: List[ComponentWorld.Endpoint],
      namedTypes: List[WitNamedTypeDef],
      definedAliases: List[WitAliasDef],
      worldTypeIds: Map[(WitScope, String), TypeID],
      typeDefByClass: Map[ClassName, WitNamedTypeDef]): List[Decl] = {
    new InstanceTypeEncoder(scope, endpoints, namedTypes, definedAliases,
        worldTypeIds, typeDefByClass).encode()
  }

  /** Local named types in dependency order. */
  private[component] def topologicalSortNamedTypes(namedTypes: List[WitNamedTypeDef],
      localAliases: List[WitAliasDef] = Nil): List[WitNamedTypeDef] = {
    val byClass = namedTypes.map(t => t.className -> t).toMap
    val aliasByName = localAliases.map(a => a.name -> a).toMap
    val ordered = mutable.LinkedHashSet.empty[ClassName]
    val visiting = mutable.Set.empty[ClassName]

    def visit(className: ClassName): Unit = {
      if (ordered.contains(className)) {
        // skip
      } else if (!visiting.add(className)) {
        throw new AssertionError(
            s"cyclic dependency ${className.nameString}")
      } else {
        val td = byClass(className)
        for (dep <- namedTypeRefs(td)) {
          if (byClass.contains(dep))
            visit(dep)
        }
        for (a <- namedTypeAliasRefs(td) if a.scope == td.scope) {
          for (dep <- valTypeRefs(aliasByName(a.name).target) if byClass.contains(dep))
            visit(dep)
        }
        visiting -= className
        ordered += className
      }
    }

    namedTypes.foreach(t => visit(t.className))
    ordered.iterator.map(byClass).toList
  }

  /** Encode record/variant/enum/flags body.
   *
   *  Field/case types must be TypeIDs already in the caller's table (world imports
   *  vs instance exports). WorldEncoder and InstanceTypeEncoder each pass their
   *  own `resolve` for that.
   */
  private[component] def addNamedTypeBody(builder: ComponentBuilder, named: WitNamedTypeDef,
      resolve: ValType => ValRef): TypeID = {
    named match {
      case WitTypeDef.Record(_, _, _, fields) =>
        builder.addRecord(fields.map { f =>
          f.name -> resolve(f.tpe)
        })
      case WitTypeDef.Variant(_, _, _, cases) =>
        builder.addVariant(cases.map { c =>
          c.name -> c.tpe.map(resolve)
        })
      case WitTypeDef.Enum(_, _, _, cases) =>
        builder.addEnum(cases.map(_.name))
      case WitTypeDef.Flags(_, _, _, names) =>
        builder.addFlags(names)
      case _: WitTypeDef.Resource =>
        throw new AssertionError("resource body is exported as sub resource")
    }
  }

  /** Convert IR `ValType` to component-model encoding (`ValRef`). */
  private[component] def resolveValRef(builder: ComponentBuilder, tpe: ValType,
      localTypeIdx: Map[ClassName, TypeID],
      alias: (WitScope, String) => ValRef): ValRef = {
    tpe match {
      case BoolType   => ValRef.Bool
      case S8Type     => ValRef.S8
      case U8Type     => ValRef.U8
      case S16Type    => ValRef.S16
      case U16Type    => ValRef.U16
      case S32Type    => ValRef.S32
      case U32Type    => ValRef.U32
      case S64Type    => ValRef.S64
      case U64Type    => ValRef.U64
      case F32Type    => ValRef.F32
      case F64Type    => ValRef.F64
      case CharType   => ValRef.Char
      case StringType => ValRef.String

      case ListType(elem, None) =>
        ValRef.Type(builder.addList(resolveValRef(builder, elem, localTypeIdx, alias)))
      case ListType(elem, Some(len)) =>
        ValRef.Type(builder.addFixedList(
            resolveValRef(builder, elem, localTypeIdx, alias), len))

      case TupleType(ts, _) =>
        ValRef.Type(builder.addTuple(
            ts.map(resolveValRef(builder, _, localTypeIdx, alias))))

      case OptionType(inner) =>
        ValRef.Type(builder.addOption(
            resolveValRef(builder, inner, localTypeIdx, alias)))

      case ResultType(ok, err, _) =>
        ValRef.Type(builder.addResult(
            ok.map(resolveValRef(builder, _, localTypeIdx, alias)),
            err.map(resolveValRef(builder, _, localTypeIdx, alias))))

      case RecordTypeRef(className) =>
        ValRef.Type(localTypeIdx(className))
      case VariantTypeRef(className) =>
        ValRef.Type(localTypeIdx(className))
      case EnumTypeRef(className) =>
        ValRef.Type(localTypeIdx(className))
      case FlagsTypeRef(className) =>
        ValRef.Type(localTypeIdx(className))
      case AliasTypeRef(scope, name, _) =>
        alias(scope, name)

      case ResourceType(className, ownership) =>
        val resId = localTypeIdx(className)
        ownership match {
          case ResourceOwnership.Own =>
            ValRef.Type(builder.addOwn(resId))
          case ResourceOwnership.Borrow =>
            ValRef.Type(builder.addBorrow(resId))
        }
    }
  }
}
