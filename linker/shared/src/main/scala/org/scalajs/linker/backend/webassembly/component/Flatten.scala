package org.scalajs.linker.backend.webassembly.component

import org.scalajs.linker.backend.webassembly.{Types => watpe}
import org.scalajs.ir.{WasmInterfaceTypes => wit}

object Flatten {
  val MaxFlatParams = 16
  val MaxFlatResults = 1

  def lowerFlattenFuncType(funcType: wit.FuncType): watpe.FunctionType = {
    val flatParamTypes = funcType.paramTypes.flatMap(flattenType)
    val flatResultTypes = flattenType(funcType.resultType)

    val paramsViaMemory = flatParamTypes.length > MaxFlatParams
    val returnsViaMemory = flatResultTypes.length > MaxFlatResults

    val finalParamTypes = if (paramsViaMemory) List(watpe.Int32) else flatParamTypes
    val finalResultTypes = if (returnsViaMemory) Nil else flatResultTypes

    watpe.FunctionType(
      if (returnsViaMemory) finalParamTypes :+ watpe.Int32 else finalParamTypes,
      finalResultTypes)
  }

  def liftFlattenFuncType(funcType: wit.FuncType): watpe.FunctionType = {
    val flatParamTypes = funcType.paramTypes.flatMap(flattenType)
    val flatResultTypes = flattenType(funcType.resultType)

    val paramsViaMemory = flatParamTypes.length > MaxFlatParams
    val returnsViaMemory = flatResultTypes.length > MaxFlatResults

    val finalParamTypes = if (paramsViaMemory) List(watpe.Int32) else flatParamTypes
    val finalResultTypes = if (returnsViaMemory) List(watpe.Int32) else flatResultTypes

    watpe.FunctionType(finalParamTypes, finalResultTypes)
  }

  def flattenType(tpe: wit.ValType): List[watpe.Type] =
    wit.despecialize(tpe) match {
      case wit.VoidType => Nil
      case wit.BoolType => List(watpe.Int32)
      case wit.U8Type | wit.U16Type | wit.U32Type => List(watpe.Int32)
      case wit.S8Type | wit.S16Type | wit.S32Type => List(watpe.Int32)
      case wit.U64Type | wit.S64Type => List(watpe.Int64)
      case wit.F32Type => List(watpe.Float32)
      case wit.F64Type => List(watpe.Float64)
      case wit.CharType => List(watpe.Int32)
      case wit.StringType => List(watpe.Int32, watpe.Int32)
      case t: wit.ListType => flattenList(t)
      case t: wit.RecordType => flattenRecord(t)
      case t: wit.VariantType => flattenVariant(t)
      case _: wit.FlagsType => List(watpe.Int32)
      case _: wit.ResourceType => List(watpe.Int32)
    }

    private def flattenList(t: wit.ListType): List[watpe.Type] =
      t.length match {
        case Some(length) => List.fill(length)(flattenType(t.elemType)).flatten
        case None => List(watpe.Int32, watpe.Int32)
      }

    private def flattenRecord(t: wit.RecordType): List[watpe.Type] =
      t.fields.flatMap(f => flattenType(f.tpe))

    private def flattenVariant(t: wit.VariantType): List[watpe.Type] = {
      val variantTypes = t.cases.collect { case wit.CaseType(_, tpe) => tpe }
      List(watpe.Int32) ++ flattenVariants(variantTypes)
    }

    def flattenVariants(variants: List[wit.ValType]): List[watpe.Type] = {
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


}