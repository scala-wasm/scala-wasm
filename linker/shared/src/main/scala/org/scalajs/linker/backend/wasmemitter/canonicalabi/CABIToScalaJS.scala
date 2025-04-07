package org.scalajs.linker.backend.wasmemitter.canonicalabi

import org.scalajs.ir.Types._
import org.scalajs.ir.{WasmInterfaceTypes => wit}
import org.scalajs.ir.Names._
import org.scalajs.ir.WellKnownNames._
import org.scalajs.ir.Trees.MemberNamespace
import org.scalajs.ir.OriginalName.NoOriginalName
import org.scalajs.ir.ClassKind

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
import org.scalajs.linker.backend.wasmemitter.VarGen.genTypeID.ObjectStruct

object CABIToScalaJS {
  /** Load data from linear memory.
    * This generator expects an offset to be on the stack before loading.
    */
  def genLoadMemory(fb: FunctionBuilder, tpe: wit.ValType)(implicit ctx: WasmContext): Unit = {
    tpe match {
      case wit.VoidType => // do nothing
      case pt: wit.PrimValType =>
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
            val base = fb.addLocal(NoOriginalName, watpe.Int32)
            val stringOffset = fb.addLocal(NoOriginalName, watpe.Int32)
            fb += wa.LocalTee(base)
            fb += wa.I32Load() // offset
            fb += wa.LocalTee(stringOffset)
            fb += wa.LocalGet(base)
            fb += wa.I32Const(4)
            fb += wa.I32Add
            fb += wa.I32Load() // code units (UTF-16)
            fb += wa.Call(genFunctionID.cabiLoadString)

            // Save stack pointer to restore post-return
            // see: `genFunctionID.malloc`
            fb += wa.GlobalGet(genGlobalID.savedStackPointer)
            fb += wa.LocalGet(stringOffset)
            fb += wa.I32GtU
            fb.ifThen() { // if savedStackPointer > offset
              fb += wa.LocalGet(stringOffset)
              fb += wa.GlobalSet(genGlobalID.savedStackPointer)
            }
        }
        // genMovePtr(fb, ptr, tpe)

      case listType: wit.ListType =>
        listType.length match {
          case None =>
            val base = fb.addLocal(NoOriginalName, watpe.Int32)
            val listOffset = fb.addLocal(NoOriginalName, watpe.Int32)
            fb += wa.LocalTee(base)
            fb += wa.I32Load() // offset
            fb += wa.LocalTee(listOffset)

            fb += wa.LocalGet(base)
            fb += wa.I32Const(4)
            fb += wa.I32Add
            fb += wa.I32Load() // length
            genLoadVariableLengthList(fb, listType)

            fb += wa.GlobalGet(genGlobalID.savedStackPointer)
            fb += wa.LocalGet(listOffset)
            fb += wa.I32GtU
            fb.ifThen() { // if savedStackPointer > offset
              fb += wa.LocalGet(listOffset)
              fb += wa.GlobalSet(genGlobalID.savedStackPointer)
            }
          case Some(value) => ???
        }

      case flags: wit.FlagsType =>
        wit.elemSize(flags) match {
          case 1 => fb += wa.I32Load8U()
          case 2 => fb += wa.I32Load16U()
          case 4 => fb += wa.I32Load()
        }

      case wit.RecordType(className, fields) =>
        val typeRefs = fields.map(f => wit.toTypeRef(f.tpe))
        val ctor = MethodName.constructor(typeRefs)
        val ptr = fb.addLocal(NoOriginalName, watpe.Int32)
        fb += wa.LocalSet(ptr)
        genNewScalaClass(fb, className, ctor) {
          for (f <- fields) {
            genAlignTo(fb, wit.alignment(f.tpe), ptr)
            fb += wa.LocalGet(ptr)
            genLoadMemory(fb, f.tpe)
            genMovePtr(fb, ptr, wit.elemSize(f.tpe))
          }
        }
        // genAlignTo(fb, wit.alignment(tpe), ptr)

      case wit.TupleType(fields) =>
        val ctor = MethodName.constructor(fields.map(_ => ClassRef(ObjectClass)))
        val className = ClassName("scala.Tuple" + fields.size)
        val ptr = fb.addLocal(NoOriginalName, watpe.Int32)
        fb += wa.LocalSet(ptr)
        genNewScalaClass(fb, className, ctor) {
          for (f <- fields) {
            genAlignTo(fb, wit.alignment(f), ptr)
            fb += wa.LocalGet(ptr)
            genLoadMemory(fb, f)
            f.toIRType() match {
              case t: PrimTypeWithRef => genBox(fb, t)
              case _ =>
            }
            genMovePtr(fb, ptr, wit.elemSize(f))
          }
        }
        // genAlignTo(fb, wit.alignment(tpe), ptr)

      case wit.ResourceType(_) =>
        fb += wa.I32Load()

      case variant @ wit.VariantType(className, cases) =>
        genLoadVariantMemory(fb, cases, false)

      case wit.OptionType(t) =>
        val optionType = watpe.RefType.nullable(genTypeID.forClass(juOptionalClass))
        val maxCaseAlignment = wit.alignment(t)
        val ctorID = MethodName.constructor(List(ClassRef(ObjectClass)))

        val ptr = fb.addLocal(NoOriginalName, watpe.Int32)

        // load case index
        fb += wa.LocalTee(ptr)
        fb += wa.I32Load8U()

        // move pointer
        genMovePtr(fb, ptr, 1)
        genAlignTo(fb, maxCaseAlignment, ptr)

        fb.ifThenElse(optionType) {
          // non-null
          genNewScalaClass(fb, juOptionalClass, ctorID) {
            fb += wa.LocalGet(ptr)
            genLoadMemory(fb, t)
            t.toIRType match {
              case t: PrimTypeWithRef => genBox(fb, t)
              case _ =>
            }
          }
        } {
          genNewScalaClass(fb, juOptionalClass, ctorID) {
            fb += wa.RefNull(watpe.HeapType.Any)
          }
        }

      case result @ wit.ResultType(ok, err) =>
        val cases = List(
          wit.CaseType(ComponentResultOkClass, ok),
          wit.CaseType(ComponentResultErrClass, err)
        )
        genLoadVariantMemory(fb, cases, true)

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
        val offset = fb.addLocal(NoOriginalName, watpe.Int32)
        vi.next(watpe.Int32)
        fb += wa.LocalTee(offset)
        vi.next(watpe.Int32)
        fb += wa.Call(genFunctionID.cabiLoadString)

        // Save stack pointer to restore post-return
        // see: `genFunctionID.malloc`
        fb += wa.GlobalGet(genGlobalID.savedStackPointer)
        fb += wa.LocalGet(offset)
        fb += wa.I32GtU
        fb.ifThen() { // if savedStackPointer > offset
          fb += wa.LocalGet(offset)
          fb += wa.GlobalSet(genGlobalID.savedStackPointer)
        }

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

      case listType: wit.ListType =>
        listType.length match {
          case None =>
            val offset = fb.addLocal(NoOriginalName, watpe.Int32)
            // val length = fb.addLocal(NoOriginalName, watpe.Int32)
            vi.next(watpe.Int32)
            fb += wa.LocalTee(offset)
            vi.next(watpe.Int32)
            genLoadVariableLengthList(fb, listType)

            // Save stack pointer to restore post-return
            // see: `genFunctionID.malloc`
            fb += wa.GlobalGet(genGlobalID.savedStackPointer)
            fb += wa.LocalGet(offset)
            fb += wa.I32GtU
            fb.ifThen() { // if savedStackPointer > offset
              fb += wa.LocalGet(offset)
              fb += wa.GlobalSet(genGlobalID.savedStackPointer)
            }
          case Some(value) => ???
        }

      case flags: wit.FlagsType =>
        vi.next(watpe.Int32)

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
        genLoadVariantStack(
          fb,
          cases,
          vi,
          boxValue = false
        )

      case wit.OptionType(t) =>
        val optionType = watpe.RefType(genTypeID.forClass(juOptionalClass))
        val ctorID = MethodName.constructor(List(ClassRef(ObjectClass)))

        vi.next(watpe.Int32)
        fb.ifThenElse(optionType) {
          // non-null
          genNewScalaClass(fb, juOptionalClass, ctorID) {
            genLoadStack(fb, t, vi)
            t.toIRType match {
              case t: PrimTypeWithRef => genBox(fb, t)
              case _ =>
            }
          }
        } {
          genNewScalaClass(fb, juOptionalClass, ctorID) {
            fb += wa.RefNull(watpe.HeapType.Any)
          }
        }


      case wit.ResultType(ok, err) =>
        val cases = List(
          wit.CaseType(ComponentResultOkClass, ok),
          wit.CaseType(ComponentResultErrClass, err)
        )
        genLoadVariantStack(
          fb,
          cases,
          vi,
          boxValue = true
        )

      case _ => ???
    }
  }

  private def genLoadVariableLengthList(
    fb: FunctionBuilder,
    listType: wit.ListType
  )(implicit ctx: WasmContext): Unit = {
    val wit.ListType(elemType, maybeLength) = listType
    assert(maybeLength.isEmpty)
    val arrayTypeRef = ArrayTypeRef.of(wit.toTypeRef(elemType))
    val arrayUnderlyingID = genTypeID.underlyingOf(arrayTypeRef)
    val arrayStructTypeID = genTypeID.forArrayClass(arrayTypeRef)
    val elemSize = wit.elemSize(elemType)

    val base = fb.addLocal(NoOriginalName, watpe.Int32)
    val length = fb.addLocal(NoOriginalName, watpe.Int32)
    val iLocal = fb.addLocal("iLocal", watpe.Int32)
    val array = fb.addLocal("array", watpe.RefType(arrayUnderlyingID))

    fb += wa.LocalSet(length)
    fb += wa.LocalSet(base)

    fb += wa.LocalGet(length)
    fb += wa.ArrayNewDefault(arrayUnderlyingID)
    fb += wa.LocalSet(array)

    fb += wa.I32Const(0)
    fb += wa.LocalSet(iLocal)

    fb.block() { exit =>
      fb.loop() { loop =>
        // if i >= units, break
        fb += wa.LocalGet(iLocal)
        fb += wa.LocalGet(length)
        fb += wa.I32GeU
        fb += wa.BrIf(exit)

        // for array.set
        fb += wa.LocalGet(array)
        fb += wa.LocalGet(iLocal)

        fb += wa.LocalGet(base)
        fb += wa.LocalGet(iLocal)
        fb += wa.I32Const(elemSize)
        fb += wa.I32Mul
        fb += wa.I32Add
        genLoadMemory(fb, elemType)

        fb += wa.ArraySet(arrayUnderlyingID)

        fb += wa.LocalGet(iLocal)
        fb += wa.I32Const(1)
        fb += wa.I32Add
        fb += wa.LocalSet(iLocal)

        fb += wa.Br(loop)
      }
    }

    // TODO: we reach here only when targetPureWasm = treu
    SWasmGen.genLoadVTableAndITableForArray(fb, arrayTypeRef, targetPureWasm = true)
    fb += wa.LocalGet(array)
    fb += wa.StructNew(genTypeID.forArrayClass(arrayTypeRef))
  }

  private def genLoadVariantMemory(
    fb: FunctionBuilder,
    cases: List[wit.CaseType],
    boxValue: Boolean
  )(implicit ctx: WasmContext): Unit = {
    val ptr = fb.addLocal(NoOriginalName, watpe.Int32)
    fb += wa.LocalSet(ptr)

    val flattened = Flatten.flattenVariants(cases.map(_.tpe))
    fb.switch(
      Sig(Nil, Nil),
      Sig(Nil, List(watpe.RefType(false, genTypeID.ObjectStruct)))
    )( () => {
      fb += wa.LocalGet(ptr)
      genLoadMemory(fb, wit.discriminantType(cases)) // load i32 (case index) from memory
      genMovePtr(fb, ptr, wit.elemSize(wit.discriminantType(cases)))
      genAlignTo(fb, wit.maxCaseAlignment(cases), ptr)
    })(
      cases.zipWithIndex.map { case (c, i) =>
        (List(i), () => {
          val isModule = ctx.getClassInfo(c.className).kind == ClassKind.ModuleClass
          if (isModule) {
            fb += wa.Call(genFunctionID.loadModule(c.className))
          } else {
            val ctorID =
              if (boxValue) MethodName.constructor(List(ClassRef(ObjectClass)))
              else wit.makeCtorName(c.tpe)
            genNewScalaClass(fb, c.className, ctorID) {
              if (c.tpe == wit.VoidType) fb += wa.GlobalGet(genGlobalID.undef)
              else {
                fb += wa.LocalGet(ptr)
                genLoadMemory(fb, c.tpe)
                if (boxValue) {
                  c.tpe.toIRType match {
                    case t: PrimTypeWithRef => genBox(fb, t)
                    case _ =>
                  }
                }
              }
            }
          }
        })
      }: _*
    ) { () =>
      fb += wa.Unreachable
    }
    // genAlignTo(fb, alignment, ptr)
  }

  private def genLoadVariantStack(
    fb: FunctionBuilder,
    cases: List[wit.CaseType],
    vi: ValueIterator,
    boxValue: Boolean,
  )(implicit ctx: WasmContext): Unit = {
    // val idx = fb.addLocal(NoOriginalName, watpe.Int32)
    val flattened = Flatten.flattenVariants(cases.map(_.tpe))
    fb.switch(
      Sig(Nil, Nil),
      Sig(Nil, List(watpe.RefType(false, genTypeID.ObjectStruct)))
    ) { () => vi.next(watpe.Int32)
    } (
      cases.zipWithIndex.map { case (c, i) =>
        // While the variant uses `case object`` when the value type is Unit,
        // the Result/Option type still uses `case class Ok(())`` even when the value type is Unit.
        (List(i), () => {
          val isModule = ctx.getClassInfo(c.className).kind == ClassKind.ModuleClass
          if (isModule) {
            fb += wa.Call(genFunctionID.loadModule(c.className))
          } else {
            val ctorID =
              if (boxValue) MethodName.constructor(List(ClassRef(ObjectClass)))
              else wit.makeCtorName(c.tpe)
            genNewScalaClass(fb, c.className, ctorID) {
              if (c.tpe == wit.VoidType) fb += wa.GlobalGet(genGlobalID.undef)
              else {
                val expect = Flatten.flattenType(c.tpe)
                genCoerceValues(fb, vi.copy(), flattened, expect)
                genLoadStack(fb, c.tpe, ValueIterator(fb, expect))
                if (boxValue) {
                  c.tpe.toIRType match {
                    case t: PrimTypeWithRef => genBox(fb, t)
                    case _ =>
                  }
                }
              }
            }
          }
        })
      }: _*
    ) { () =>
      fb += wa.Unreachable
    }
    for (t <- flattened) { vi.skip(t) }
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
    // for (_ <- types.drop(expect.length)) {  }
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
      case ByteType | ShortType =>
        fb += wa.RefI31
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