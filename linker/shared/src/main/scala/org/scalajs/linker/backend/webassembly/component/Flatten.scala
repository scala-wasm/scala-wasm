package org.scalajs.linker.backend.webassembly.component

import Types._

import org.scalajs.linker.backend.webassembly.{Types => watpe}

object Flatten {
  val MaxFlatParams = 16
  val MaxFlatResults = 1

  def lowerFlattenFuncType(funcType: FuncType): watpe.FunctionType = {
    val flatParamTypes = funcType.paramTypes.flatMap(flattenType)
    val flatResultTypes = funcType.resultTypes.flatMap(flattenType)

    val paramsViaMemory = flatParamTypes.length > MaxFlatParams
    val returnsViaMemory = flatResultTypes.length > MaxFlatResults

    val finalParamTypes = if (paramsViaMemory) List(watpe.Int32) else flatParamTypes
    val finalResultTypes = if (returnsViaMemory) Nil else flatResultTypes

    watpe.FunctionType(
      if (returnsViaMemory) finalParamTypes :+ watpe.Int32 else finalParamTypes,
      finalResultTypes)
  }

  def liftFlattenFuncType(funcType: FuncType): watpe.FunctionType = {
    val flatParamTypes = funcType.paramTypes.flatMap(flattenType)
    val flatResultTypes = funcType.resultTypes.flatMap(flattenType)

    val paramsViaMemory = flatParamTypes.length > MaxFlatParams
    val returnsViaMemory = flatResultTypes.length > MaxFlatResults

    val finalParamTypes = if (paramsViaMemory) List(watpe.Int32) else flatParamTypes
    val finalResultTypes = if (returnsViaMemory) List(watpe.Int32) else flatResultTypes

    watpe.FunctionType(finalParamTypes, finalResultTypes)
  }

  def flattenType(tpe: Option[ValType]): List[watpe.Type] =
    tpe match {
      case None => Nil
      case Some(t) => flattenType(t)
    }

  def flattenType(tpe: ValType): List[watpe.Type] =
    despecialize(tpe) match {
      case BoolType => List(watpe.Int32)
      case U8Type | U16Type | U32Type => List(watpe.Int32)
      case S8Type | S16Type | S32Type => List(watpe.Int32)
      case U64Type | S64Type => List(watpe.Int64)
      case F32Type => List(watpe.Float32)
      case F64Type => List(watpe.Float64)
      case CharType => List(watpe.Int32)
      case StringType => List(watpe.Int32, watpe.Int32)
      case t: ListType => flattenList(t)
      case t: RecordType => flattenRecord(t)
      case t: VariantType => flattenVariant(t)
      case _: FlagsType => List(watpe.Int32)
    }

    private def flattenList(t: ListType): List[watpe.Type] =
      t.length match {
        case Some(length) => List.fill(length)(flattenType(t.elemType)).flatten
        case None => List(watpe.Int32, watpe.Int32)
      }

    private def flattenRecord(t: RecordType): List[watpe.Type] =
      t.fields.flatMap(f => flattenType(f.tpe))

    private def flattenVariant(t: VariantType): List[watpe.Type] = {
      val variantTypes = t.cases.collect { case CaseType(_, Some(tpe)) => tpe }
      List(watpe.Int32) ++ flattenVariants(variantTypes)
    }

    def flattenVariants(variants: List[ValType]): List[watpe.Type] = {
      variants.foldLeft(List.empty[watpe.Type]) { case (acc, variant) =>
        val flattened = flattenType(variant)
        val joined = acc.zip(flattened).map { case (a, b) => join(a, b) }
        joined ++ flattened.drop(joined.length) ++ acc.drop(joined.length)
      }
    }

    private def join(a: watpe.Type, b: watpe.Type): watpe.Type = {
      if (a == b) a
      else if ((a == watpe.Int32 && b == watpe.Float32) ||
               (a == watpe.Float32 && b == watpe.Int32)) watpe.Int32
      else watpe.Int64
    }


  /**
    *
    * @see
    *   [[https://github.com/WebAssembly/component-model/blob/main/design/mvp/CanonicalABI.md#despecialization]]
    */
  private def despecialize(t: ValType): FundamentalType = t match {
    case st: SpecializedType => st match {

      case TupleType(ts) =>
        RecordType(ts.zipWithIndex.map { case (t, i) => FieldType(i.toString, t) })

      case EnumType(labels) =>
        VariantType(labels.map(l => CaseType(l, None)))

      case OptionType(t) =>
        VariantType(List(
          CaseType("none", None),
          CaseType("some", Some(t))
        ))

      case ResultType(ok, err) =>
        VariantType(List(
          CaseType("ok", ok),
          CaseType("error", err)
        ))
    }
    case ft: FundamentalType => ft
  }
}