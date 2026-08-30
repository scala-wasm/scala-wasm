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

/** Component Model types and their structure (wit-component metadata subset).
 *
 *  @see
 *    [[https://github.com/WebAssembly/component-model/blob/main/design/mvp/Binary.md]]
 *
 *  @see
 *    [[https://github.com/bytecodealliance/wasm-tools/blob/main/crates/wit-component/src/metadata.rs]]
 */
object Components {

  final class TypeID
  final class InstanceID

  /** A Component Model `valtype`. */
  sealed abstract class ValRef

  object ValRef {
    case object Bool extends ValRef
    case object S8 extends ValRef
    case object U8 extends ValRef
    case object S16 extends ValRef
    case object U16 extends ValRef
    case object S32 extends ValRef
    case object U32 extends ValRef
    case object S64 extends ValRef
    case object U64 extends ValRef
    case object F32 extends ValRef
    case object F64 extends ValRef
    case object Char extends ValRef
    case object String extends ValRef
    final case class Type(id: TypeID) extends ValRef
  }

  /** A Component Model `externtype`. */
  sealed abstract class ExternType

  object ExternType {
    final case class Func(ty: TypeID) extends ExternType
    final case class Instance(ty: TypeID) extends ExternType
    final case class Component(ty: TypeID) extends ExternType
  }

  /** A Component Model `componentdecl` / `instancedecl`. */
  sealed abstract class Decl

  sealed abstract class TypeDecl extends Decl {
    def id: TypeID
  }

  object Decl {
    final case class ComponentType(id: TypeID, nested: List[Decl]) extends TypeDecl
    final case class InstanceType(id: TypeID, nested: List[Decl]) extends TypeDecl

    final case class FuncType(id: TypeID, params: List[(String, ValRef)],
        result: scala.Option[ValRef])
        extends TypeDecl

    final case class Record(id: TypeID, fields: List[(String, ValRef)]) extends TypeDecl

    final case class Variant(id: TypeID, cases: List[(String, scala.Option[ValRef])])
        extends TypeDecl

    final case class Enum(id: TypeID, cases: List[String]) extends TypeDecl
    final case class Flags(id: TypeID, names: List[String]) extends TypeDecl
    final case class ListTy(id: TypeID, elem: ValRef) extends TypeDecl
    final case class FixedList(id: TypeID, elem: ValRef, len: Int) extends TypeDecl
    final case class Tuple(id: TypeID, elems: List[ValRef]) extends TypeDecl
    final case class OptionTy(id: TypeID, inner: ValRef) extends TypeDecl

    final case class ResultTy(id: TypeID, ok: scala.Option[ValRef],
        err: scala.Option[ValRef])
        extends TypeDecl

    final case class Own(id: TypeID, resource: TypeID) extends TypeDecl
    final case class Borrow(id: TypeID, resource: TypeID) extends TypeDecl

    /** `(type $id <valtype>)` */
    final case class ValTypeDef(id: TypeID, v: ValRef) extends TypeDecl

    /** `(alias export $instance "name" (type ...))` */
    final case class AliasExport(id: TypeID, instance: InstanceID, name: String) extends TypeDecl

    /** `(alias outer $enclosing $type (type ...))` */
    final case class AliasOuter(id: TypeID, outer: TypeID) extends TypeDecl

    final case class ExportResource(id: TypeID, name: String) extends TypeDecl
    final case class ExportTypeEq(id: TypeID, name: String, ty: TypeID) extends TypeDecl

    /** `(import "name" (type (eq $ty)))` world-level named type. */
    final case class ImportTypeEq(id: TypeID, name: String, ty: TypeID) extends TypeDecl

    /** `(import "name" (type (sub resource)))` world-level resource. */
    final case class ImportTypeResource(id: TypeID, name: String) extends TypeDecl

    final case class Import(name: String, externtype: ExternType,
        instance: scala.Option[InstanceID])
        extends Decl

    final case class Export(name: String, externtype: ExternType) extends Decl
  }

  /** A wit-component metadata component. */
  final case class Component(
      packageName: String,
      worldName: String,
      decls: List[Decl]
  )
}
