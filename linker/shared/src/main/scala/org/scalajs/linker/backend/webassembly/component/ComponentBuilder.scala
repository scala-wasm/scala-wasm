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
import ComponentWorld.{endpointTypeRefs, namedTypeRefs}

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
  private def encodeWorld(world: ComponentWorld): List[Decl] =
    new WorldEncoder(world).encode()

  /** Encode one WIT world into componenttype decls. */
  private final class WorldEncoder(world: ComponentWorld) {
    private val builder = new ComponentBuilder
    private val typeDefByClass = world.typeDefByClass
    private val emittedAliases = mutable.Set.empty[ClassName]

    private val instanceIds: Map[WitScope.Interface, InstanceID] = {
      world.imports.collect {
        // We don't pre-allocate instance IDs for inline blocks or funcs, because WIT cannot
        // import types/functions defined in inline interface.
        case ComponentWorld.WorldItem.Interface(iface) =>
          iface -> new InstanceID
      }.toMap
    }

    private val typeIds: Map[(WitScope, String), TypeID] = {
      // World-level named types and types imported from other interfaces.
      val fromWorldTypes = world.imports.collect {
        case ComponentWorld.WorldItem.Type(td) => (td.scope, td.name)
      }
      val uses = (world.imports ++ world.exports).flatMap(foreignClassNamesUsedBy).map {
        className =>
          val td = typeDefByClass(className)
          (td.scope, td.name)
      }
      (fromWorldTypes ++ uses).distinct.map(k => k -> new TypeID).toMap
    }

    private val typeIdsByClass: Map[ClassName, TypeID] = {
      typeDefByClass.flatMap { case (className, td) =>
        typeIds.get((td.scope, td.name)).map(className -> _)
      }
    }

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
            // `(import "ns:pkg/iface" (instance $id ...))`
          case ComponentWorld.WorldItem.Interface(iface) =>
            // alias export imported types
            // e.g. (alias export $error "error" (type $t))
            aliasExports(foreignClassNamesUsedBy(item))
            builder.addInstanceImport(iface.witId, instanceType(iface), instanceIds(iface))
            // `(import "name" (instance ...))`
          case ComponentWorld.WorldItem.Inline(inline) =>
            aliasExports(foreignClassNamesUsedBy(item))
            builder.addInstanceImport(inline.name, instanceType(inline))
            // `(import "f" (func ...))`
          case ComponentWorld.WorldItem.Func(func) =>
            aliasExports(foreignClassNamesUsedBy(item))
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
            aliasExports(foreignClassNamesUsedBy(item))
            builder.addExport(iface.witId, ExternType.Instance(instanceType(iface)))
            // `(export "name" (instance ...))`
          case ComponentWorld.WorldItem.Inline(inline) =>
            aliasExports(foreignClassNamesUsedBy(item))
            builder.addExport(inline.name, ExternType.Instance(instanceType(inline)))
            // `(export "f" (func ...))`
          case ComponentWorld.WorldItem.Func(func) =>
            aliasExports(foreignClassNamesUsedBy(item))
            builder.addExport(WitFunctionName.wasmName(func.name),
                ExternType.Func(bareFunc(func)))
          case _: ComponentWorld.WorldItem.Type =>
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
              val body = addNamedTypeBody(builder, other, typeIdsByClass)
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
      emittedAliases += named.className
      builder.addImportTypeEq(id, named.name, aliased)
    }

    private def topologicalSortWorldItems(
        items: List[ComponentWorld.WorldItem]): List[ComponentWorld.WorldItem] = {
      val interfaceItems = items.collect {
        case i @ ComponentWorld.WorldItem.Interface(iface) => iface -> i
      }.toMap
      val typeItems = items.collect {
        case t @ ComponentWorld.WorldItem.Type(named) => named.className -> t
      }.toMap
      val ordered = mutable.LinkedHashSet.empty[ComponentWorld.WorldItem]
      val visiting = mutable.Set.empty[ComponentWorld.WorldItem]

      def visit(item: ComponentWorld.WorldItem): Unit = {
        if (ordered.contains(item)) {
          // skip
        } else if (!visiting.add(item)) {
          throw new AssertionError("cyclic world item dependency")
        } else {
          item match {
            case ComponentWorld.WorldItem.Type(named) =>
              named.scope match {
                case iface: WitScope.Interface =>
                  interfaceItems.get(iface).foreach(visit)
                case _ =>
              }
              for (className <- namedTypeRefs(named) if className != named.className) {
                typeItems.get(className).foreach(visit)
                interfaceOf(className).flatMap(interfaceItems.get).foreach(visit)
              }
            case _ =>
              for (className <- foreignClassNamesUsedBy(item)) {
                typeItems.get(className).foreach(visit)
                interfaceOf(className).flatMap(interfaceItems.get).foreach(visit)
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
    private def aliasExports(classNames: Iterable[ClassName]): Unit = {
      for (className <- classNames if emittedAliases.add(className)) {
        val td = typeDefByClass(className)
        val owner = interfaceOf(className).getOrElse(
            throw new AssertionError(
                s"foreign type ${className.nameString} is not from a package interface"))
        val inst = instanceIds.getOrElse(owner,
            throw new AssertionError(
                s"missing imported dependency interface ${owner.witId} " +
                s"before aliasing ${td.name}"))
        builder.addAliasExport(typeIdsByClass(className), inst, td.name)
      }
    }

    /** `ClassName`s of types from other interfaces that `item` references. */
    private def foreignClassNamesUsedBy(item: ComponentWorld.WorldItem): List[ClassName] = {
      item match {
        case ComponentWorld.WorldItem.Interface(iface) =>
          filterForeignClassNames(iface, scopeTypeRefs(iface))
        case ComponentWorld.WorldItem.Inline(inline) =>
          filterForeignClassNames(inline, scopeTypeRefs(inline))
        case ComponentWorld.WorldItem.Func(func) =>
          filterForeignClassNames(func.scope, endpointTypeRefs(func))
        case ComponentWorld.WorldItem.Type(named) =>
          filterForeignClassNames(named.scope, namedTypeRefs(named))
      }
    }

    /** Collect referenced types from the given scope. */
    private def scopeTypeRefs(scope: WitScope): List[ClassName] = {
      world.typesFor(scope).flatMap(namedTypeRefs) ++
      world.endpointsFor(scope).flatMap(endpointTypeRefs)
    }

    /** Filter out classes defined in this scope */
    private def filterForeignClassNames(scope: WitScope,
        classNames: Iterable[ClassName]): List[ClassName] = {
      classNames.filter { className =>
        interfaceOf(className).exists(_ != scope)
      }.toList.distinct
    }

    private def interfaceOf(className: ClassName): Option[WitScope.Interface] = {
      typeDefByClass(className).scope match {
        case iface: WitScope.Interface => Some(iface)
        case _                         => None
      }
    }

    private def bareFunc(func: ComponentWorld.Endpoint): TypeID = {
      val params = func.signature.params.map { p =>
        p.name -> resolveValRef(builder, p.tpe, typeIdsByClass)
      }
      val result = func.signature.resultType.map(resolveValRef(builder, _, typeIdsByClass))
      builder.addFuncType(params, result)
    }

    private def instanceType(scope: WitScope): TypeID = {
      builder.addInstanceType(encodeInstanceType(
          scope, world.endpointsFor(scope), world.typesFor(scope), typeIdsByClass,
          typeDefByClass))
    }
  }

  /** Build decls for one WIT interface/inline as a component instance type.
   *
   *  Emits, in order:
   *   1. Types defined in another interfaces: `(alias outer ...)` + `(export "T" (type ...))`
   *   2. Named types defined in this scope in topological order.
   *   3. Exported funcs.
   *
   *  @param scope This interface or inline scope
   *  @param endpoints Functions belong to this `scope`
   *  @param namedTypes Named types defined in `scope`
   *  @param worldTypeIds World-referencable types keyed by IR class
   *  @param typeDefByClass All named types in the world, for resolving type refs
   */
  private[component] def encodeInstanceType(scope: WitScope,
      endpoints: List[ComponentWorld.Endpoint],
      namedTypes: List[WitTypeDef],
      worldTypeIds: Map[ClassName, TypeID],
      typeDefByClass: Map[ClassName, WitTypeDef]): List[Decl] = {
    val builder = new ComponentBuilder
    var localTypeIdx = Map.empty[ClassName, TypeID]

    // Step 1: Index WIT `use other.{T}`. Pull world's
    // `(alias export ...)` from `worldTypeIds` using `alias outer`.
    // `alias outer`, then export the local name `typeName`.
    //   (alias outer 1 $t (type $error))
    //   (export "error" (type $error))
    val usedFromOtherIfaces = (
      namedTypes.flatMap(namedTypeRefs) ++ endpoints.flatMap(endpointTypeRefs)
    ).distinct.filter { className =>
      typeDefByClass(className).scope != scope
    }
    for (className <- usedFromOtherIfaces) {
      val outerId = worldTypeIds.getOrElse(className,
          throw new AssertionError(
              s"missing aliased foreign type for ${className.nameString}"))
      val aliased = builder.addAliasOuter(outerId)
      localTypeIdx = localTypeIdx + (className ->
        builder.addExportTypeEq(typeDefByClass(className).name, aliased))
    }

    // Step 2: Index named types defined in this interface (record, variant, resouce, and etc)
    // Topological sort so field referencess can resolve when bodies are encoded.
    for (named <- topologicalSortNamedTypes(namedTypes)) {
      named match {
        case _: WitTypeDef.Resource =>
          localTypeIdx = localTypeIdx + (named.className -> builder.addExportResource(named.name))
        case other =>
          val defId = addNamedTypeBody(builder, other, localTypeIdx)
          localTypeIdx =
            localTypeIdx + (other.className -> builder.addExportTypeEq(other.name, defId))
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
        val params = func.signature.params.map { p =>
          p.name -> resolveValRef(builder, p.tpe, localTypeIdx)
        }
        val result = func.signature.resultType.map(resolveValRef(builder, _, localTypeIdx))
        val ty = builder.addFuncType(params, result)
        builder.addExport(exportName, ExternType.Func(ty))
      }
    }

    builder.decls()
  }

  /** Local named types in dependency order. */
  private def topologicalSortNamedTypes(namedTypes: List[WitTypeDef]): List[WitTypeDef] = {
    val byClass = namedTypes.map(t => t.className -> t).toMap
    val ordered = mutable.LinkedHashSet.empty[ClassName]
    val visiting = mutable.Set.empty[ClassName]

    def visit(className: ClassName): Unit = {
      if (ordered.contains(className)) {
        // skip
      } else if (!visiting.add(className)) {
        throw new AssertionError(
            s"cyclic dependency ${className.nameString}")
      } else {
        for (dep <- namedTypeRefs(byClass(className))) {
          if (byClass.contains(dep))
            visit(dep)
        }
        visiting -= className
        ordered += className
      }
    }

    namedTypes.foreach(t => visit(t.className))
    ordered.iterator.map(byClass).toList
  }

  private def addNamedTypeBody(builder: ComponentBuilder, named: WitTypeDef,
      localTypeIdx: Map[ClassName, TypeID]): TypeID = {
    named match {
      case WitTypeDef.Record(_, _, _, fields) =>
        builder.addRecord(fields.map { f =>
          f.name -> resolveValRef(builder, f.tpe, localTypeIdx)
        })
      case WitTypeDef.Variant(_, _, _, cases) =>
        builder.addVariant(cases.map { c =>
          c.name -> c.tpe.map(resolveValRef(builder, _, localTypeIdx))
        })
      case WitTypeDef.Enum(_, _, _, cases) =>
        builder.addEnum(cases.map(_.name))
      case WitTypeDef.Flags(_, _, _, names) =>
        builder.addFlags(names)
      case _: WitTypeDef.Resource =>
        throw new AssertionError("resource body is exported as sub resource")
    }
  }

  private def resolveValRef(builder: ComponentBuilder, tpe: ValType,
      localTypeIdx: Map[ClassName, TypeID]): ValRef = {
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
