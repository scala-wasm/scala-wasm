package org.scalajs.linker.backend.wasmemitter.canonicalabi

import org.scalajs.ir.Names.ClassName
import org.scalajs.ir.WasmInterfaceTypes._
import org.scalajs.ir.WellKnownNames._
import org.scalajs.ir.WitTypeDef

import org.scalajs.linker.backend.wasmemitter.WasmContext

private[canonicalabi] object WitTypeOps {

  def recordFields(className: ClassName)(
      implicit ctx: WasmContext): List[FieldType] = {
    val WitTypeDef.Record(_, _, _, fields) =
      ctx.getWitTypeDef(className): @unchecked
    fields
  }

  def elemSize(tpe: Option[ValType])(implicit ctx: WasmContext): Int = tpe match {
    case None        => 0
    case Some(value) => elemSize(value)
  }

  def elemSize(tpe: ValType)(implicit ctx: WasmContext): Int = tpe match {
    case BoolType | U8Type | S8Type  => 1
    case U16Type | S16Type           => 2
    case U32Type | S32Type | F32Type => 4
    case U64Type | S64Type | F64Type => 8
    case CharType                    => 4
    case StringType                  => 8
    case ListType(elemType, length)  =>
      length match {
        case None        => 8
        case Some(value) => elemSize(elemType) * value
      }
    case TupleType(ts) =>
      val size = ts.foldLeft(0) { case (ptr, t) =>
        alignTo(ptr, alignment(t)) + elemSize(t)
      }
      alignTo(size, alignment(tpe))
    case RecordTypeRef(className) =>
      val size = recordFields(className).foldLeft(0) { case (ptr, f) =>
        alignTo(ptr, alignment(f.tpe)) + elemSize(f.tpe)
      }
      alignTo(size, alignment(tpe))
    case tpe: VariantTypeRef =>
      elemSizeVariant(getVariantCases(tpe))
    case tpe: EnumTypeRef =>
      elemSizeVariant(getVariantCases(tpe))
    case tpe: OptionType =>
      elemSizeVariant(getVariantCases(tpe))
    case tpe: ResultType =>
      elemSizeVariant(getVariantCases(tpe))
    case FlagsTypeRef(className) =>
      flagsSize(flagNames(className))
    case a: AliasTypeRef =>
      elemSize(ctx.dealiasWit(a))
    case ResourceType(_, _) =>
      4
  }

  def alignment(tpe: ValType)(implicit ctx: WasmContext): Int = tpe match {
    case BoolType | U8Type | S8Type  => 1
    case U16Type | S16Type           => 2
    case U32Type | S32Type | F32Type => 4
    case U64Type | S64Type | F64Type => 8
    case CharType                    => 4
    case StringType                  => 4
    case ListType(elemType, length)  =>
      length match {
        case None    => 4
        case Some(_) => alignment(elemType)
      }
    case TupleType(ts) =>
      ts.map(alignment).max
    case RecordTypeRef(className) =>
      recordFields(className).map(f => alignment(f.tpe)).max
    case tpe: VariantTypeRef =>
      alignmentVariant(getVariantCases(tpe))
    case tpe: EnumTypeRef =>
      alignmentVariant(getVariantCases(tpe))
    case tpe: OptionType =>
      alignmentVariant(getVariantCases(tpe))
    case tpe: ResultType =>
      alignmentVariant(getVariantCases(tpe))
    case FlagsTypeRef(className) =>
      flagsSize(flagNames(className))
    case a: AliasTypeRef =>
      alignment(ctx.dealiasWit(a))
    case ResourceType(_, _) =>
      4
  }

  def maxCaseAlignment(cases: List[CaseType])(
      implicit ctx: WasmContext): Int = {
    cases.map(c => c.tpe.map(alignment).getOrElse(1)).max
  }

  def discriminantType(cases: Seq[_]): PrimValType = {
    val n = cases.length
    require(0 < n && n < (1L << 32), "Number of cases must be within range.")
    (math.ceil(math.log(n) / math.log(2) / 8)).toInt match {
      case 0 => U8Type
      case 1 => U8Type
      case 2 => U16Type
      case 3 => U32Type
      case _ =>
        throw new AssertionError("Number of cases must be within the 2^32.")
    }
  }

  private def flagNames(className: ClassName)(
      implicit ctx: WasmContext): List[String] = {
    val WitTypeDef.Flags(_, _, _, names) =
      ctx.getWitTypeDef(className): @unchecked
    names
  }

  def getVariantCases(tpe: ValType)(implicit ctx: WasmContext): List[CaseType] = {
    tpe match {
      case VariantTypeRef(className) =>
        val WitTypeDef.Variant(_, _, _, cases) =
          ctx.getWitTypeDef(className): @unchecked
        cases
      case EnumTypeRef(className) =>
        val WitTypeDef.Enum(_, _, _, cases) =
          ctx.getWitTypeDef(className): @unchecked
        cases
      case OptionType(tpe) =>
        List(
          CaseType(juOptionalClass, "none", None),
          CaseType(juOptionalClass, "some", Some(tpe))
        )
      case ResultType(ok, err) =>
        List(
          CaseType(ComponentResultOkClass, "ok", ok),
          CaseType(ComponentResultErrClass, "err", err)
        )
      case _ =>
        throw new AssertionError(s"Not a variant-like type: $tpe")
    }
  }

  private def flagsSize(names: List[String]): Int = {
    val n = names.size
    assert(n > 0)
    assert(n <= 32)
    if (n <= 8) 1
    else if (n <= 16) 2
    else 4
  }

  private def elemSizeVariant(cases: List[CaseType])(
      implicit ctx: WasmContext): Int = {
    val indexSize =
      alignTo(elemSize(discriminantType(cases)), maxCaseAlignment(cases))
    val size = indexSize + cases.map(c => elemSize(c.tpe)).max
    alignTo(size, alignmentVariant(cases))
  }

  private def alignmentVariant(cases: List[CaseType])(
      implicit ctx: WasmContext): Int = {
    val maxCaseAlign = maxCaseAlignment(cases)
    val caseIndexAlign = alignment(discriminantType(cases))
    if (maxCaseAlign > caseIndexAlign) maxCaseAlign else caseIndexAlign
  }

  private def alignTo(ptr: Int, alignment: Int): Int =
    ((ptr + alignment - 1) / alignment) * alignment
}
