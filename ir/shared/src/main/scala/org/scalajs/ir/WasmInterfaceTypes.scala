package org.scalajs.ir

import Names._
import org.scalajs.ir.{Types => jstpe}

object WasmInterfaceTypes {
  sealed trait WasmInterfaceType
  sealed trait ValType extends WasmInterfaceType {
    def toIRType(): jstpe.Type
  }
  sealed trait ExternType extends WasmInterfaceType

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

  case object VoidType extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.VoidType
  }
  case object BoolType extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.BooleanType
  }
  case object U8Type   extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.ByteType
  }
  case object U16Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.ShortType
  }
  case object U32Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.IntType
  }
  case object U64Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.LongType
  }
  case object S8Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.ByteType
  }
  case object S16Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.ShortType
  }
  case object S32Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.IntType
  }
  case object S64Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.LongType
  }
  case object F32Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.FloatType
  }
  case object F64Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.DoubleType
  }
  case object CharType extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.CharType
  }
  case object StringType extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.StringType
  }

  final case class ListType(elemType: ValType, length: Option[Int]) extends FundamentalType {
    def toIRType(): jstpe.Type = ??? // Types.ArrayType(ArrayTypeRef.of(toTypeRef(elemType.toIRType())), false)
  }

  final case class FieldType(label: String, tpe: ValType)
  final case class RecordType(fields: List[FieldType]) extends FundamentalType {
    def toIRType(): jstpe.Type = ???
  }

  final case class TupleType(ts: List[ValType]) extends SpecializedType {
    def toIRType(): jstpe.Type = ???
  }

  final case class CaseType(className: ClassName, tpe: ValType) {
    def toIRType(): jstpe.Type = jstpe.ClassType(className, false)
  }
  final case class VariantType(className: ClassName, cases: List[CaseType]) extends FundamentalType {
    def toIRType(): jstpe.Type = jstpe.ClassType(className, false)
  }
  final case class EnumType(labels: List[String]) extends SpecializedType {
    override def toIRType(): jstpe.Type = ???
  }
  final case class OptionType(tpe: ValType) extends SpecializedType {
    def toIRType(): jstpe.Type = ???
  }
  // final case class ResultType(
  //     ok: ValType,
  //     error: ValType) extends SpecializedType {
  //   def toIRType(): jstpe.Type = ???
  // }

  final case class FlagsType(labels: List[String]) extends FundamentalType {
    def toIRType(): jstpe.Type = ???
  }

  final case class ResourceType(className: ClassName) extends FundamentalType {
    def toIRType(): jstpe.Type = jstpe.ClassType(className, false)
  }

  // ExternTypes
  final case class FuncType(paramTypes: List[ValType], resultType: ValType) extends ExternType

  // def fromIRType(tpe: jstpe.Type): WasmInterfaceType = {
  //   tpe match {
  //     case jstpe.BooleanType => BoolType
  //     case jstpe.ByteType => S8Type
  //     case jstpe.ShortType => S16Type
  //     case jstpe.IntType => S32Type
  //     case jstpe.LongType => S64Type
  //     case jstpe.FloatType => F32Type
  //     case jstpe.DoubleType => F64Type
  //     case jstpe.CharType => CharType
  //     case jstpe.StringType => StringType
  //     case jstpe.ArrayType(_, _) => ListType(???, ???)
  //     // record
  //     // tuple
  //     // variant
  //     case _ if tpe.typeSymbol.isSubClass() =>
  //     // enum
  //     // option
  //     // result
  //     // flag
  //     // resource
  //     case _ => throw new AssertionError(s"Invalid type $tpe")

  //   }
  // }

}