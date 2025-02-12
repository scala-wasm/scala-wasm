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
import org.scalajs.linker.backend.webassembly.component.Flatten

import org.scalajs.linker.backend.wasmemitter.VarGen._
import org.scalajs.linker.backend.wasmemitter.SpecialNames
import org.scalajs.linker.backend.wasmemitter.TypeTransformer._
import org.scalajs.linker.backend.wasmemitter.SWasmGen
import org.scalajs.linker.backend.wasmemitter.WasmContext

import ValueIterators._

object CABIToScalaJS {

  /** Load data from linear memory and move the pointer.
    *
    * @param tpe - target Wasm Interface Type to load
    * @param ptr - memory offset for loading data from
    */
  def genLoadMemory(fb: FunctionBuilder, tpe: wit.ValType, ptr: wanme.LocalID)(implicit ctx: WasmContext): Unit = {
    tpe match {
      case wit.VoidType => // do nothing
      case pt: wit.PrimValType =>
        fb += wa.LocalGet(ptr)
        pt match {
          case wit.VoidType => throw new AssertionError("should be handled outside")
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
        genMovePtr(fb, ptr, tpe)

      case wit.RecordType(className, fields) =>
        val typeRefs = fields.map(f => wit.toTypeRef(f.tpe))
        val ctor = MethodName.constructor(typeRefs)
        genNewScalaClass(fb, className, ctor) {
          for (f <- fields) {
            val align = wit.alignment(f.tpe)
            genAlignTo(fb, align, ptr)
            genLoadMemory(fb, f.tpe, ptr) // load and move memory
          }
        }
        genAlignTo(fb, wit.alignment(tpe), ptr)

      case wit.TupleType(fields) =>
        val ctor = MethodName.constructor(fields.map(_ => ClassRef(ObjectClass)))
        val className = ClassName("scala.Tuple" + fields.size)
        genNewScalaClass(fb, className, ctor) {
          for (f <- fields) {
            val align = wit.alignment(f)
            genAlignTo(fb, align, ptr)
            genLoadMemory(fb, f, ptr) // load and move memory
            f.toIRType() match {
              case t: PrimTypeWithRef => genBox(fb, t)
              case _ =>
            }
          }
        }

      case wit.ResourceType(_) =>
        fb += wa.LocalGet(ptr)
        fb += wa.I32Load()
        genMovePtr(fb, ptr, tpe)

      case variant @ wit.VariantType(className, cases) =>
        genLoadVariant(
          fb,
          variant,
          () => {
            genLoadMemory(fb, wit.discriminantType(cases), ptr) // load i32 (case index) from memory
            genAlignTo(fb, wit.maxCaseAlignment(cases), ptr)
          },
          (tpe) => {
            genLoadMemory(fb, tpe, ptr)
            val maxElemSize = cases.map(c => wit.elemSize(c.tpe)).max
            val elemSize = wit.elemSize(tpe)
            genMovePtr(fb, ptr, maxElemSize - elemSize)
          },
          className
        )
        genAlignTo(fb, wit.alignment(variant), ptr)

      case tpe => throw new AssertionError(s"unsupported tpe: $tpe")
    }
  }

  def genLoadStack(fb: FunctionBuilder, tpe: wit.ValType, vi: ValueIterator)(implicit ctx: WasmContext): Unit = {
    tpe match {
      case wit.VoidType => // do nothing
      // Scala.js has a same representation
      case wit.BoolType | wit.S8Type | wit.S16Type | wit.S32Type |
          wit.U8Type | wit.U16Type | wit.U32Type | wit.CharType =>
        vi.next(watpe.Int32)

      case wit.S64Type | wit.U64Type =>
        vi.next(watpe.Int64)

      case wit.F32Type => vi.next(watpe.Float32)
      case wit.F64Type => vi.next(watpe.Float64)

      case wit.StringType =>
        vi.next(watpe.Int32)
        vi.next(watpe.Int32)
        fb += wa.Call(genFunctionID.cabiLoadString)

      case wit.ResourceType(_) =>
        vi.next(watpe.Int32)

      case wit.TupleType(fields) =>
        val ctor = MethodName.constructor(fields.map(_ => ClassRef(ObjectClass)))
        val className = ClassName("scala.Tuple" + fields.size)
        genNewScalaClass(fb, className, ctor) {
          for (f <- fields) {
            val fieldType = Flatten.flattenType(f)
            genLoadStack(fb, f, vi)
            f.toIRType() match {
              case t: PrimTypeWithRef => genBox(fb, t)
              case _ =>
            }
          }
        }

      case wit.RecordType(className, fields) =>
        val typeRefs = fields.map(f => wit.toTypeRef(f.tpe))
        val ctor = MethodName.constructor(typeRefs)
        genNewScalaClass(fb, className, ctor) {
          for (f <- fields) {
            val fieldType = Flatten.flattenType(f.tpe)
            genLoadStack(fb, f.tpe, vi)
          }
        }

      case variant @ wit.VariantType(className, cases) =>
        val flattened = Flatten.flattenVariants(cases.map(_.tpe))
        genLoadVariant(
          fb,
          variant,
          () => vi.next(watpe.Int32),
          (tpe) => {
            val expect = Flatten.flattenType(tpe)
            genCoerceValues(fb, vi.copy(), flattened, expect)
            genLoadStack(fb, tpe, ValueIterator(fb, expect))
          },
          className
        )
        // drop values (we use the copied iterator for each cases)
        for (t <- flattened) { vi.skip(t) }

      case _ => ???
    }
  }

  private def genLoadVariant(
      fb: FunctionBuilder,
      variant: wit.VariantType,
      genGetIndex: () => Unit,
      genLoadValue: (wit.ValType) => Unit,
      className: ClassName
  )(implicit ctx: WasmContext): Unit = {
    val cases = variant.cases
    val flattened = Flatten.flattenVariants(cases.map(_.tpe))

    fb.switch(
      Sig(Nil, Nil),
      Sig(Nil, List(watpe.RefType(false, genTypeID.ObjectStruct)))
    )(
      genGetIndex
    )(
      cases.zipWithIndex.map { case (c, i) =>
        // TODO: maybe we shoulnd't despecialize? it seems we have some special case for specialized types
        // the constructor of result class will be j.l.Object
        // need to call specific constructor, and box the value
        val isResult = className == SpecialNames.WasmComponentResultClass
        val ctor =
          if (c.tpe == wit.VoidType)
            MethodName.constructor(Nil)
          else if (isResult)
            MethodName.constructor(List(ClassRef(ObjectClass)))
          else
            wit.makeCtorName(c.tpe)
        (List(i), () => {
          if (c.tpe == wit.VoidType) {
            fb += wa.Call(genFunctionID.loadModule(c.className))
          } else {
            genNewScalaClass(fb, c.className, ctor) {
              genLoadValue(c.tpe)
              c.tpe.toIRType match {
                case t: PrimTypeWithRef if isResult => genBox(fb, t)
                case ClassType(className, _) if isResult && ctx.getClassInfo(className).isWasmComponentResource =>
                  genBox(fb, IntType) // there're too much special case for resource class... let's add a new type for it?
                case _ =>
              }
            }
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
    // for (_ <- types.drop(expect.length)) {} // do nothing since there's no values on the stack
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

  private def genBox(fb: FunctionBuilder, primType: PrimTypeWithRef): Unit = {
    primType match {
      case NothingType | NullType =>
        throw new AssertionError(s"Unexpected boxing from $primType")
      case VoidType =>
      case p =>
        fb += wa.Call(genFunctionID.box(p.primRef))
    }
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

  private def genMovePtr(fb: FunctionBuilder, ptr: wanme.LocalID, tpe: wit.ValType): Unit =
    genMovePtr(fb, ptr, wit.elemSize(tpe))

  private def genMovePtr(fb: FunctionBuilder, ptr: wanme.LocalID, size: Int): Unit = {
    fb += wa.LocalGet(ptr)
    fb += wa.I32Const(size)
    fb += wa.I32Add
    fb += wa.LocalSet(ptr)
  }

}