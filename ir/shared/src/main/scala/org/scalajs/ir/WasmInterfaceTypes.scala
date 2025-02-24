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
    def toIRType(): jstpe.Type = jstpe.ClassType(BoxedStringClass, true)
  }

  final case class ListType(elemType: ValType, length: Option[Int]) extends FundamentalType {
    def toIRType(): jstpe.Type = {
      val ref = toTypeRef(elemType)
      Types.ArrayType(Types.ArrayTypeRef.of(ref), true)
    }
  }

  /** label won't be used in load/store with memory or stack, used for Analyzer */
  final case class FieldType(label: FieldName, tpe: ValType)
  /**
    * className is required for loading data back to Scala class
    */
  final case class RecordType(className: ClassName, fields: List[FieldType]) extends FundamentalType {
    def toIRType(): jstpe.Type = jstpe.ClassType(className, true)
  }

  final case class TupleType(ts: List[ValType]) extends SpecializedType {
    def toIRType(): jstpe.Type = jstpe.ClassType(ClassName("scala.Tuple" + ts.size), true)
  }

  final case class CaseType(className: ClassName, tpe: ValType) {
    def toIRType(): jstpe.Type = jstpe.ClassType(className, true)
  }
  final case class VariantType(className: ClassName, cases: List[CaseType]) extends FundamentalType {
    def toIRType(): jstpe.Type = jstpe.ClassType(className, true)
  }

  final case class ResultType(ok: ValType, err: ValType) extends SpecializedType {
    def toIRType(): jstpe.Type = jstpe.ClassType(Names.ComponentResultClass, true)
  }

  final case class EnumType(labels: List[String]) extends SpecializedType {
    override def toIRType(): jstpe.Type = ???
  }
  final case class OptionType(tpe: ValType) extends SpecializedType {
    def toIRType(): jstpe.Type = jstpe.ClassType(Names.ComponentOptionClass, true)
  }
  final case class FlagsType(numFields: Int) extends FundamentalType {
    def toIRType(): jstpe.Type = jstpe.IntType
  }

  final case class ResourceType(className: ClassName) extends FundamentalType {
    def toIRType(): jstpe.Type = jstpe.ClassType(className, true)
  }

  // ExternTypes
  final case class FuncType(paramTypes: List[ValType], resultType: Option[ValType]) extends ExternType

  // utilities

  def toTypeRef(tpe: ValType): jstpe.TypeRef = tpe match {
    case VoidType => jstpe.ClassRef(BoxedUnitClass)
    case BoolType => jstpe.BooleanRef
    case U8Type | S8Type => jstpe.ByteRef
    case U16Type | S16Type => jstpe.ShortRef
    case U32Type | S32Type => jstpe.IntRef
    case U64Type | S64Type => jstpe.LongRef
    case F32Type => jstpe.FloatRef
    case F64Type => jstpe.DoubleRef
    case CharType => jstpe.CharRef
    case StringType => jstpe.ClassRef(BoxedStringClass)
    case ListType(elemType, length) => ???
    case RecordType(className, fields) => jstpe.ClassRef(className)
    case TupleType(ts) => ???
    case VariantType(className, cases) => jstpe.ClassRef(className)
    case ResultType(ok, err) => jstpe.ClassRef(ComponentResultClass)
    case EnumType(labels) => ???
    case OptionType(tpe) => ???
    case FlagsType(_) => jstpe.IntRef
    case ResourceType(className) => jstpe.ClassRef(className)
  }

  def makeCtorName(tpe: ValType): MethodName = {
    if (tpe == VoidType)
      MethodName.constructor(Nil)
    else
      MethodName.constructor(List(toTypeRef(tpe)))
  }

  /**
    *
    * @see
    *   [[https://github.com/WebAssembly/component-model/blob/main/design/mvp/CanonicalABI.md#despecialization]]
    */
  def despecialize(t: ValType): FundamentalType = t match {
    case st: SpecializedType => st match {

      case TupleType(ts) =>
        val className = ClassName("scala.Tuple" + ts.size)
        RecordType(
          className,
          ts.zipWithIndex.map { case (t, i) =>
            FieldType(FieldName(className, SimpleFieldName(s"_${i+1}")), t)
          }
        )

      case EnumType(labels) =>
        VariantType(???, labels.map(l => CaseType(???, VoidType)))

      case OptionType(t) =>
        VariantType(
          Names.ComponentOptionClass,
          List(
            CaseType(Names.ComponentOptionSomeClass, t),
            CaseType(Names.ComponentOptionNoneClass, VoidType)
          )
        )

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

  def elemSize(tpe: Option[ValType]): Int = tpe match {
    case None => 0
    case Some(value) => elemSize(value)
  }
  def elemSize(tpe: ValType): Int =
    despecialize(tpe) match {
      case VoidType => 0
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
      case RecordType(_, fields) =>
        val size = fields.foldLeft(0) { case (ptr, f) =>
          alignTo(ptr, alignment(f.tpe)) + elemSize(f.tpe)
        }
        alignTo(size, alignment(tpe))

      case VariantType(_, cases) =>
        val indexSize = alignTo(elemSize(discriminantType(cases)), maxCaseAlignment(cases))
        val size = indexSize + cases.map(c => elemSize(c.tpe)).max
        alignTo(size, alignment(tpe))
      case FlagsType(n) =>
        assert(n > 0)
        assert(n <= 32)
        if (n <= 8) 1
        else if (n <= 16) 2
        else 4
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
      case RecordType(_, fields) =>
        fields.map(f => alignment(f.tpe)).max
      case VariantType(_, cases) =>
        val maxCaseAlign = maxCaseAlignment(cases)
        val caseIndexAlign = alignment(discriminantType(cases))
        if (maxCaseAlign > caseIndexAlign) maxCaseAlign else caseIndexAlign
      case FlagsType(n) =>
        assert(n > 0)
        assert(n <= 32)
        if (n <= 8) 1
        else if (n <= 16) 2
        else 4
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