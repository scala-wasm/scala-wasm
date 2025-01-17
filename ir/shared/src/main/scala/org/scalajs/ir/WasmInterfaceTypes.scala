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
    def toIRType(): jstpe.Type = {
        elemType.toIRType() match {
          case pt: jstpe.PrimTypeWithRef => pt.primRef
          case jstpe.ClassType(className, _) => jstpe.ClassRef(className)
          case jstpe.StringType => jstpe.ClassRef(BoxedStringClass)
          case jstpe.ArrayType(_, _) =>
            throw new AssertionError(s"TODO support nested list type")
          case jstpe.AnyType | jstpe.AnyNotNullType | jstpe.UndefType | jstpe.WasmComponentResourceType(_) | jstpe.RecordType(_) =>
            throw new AssertionError(s"Invalid type ${elemType}")

        }
        Types.ArrayType(Types.ArrayTypeRef.of(???), false)
    }
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

  final case class ResultType(ok: ValType, err: ValType) extends SpecializedType {
    def toIRType(): jstpe.Type = jstpe.ClassType(Names.ComponentResultClass, false)
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
    def toIRType(): jstpe.Type = jstpe.WasmComponentResourceType(className)
  }

  // ExternTypes
  final case class FuncType(paramTypes: List[ValType], resultType: ValType) extends ExternType

  // utilities

  def makeCtorName(tpe: ValType): MethodName = {
    tpe.toIRType() match {
      case pt: jstpe.PrimTypeWithRef => MethodName.constructor(List(pt.primRef))
      case jstpe.ClassType(className, _) => MethodName.constructor(List(jstpe.ClassRef(className)))
      case jstpe.StringType => MethodName.constructor(List(jstpe.ClassRef(BoxedStringClass)))
      case jstpe.AnyType => MethodName.constructor(List(jstpe.ClassRef(ObjectClass)))
      case jstpe.AnyNotNullType => MethodName.constructor(List(jstpe.ClassRef(ObjectClass)))
      case jstpe.ArrayType(_, _) => ???
      case jstpe.RecordType(_) => ???
      case jstpe.UndefType => ???
      case jstpe.WasmComponentResourceType(className) =>
        MethodName.constructor(List(jstpe.ClassRef(className)))
    }
  }

  /**
    *
    * @see
    *   [[https://github.com/WebAssembly/component-model/blob/main/design/mvp/CanonicalABI.md#despecialization]]
    */
  def despecialize(t: ValType): FundamentalType = t match {
    case st: SpecializedType => st match {

      case TupleType(ts) =>
        RecordType(ts.zipWithIndex.map { case (t, i) => FieldType(i.toString, t) })

      case EnumType(labels) =>
        VariantType(???, labels.map(l => CaseType(???, VoidType)))

      case OptionType(t) =>
        VariantType(???, List(
          CaseType(???, VoidType),
          CaseType(???, t)
        ))

      case ResultType(ok, err) =>
        VariantType(
          Names.ComponentResultClass,
          List(
            CaseType(Names.ComponentResultOkClass, ok),
            CaseType(Names.ComponentResultErrClass, err)
          )
        )
    }
    case ft: FundamentalType => ft
  }

  def elemSize(tpe: ValType): Int =
    despecialize(tpe) match {
      case VoidType => 1
      case BoolType | U8Type | S8Type => 1
      case U16Type | S16Type => 2
      case U32Type | S32Type | F32Type => 4
      case U64Type | S64Type | F64Type => 8
      case CharType => 4
      case StringType => 8
      case ListType(elemType, length) =>
        length match {
          case None => 8
          case Some(value) => elemSize(elemType) * value
        }
      case RecordType(fields) => ???
      case VariantType(_, cases) =>
        val indexSize = alignTo(elemSize(discriminantType(cases)), maxCaseAlignment(cases))
        val size = indexSize + cases.map(c => elemSize(c.tpe)).max
        alignTo(size, alignment(tpe))
      case FlagsType(labels) => ???
      case ResourceType(className) => 4
    }

  def alignment(tpe: ValType): Int =
    despecialize(tpe) match {
      case VoidType => 1
      case BoolType | U8Type | S8Type => 1
      case U16Type | S16Type => 2
      case U32Type | S32Type | F32Type => 4
      case U64Type | S64Type | F64Type => 8
      case CharType => 4
      case StringType => 4
      case ListType(elemType, length) =>
        length match {
          case None => 4
          case Some(_) => alignment(elemType)
        }
      case RecordType(fields) =>
        fields.map(f => alignment(f.tpe)).max
      case VariantType(_, cases) =>
        val maxCaseAlign = maxCaseAlignment(cases)
        val caseIndexAlign = alignment(discriminantType(cases))
        if (maxCaseAlign > caseIndexAlign) maxCaseAlign else caseIndexAlign
      case FlagsType(labels) => ???
      case ResourceType(className) => 4
    }

  // def align_to(ptr, alignment):
  // return math.ceil(ptr / alignment) * alignment
  private def alignTo(ptr: Int, alignment: Int): Int =
    ((ptr + alignment - 1) / alignment) * alignment

  def maxCaseAlignment(cases: List[CaseType]): Int =
    cases.map(c => alignment(c.tpe)).max

  def discriminantType(cases: Seq[_]): PrimValType = {
    val n = cases.length
    require(0 < n && n < (1L << 32), "Number of cases must be within range.")
    (math.ceil(math.log(n) / math.log(2) / 8)).toInt match {
      case 0 => U8Type
      case 1 => U8Type
      case 2 => U16Type
      case 3 => U32Type
      case _ => throw new AssertionError(s"Number of cases must be within the 2^32.")
    }
  }

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