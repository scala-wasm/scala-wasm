package org.scalajs.linker.backend.webassembly.component

object Types {
  sealed trait Type
  sealed trait ValType extends Type
  sealed trait ExternType extends Type

  sealed trait FundamentalType extends ValType
  /** A specialized value types in Wasm Component Model.
   *
   *  Specialized value types are defined by expansion into the `fundamental value types`.
   *
   *  For example:
   *  - A `result` type is more than a variant - it represents success/failure and enables
   *    idiomatic error handling in source languages
   *  - `string` uses Unicode encodings while `list<char>` uses 4-byte char code points
   *  - `flags` uses bit-vectors while equivalent boolean field records use byte sequences
   *
   *  Note that, while Component Model defines `string` and `flags` as specialized value types,
   *  we do not mark them as specialized types, because they have distinct
   *  core Wasm representations distinct from their respective expansions in CanonicalABI.
   *
   *  @see
   *    [[https://github.com/WebAssembly/component-model/blob/main/design/mvp/Explainer.md#specialized-value-types]]
   */
  sealed trait SpecializedType extends ValType

  sealed abstract class PrimValType extends FundamentalType

  case object BoolType extends PrimValType
  case object U8Type   extends PrimValType
  case object U16Type extends PrimValType
  case object U32Type extends PrimValType
  case object U64Type extends PrimValType
  case object S8Type extends PrimValType
  case object S16Type extends PrimValType
  case object S32Type extends PrimValType
  case object S64Type extends PrimValType
  case object F32Type extends PrimValType
  case object F64Type extends PrimValType
  case object CharType extends PrimValType
  case object StringType extends PrimValType

  final case class ListType(elemType: ValType, length: Option[Int]) extends FundamentalType

  final case class FieldType(label: String, tpe: ValType)
  final case class RecordType(fields: List[FieldType]) extends FundamentalType

  final case class TupleType(ts: List[ValType]) extends SpecializedType

  final case class CaseType(label: String, tpe: Option[ValType])
  final case class VariantType(cases: List[CaseType]) extends FundamentalType
  final case class EnumType(labels: List[String]) extends SpecializedType
  final case class OptionType(tpe: ValType) extends SpecializedType
  final case class ResultType(
    ok: Option[ValType],
    error: Option[ValType]) extends SpecializedType

  final case class FlagsType(labels: List[String]) extends FundamentalType

  // final case class OwnType(rt: ResourceType) extends ValType
  // final case class BorrowType(rt: ResourceType) extends ValType

  // ExternTypes
  final case class FuncType(paramTypes: List[ValType], resultTypes: List[ValType])
}