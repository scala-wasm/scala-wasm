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
import org.scalajs.ir.{WitScope, WitTypeDef}
import org.scalajs.ir.WasmInterfaceTypes.{ExternType => _, _}

import Components._
import ComponentWorld.{namedTypeRefs, valTypeRefs}

private[backend] final class ComponentBuilder {
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

  def addFuncType(signature: FuncType, resolve: ValType => ValRef): TypeID = {
    val params = signature.params.map(p => p.name -> resolve(p.tpe))
    val result = signature.resultType.map(resolve)
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

  def addAliasExport(instance: InstanceID, name: String): TypeID =
    addType(Decl.AliasExport(_, instance, name))

  def addAliasOuter(outer: TypeID): TypeID =
    addType(Decl.AliasOuter(_, outer))

  def addExportResource(name: String): TypeID =
    addType(Decl.ExportResource(_, name))

  def addExportTypeEq(name: String, ty: TypeID): TypeID =
    addType(Decl.ExportTypeEq(_, name, ty))

  def addInstanceImport(name: String, ty: TypeID): InstanceID = {
    val id = new InstanceID
    declBuf += Decl.Import(name, ExternType.Instance(ty), Some(id))
    id
  }

  def addImport(name: String, externtype: ExternType): Unit = {
    externtype match {
      case ExternType.Instance(_) =>
        throw new AssertionError("instance import needs InstanceID; use addInstanceImport")
      case _ =>
        declBuf += Decl.Import(name, externtype, None)
    }
  }

  def addExport(name: String, externtype: ExternType): Unit =
    declBuf += Decl.Export(name, externtype)

  def build(packageName: String, worldName: String): Component =
    Component(packageName, worldName, decls())

  private def decls(): List[Decl] = declBuf.toList
}

private[backend] object ComponentBuilder {

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
  private def encodeWorld(world: ComponentWorld): List[Decl] = {
    val builder = new ComponentBuilder
    val instanceIds = mutable.Map.empty[WitScope.Interface, InstanceID]
    val aliases = mutable.Map.empty[(WitScope, String), TypeID]
    val typeDefByClass = world.typeDefByClass

    def aliasExports(uses: List[(WitScope.Interface, String)]): Unit = {
      for ((owner, typeName) <- uses) {
        val inst = instanceIds.getOrElse(owner,
            throw new AssertionError(
                s"missing imported dependency interface ${owner.witId} " +
                s"before aliasing $typeName"))
        aliases((owner, typeName)) = builder.addAliasExport(inst, typeName)
      }
    }

    /** WIT `use other.{T}` in `scope`. */
    def witUses(scope: WitScope): List[(WitScope.Interface, String)] = {
      typeRefsIn(world.typesFor(scope), world.endpointsFor(scope),
          typeDefByClass).collect {
        case (owner: WitScope.Interface, typeName) if owner != scope =>
          (owner, typeName)
      }.distinct.sorted
    }

    /** WIT `use other.{T}` on this item that are not yet in `aliases`. */
    def usesOf(item: ComponentWorld.WorldItem): List[(WitScope.Interface, String)] = {
      item match {
        case ComponentWorld.WorldItem.Interface(iface) =>
          witUses(iface).filter(u => !aliases.contains(u))
        case ComponentWorld.WorldItem.Inline(inline) =>
          witUses(inline).filter(u => !aliases.contains(u))
        case ComponentWorld.WorldItem.Func(_) =>
          Nil
      }
    }

    def addImportedItem(item: ComponentWorld.WorldItem): Unit = {
      item match {
        case ComponentWorld.WorldItem.Interface(iface) =>
          val instanceTy = encodeInstanceType(
              iface, world.endpointsFor(iface), world.typesFor(iface), aliases,
              typeDefByClass)
          instanceIds(iface) =
            builder.addInstanceImport(iface.witId, builder.addInstanceType(instanceTy))
        case ComponentWorld.WorldItem.Inline(inline) =>
          val instanceTy = encodeInstanceType(
              inline, world.endpointsFor(inline), world.typesFor(inline), aliases,
              typeDefByClass)
          builder.addInstanceImport(inline.name, builder.addInstanceType(instanceTy))
        case ComponentWorld.WorldItem.Func(func) =>
          val resolve = (tpe: ValType) =>
            resolveValRef(builder, tpe, mutable.Map.empty)
          val ty = builder.addFuncType(func.signature, resolve)
          builder.addImport(WitFunctionName.wasmName(func.name), ExternType.Func(ty))
      }
    }

    def addExportedItem(item: ComponentWorld.WorldItem): Unit = {
      item match {
        case ComponentWorld.WorldItem.Interface(iface) =>
          val instanceTy = encodeInstanceType(
              iface, world.endpointsFor(iface), world.typesFor(iface), aliases,
              typeDefByClass)
          builder.addExport(iface.witId,
              ExternType.Instance(builder.addInstanceType(instanceTy)))
        case ComponentWorld.WorldItem.Inline(inline) =>
          val instanceTy = encodeInstanceType(
              inline, world.endpointsFor(inline), world.typesFor(inline), aliases,
              typeDefByClass)
          builder.addExport(inline.name,
              ExternType.Instance(builder.addInstanceType(instanceTy)))
        case ComponentWorld.WorldItem.Func(func) =>
          val resolve = (tpe: ValType) =>
            resolveValRef(builder, tpe, mutable.Map.empty)
          val ty = builder.addFuncType(func.signature, resolve)
          builder.addExport(WitFunctionName.wasmName(func.name), ExternType.Func(ty))
      }
    }

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
    //
    // Same `aliases` table for imports and exports: later items look up $t
    // already put there (no second alias export). Alias export can only name
    // imported instances, so imports run first.
    for (item <- world.imports) {
      aliasExports(usesOf(item))
      addImportedItem(item)
    }
    for (item <- world.exports) {
      aliasExports(usesOf(item))
      addExportedItem(item)
    }
    builder.decls()
  }

  private def typeRefsIn(namedTypes: List[WitTypeDef],
      endpoints: List[ComponentWorld.Endpoint],
      typeDefByClass: Map[ClassName, WitTypeDef]): List[(WitScope, String)] = {
    (for {
      named <- namedTypes
      ref <- namedTypeRefs(named, typeDefByClass)
    } yield ref) ++ (for {
      func <- endpoints
      tpe <- func.signature.params.map(_.tpe) ++ func.signature.resultType
      ref <- valTypeRefs(tpe, typeDefByClass)
    } yield ref)
  }

  private def encodeInstanceType(scope: WitScope, endpoints: List[ComponentWorld.Endpoint],
      namedTypes: List[WitTypeDef],
      worldAliases: collection.Map[(WitScope, String), TypeID],
      typeDefByClass: Map[ClassName, WitTypeDef]): List[Decl] = {
    val builder = new ComponentBuilder
    val localTypeIdx = mutable.Map.empty[ClassName, TypeID]

    def resolve(tpe: ValType): ValRef =
      resolveValRef(builder, tpe, localTypeIdx)

    // Step 1: Index WIT `use other.{T}`. Pull world's
    // `(alias export ...)` from `worldAliases` using `alias outer`.
    // `alias outer`, then export the local name `typeName`.
    //   (alias outer 1 $t (type $error))
    //   (export "error" (type $error))
    val usedFromOtherIfaces = typeRefsIn(namedTypes, endpoints, typeDefByClass).distinct.filter {
      case (owner, _) => owner != scope
    }
    for ((owner, typeName) <- usedFromOtherIfaces) {
      val outerId = worldAliases.getOrElse((owner, typeName),
          throw new AssertionError(
              s"missing aliased foreign type ${WitScope.importModuleName(owner)}/$typeName"))
      val aliased = builder.addAliasOuter(outerId)
      val className = typeDefByClass.find {
        case (_, td) => td.scope == owner && td.name == typeName
      }.map(_._1).getOrElse {
        throw new AssertionError(
            s"missing WitTypeDef for ${WitScope.importModuleName(owner)}/$typeName")
      }
      localTypeIdx(className) = builder.addExportTypeEq(typeName, aliased)
    }

    // Step 2: Index named types defined in this interface (record, variant, resouce, and etc)
    for (named <- namedTypes) {
      named match {
        case _: WitTypeDef.Resource =>
          localTypeIdx(named.className) = builder.addExportResource(named.name)
        case other =>
          val defId = addNamedTypeBody(builder, other, resolve)
          localTypeIdx(other.className) = builder.addExportTypeEq(other.name, defId)
      }
    }

    // Step 3: Export funcs
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
        val ty = builder.addFuncType(func.signature, resolve)
        builder.addExport(exportName, ExternType.Func(ty))
      }
    }

    builder.decls()
  }

  private def addNamedTypeBody(builder: ComponentBuilder, named: WitTypeDef,
      resolve: ValType => ValRef): TypeID = {
    named match {
      case WitTypeDef.Record(_, _, _, fields) =>
        builder.addRecord(fields.map(f => f.name -> resolve(f.tpe)))
      case WitTypeDef.Variant(_, _, _, cases) =>
        builder.addVariant(cases.map(c => c.name -> c.tpe.map(resolve)))
      case WitTypeDef.Enum(_, _, _, cases) =>
        builder.addEnum(cases.map(_.name))
      case WitTypeDef.Flags(_, _, _, names) =>
        builder.addFlags(names)
      case _: WitTypeDef.Resource =>
        throw new AssertionError("resource body is exported as sub resource")
    }
  }

  private def resolveValRef(builder: ComponentBuilder, tpe: ValType,
      localTypeIdx: mutable.Map[ClassName, TypeID]): ValRef = {
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
        ValRef.Type(builder.addList(resolveValRef(builder, elem, localTypeIdx)))
      case ListType(elem, Some(len)) =>
        ValRef.Type(builder.addFixedList(
            resolveValRef(builder, elem, localTypeIdx), len))

      case TupleType(ts) =>
        ValRef.Type(builder.addTuple(
            ts.map(resolveValRef(builder, _, localTypeIdx))))

      case OptionType(inner) =>
        ValRef.Type(builder.addOption(
            resolveValRef(builder, inner, localTypeIdx)))

      case ResultType(ok, err) =>
        ValRef.Type(builder.addResult(
            ok.map(resolveValRef(builder, _, localTypeIdx)),
            err.map(resolveValRef(builder, _, localTypeIdx))))

      case RecordTypeRef(className) =>
        ValRef.Type(localTypeIdx(className))
      case VariantTypeRef(className) =>
        ValRef.Type(localTypeIdx(className))
      case EnumTypeRef(className) =>
        ValRef.Type(localTypeIdx(className))
      case FlagsTypeRef(className) =>
        ValRef.Type(localTypeIdx(className))

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
