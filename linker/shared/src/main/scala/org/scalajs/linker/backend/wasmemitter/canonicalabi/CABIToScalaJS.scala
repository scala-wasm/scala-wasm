package org.scalajs.linker.backend.wasmemitter.canonicalabi

import org.scalajs.ir.Types._
import org.scalajs.ir.{WasmInterfaceTypes => wit}
import org.scalajs.ir.Names._
import org.scalajs.ir.Trees.MemberNamespace
import org.scalajs.ir.OriginalName.NoOriginalName

import org.scalajs.linker.backend.webassembly.FunctionBuilder
import org.scalajs.linker.backend.webassembly.{Instructions => wa}
import org.scalajs.linker.backend.webassembly.{Modules => wamod}
import org.scalajs.linker.backend.webassembly.{Identitities => wanme}
import org.scalajs.linker.backend.webassembly.{Types => watpe}
import org.scalajs.linker.backend.webassembly.Types.{FunctionType => Sig}
import org.scalajs.linker.backend.wasmemitter.VarGen._
import org.scalajs.linker.backend.wasmemitter.SpecialNames
import org.scalajs.linker.backend.wasmemitter.TypeTransformer._

import org.scalajs.linker.backend.webassembly.component.Flatten

import ValueIterators._

object CABIToScalaJS {

  def genLoadMemory(fb: FunctionBuilder, tpe: wit.ValType, ptr: wanme.LocalID): Unit = {
    fb += wa.LocalGet(ptr)
    tpe match {
      case pt: wit.PrimValType => pt match {
        case wit.VoidType =>
        case wit.BoolType => fb += wa.I32Load8U()
        case wit.S8Type   => fb += wa.I32Load8S()
        case wit.S16Type  => fb += wa.I32Load16S()
        case wit.S32Type  => fb += wa.I32Load()
        case wit.S64Type  => fb += wa.I64Load()
        case wit.U8Type   => fb += wa.I32Load8U()
        case wit.U16Type  => fb += wa.I32Load16U()
        case wit.U32Type  => fb += wa.I32Load()
        case wit.U64Type  => fb += wa.I64Load()
        case wit.F32Type => fb += wa.F32Load()
        case wit.F64Type => fb += wa.F64Load()
        case wit.CharType => fb += wa.I32Load()
        case wit.StringType =>
          fb += wa.I32Load() // offset
          fb += wa.LocalGet(ptr)
          fb += wa.I32Const(4)
          fb += wa.I32Add
          fb += wa.I32Load() // code units (UTF-16)
          fb += wa.Call(genFunctionID.cabiLoadString)
      }

      case wit.ResourceType(_) =>
        fb += wa.I32Load()

      case variant @ wit.VariantType(_, cases) =>
        fb += wa.Drop
        genLoadVariant(
          fb,
          variant,
          () => {
            genLoadMemory(fb, wit.discriminantType(cases), ptr) // load i32 (case index) from memory
            genAlignTo(fb, wit.maxCaseAlignment(cases), ptr)
          },
          (tpe) => {
            genLoadMemory(fb, tpe, ptr)
          }
        )

      case _ => ???
    }

    fb += wa.LocalGet(ptr)
    fb += wa.I32Const(wit.elemSize(tpe))
    fb += wa.I32Add
    fb += wa.LocalSet(ptr)
  }

  def genLoadStack(fb: FunctionBuilder, tpe: wit.ValType, vi: ValueIterator): Unit = {
    tpe match {
      // Scala.js has a same representation
      case wit.BoolType | wit.S8Type | wit.S16Type | wit.S32Type |
          wit.U8Type | wit.U16Type | wit.U32Type =>
        vi.next(watpe.Int32)

      case wit.S64Type | wit.U64Type =>
        vi.next(watpe.Int64)

      case wit.F32Type => vi.next(watpe.Float32)
      case wit.F64Type => vi.next(watpe.Float64)

      case wit.VoidType =>

      case wit.ResourceType(_) =>
        vi.next(watpe.Int32)

      case variant @ wit.VariantType(_, cases) =>
        val flattened = Flatten.flattenVariants(cases.map(_.tpe))
        genLoadVariant(
          fb,
          variant,
          () => vi.next(watpe.Int32),
          (tpe) => {
            val expect = Flatten.flattenType(tpe)
            genCoerceValues(fb, vi, flattened, expect)
            genLoadStack(fb, tpe, new ValueIterator(fb, expect))
          }
        )

      case _ => ???
    }
  }

  private def genLoadVariant(
      fb: FunctionBuilder,
      variant: wit.VariantType,
      genGetIndex: () => Unit,
      genLoadValue: (wit.ValType) => Unit
  ): Unit = {
    val cases = variant.cases
    val flattened = Flatten.flattenVariants(cases.map(_.tpe))
    fb.switch(
      // Sig(Nil, List(watpe.Int32)),
      // Sig(List(watpe.Int32), List(watpe.RefType(false, genTypeID.ObjectStruct)))
      Sig(Nil, Nil),
      Sig(Nil, List(watpe.RefType(false, genTypeID.ObjectStruct)))
    )(
      genGetIndex
    )(
      cases.zipWithIndex.map { case (c, i) =>
        val ctor = wit.makeCtorName(c.tpe)
        (List(i), () => {
          genNewScalaClass(fb, c.className, ctor) {
            genLoadValue(c.tpe)
          }
        })
      }: _*
    ) { () =>
      fb += wa.Unreachable
    }
  }

  /**
    *
    * @see [[https://github.com/WebAssembly/component-model/blob/main/design/mvp/CanonicalABI.md#flat-lowering]]
    */
  private def genCoerceValues(
    fb: FunctionBuilder,
    vi: ValueIterator,
    types: List[watpe.Type],
    expect: List[watpe.Type]
  ): Unit = {
    types.zip(expect).map { case (have, want) =>
      vi.next(have)
      (have, want) match {
        case (watpe.Int32, watpe.Float32) =>
          fb += wa.F32ReinterpretI32
          // TODO: canonicalize NaN
        case (watpe.Int64, watpe.Int32) =>
          fb += wa.I32WrapI64
        case (watpe.Int64, watpe.Float32) =>
          fb += wa.I32WrapI64
          fb += wa.F32ReinterpretI32
          // TODO: canonicalize NaN
        case (watpe.Int64, watpe.Float64) =>
          fb += wa.F64ReinterpretI64
          // TODO: canonicalize NaN
        case _ => assert(have == want)
      }
    }
    // drop padding
    for (_ <- types.drop(expect.length)) {} // do nothing
  }

  private def genNewScalaClass(fb: FunctionBuilder, cls: ClassName, ctor: MethodName)(
      genCtorArgs: => Unit): Unit = {
    val instanceLocal = fb.addLocal(NoOriginalName, watpe.RefType(genTypeID.forClass(cls)))

    fb += wa.Call(genFunctionID.newDefault(cls))
    fb += wa.LocalTee(instanceLocal)
    genCtorArgs
    fb += wa.Call(genFunctionID.forMethod(MemberNamespace.Constructor, cls, ctor))
    fb += wa.LocalGet(instanceLocal)
  }

  private def genAlignTo(fb: FunctionBuilder, align: Int, ptr: wanme.LocalID): Unit = {
    // ;; Calculate: (ptr + alignment - 1) / alignment * alignment
    // ptr + alignment - 1
    fb += wa.LocalGet(ptr)
    fb += wa.I32Const(align)
    fb += wa.I32Add
    fb += wa.I32Const(1)
    fb += wa.I32Sub

    // (ptr + alignment - 1) / alignment
    fb += wa.I32Const(align)
    fb += wa.I32DivU
    fb += wa.I32Const(align)
    fb += wa.I32Mul

    fb += wa.LocalSet(ptr)
  }

}