package org.scalajs.linker.backend.wasmemitter.canonicalabi

import org.scalajs.ir.Types._
import org.scalajs.ir.{WasmInterfaceTypes => wit}
import org.scalajs.ir.OriginalName.NoOriginalName
import org.scalajs.ir.Names._

import org.scalajs.linker.backend.webassembly.FunctionBuilder
import org.scalajs.linker.backend.webassembly.{Instructions => wa}
import org.scalajs.linker.backend.webassembly.{Types => watpe}
import org.scalajs.linker.backend.webassembly.{Identitities => wanme}
import org.scalajs.linker.backend.webassembly.Types.{FunctionType => Sig}
import org.scalajs.linker.backend.webassembly.component.Flatten

import org.scalajs.linker.backend.wasmemitter.WasmContext
import org.scalajs.linker.backend.wasmemitter.VarGen._
import org.scalajs.linker.backend.wasmemitter.SpecialNames
import org.scalajs.linker.backend.wasmemitter.TypeTransformer.transformSingleType
import org.scalajs.ir.WasmInterfaceTypes.U8Type


object ScalaJSToCABI {

  // assume that there're ptr and value of `tpe` are on the stack.
  def genStoreMemory(fb: FunctionBuilder, tpe: wit.ValType): Unit = {
    tpe match {
      case wit.VoidType =>
      case wit.BoolType | wit.S8Type | wit.U8Type =>
        fb += wa.I32Store8()
      case wit.S16Type | wit.U16Type =>
        fb += wa.I32Store16()
      case wit.S32Type | wit.U32Type =>
        fb += wa.I32Store()
      case wit.S64Type | wit.U64Type =>
        fb += wa.I64Store()
      case wit.F32Type =>
        fb += wa.F32Store()
      case wit.F64Type =>
        fb += wa.F64Store()
      case wit.CharType =>
        fb += wa.I32Store()

      case wit.StringType =>
        val ptr = fb.addLocal(NoOriginalName, watpe.Int32)
        val offset = fb.addLocal(NoOriginalName, watpe.Int32)
        val units = fb.addLocal(NoOriginalName, watpe.Int32)
        fb += wa.Call(genFunctionID.cabiStoreString)
        // now we have [i32(string offset), i32(string units)] on stack
        fb += wa.LocalSet(units)
        fb += wa.LocalSet(offset)

        fb += wa.LocalTee(ptr)
        fb += wa.LocalGet(offset)
        fb += wa.I32Store()

        fb += wa.LocalGet(ptr)
        fb += wa.I32Const(4)
        fb += wa.I32Add
        fb += wa.LocalGet(units)
        fb += wa.I32Store()

      case wit.TupleType(fields) =>
        val className = ClassName("scala.Tuple" + fields.size)
        val ptr = fb.addLocal(NoOriginalName, watpe.Int32)
        val tuple = fb.addLocal(NoOriginalName, watpe.RefType.nullable(genTypeID.forClass(className)))
        fb += wa.LocalSet(tuple)
        fb += wa.LocalSet(ptr)

        var offset = 0
        for ((f, i) <- fields.zipWithIndex) {
          fb += wa.LocalGet(ptr)
          if (offset > 0) {
            fb += wa.I32Const(offset)
            fb += wa.I32Add
          }
          fb += wa.LocalGet(tuple)
          fb += wa.StructGet(
            genTypeID.forClass(className),
            genFieldID.forClassInstanceField(FieldName(className, SimpleFieldName(s"_${i + 1}")))
          )
          f.toIRType() match {
            case t: PrimTypeWithRef => genUnbox(fb, t)
            case _ =>
          }
          genStoreMemory(fb, f)
          offset += wit.elemSize(f)
        }

      case wit.RecordType(className, fields) =>
        val ptr = fb.addLocal(NoOriginalName, watpe.Int32)
        // TODO: NPE if null
        val record = fb.addLocal(NoOriginalName, watpe.RefType.nullable(genTypeID.forClass(className)))
        fb += wa.LocalSet(record)
        fb += wa.LocalSet(ptr)

        var offset = 0
        for (f <- fields) {
          fb += wa.LocalGet(ptr)
          if (offset > 0) {
            fb += wa.I32Const(offset)
            fb += wa.I32Add
          }
          fb += wa.LocalGet(record)
          fb += wa.StructGet(
            genTypeID.forClass(className),
            genFieldID.forClassInstanceField(f.label)
          )
          genStoreMemory(fb, f.tpe)
          offset += wit.elemSize(f.tpe)
        }

      case wit.VariantType(_, cases) =>
        val ptr = fb.addLocal(NoOriginalName, watpe.Int32)
        val variant = fb.addLocal(NoOriginalName, watpe.RefType.anyref)

        val flattened = Flatten.flattenVariants(cases.map(t => t.tpe))
        fb += wa.LocalSet(variant)
        fb += wa.LocalSet(ptr)

        fb.switchByType(Nil) {
          () => fb += wa.LocalGet(variant)
        } {
          cases.map { c =>
            val classID = genTypeID.forClass(c.className)
            val index = genFieldID.forClassInstanceField(FieldName(c.className, SimpleFieldName("_index")))
            val value = genFieldID.forClassInstanceField(FieldName(c.className, SimpleFieldName("value")))
            (c.className, () => {
              fb += wa.Drop

              fb += wa.LocalGet(ptr)
              fb += wa.LocalGet(variant)
              fb += wa.RefCast(watpe.RefType(genTypeID.forClass(c.className)))
              fb += wa.StructGet(classID, index)
              wit.discriminantType(cases) match {
                case wit.U8Type => fb += wa.I32Store8()
                case wit.U16Type => fb += wa.I32Store16()
                case wit.U32Type => fb += wa.I32Store()
                case t => throw new AssertionError(s"Invalid discriminant type $t")
              }
              genMovePtr(fb, ptr, wit.discriminantType(cases))
              genAlignTo(fb, wit.maxCaseAlignment(cases), ptr)

              fb += wa.LocalGet(ptr)
              fb += wa.LocalGet(variant)
              fb += wa.RefCast(watpe.RefType(genTypeID.forClass(c.className)))
              fb += wa.StructGet(classID, value)
              genStoreMemory(fb, c.tpe)
            })
          }: _*
        } { () =>
          fb += wa.Unreachable
        }

      case wit.ResourceType(_) =>
        fb += wa.I32Store()

      case _ => ???
    }
  }

  def genStoreStack(fb: FunctionBuilder, tpe: wit.ValType)(implicit ctx: WasmContext): Unit = {
    tpe match {
      case wit.VoidType => fb += wa.Drop
      // Scala.js has a same representation
      case wit.BoolType | wit.S8Type | wit.S16Type | wit.S32Type | wit.S64Type |
          wit.U8Type | wit.U16Type | wit.U32Type | // i32
          wit.U64Type | wit.CharType |
          wit.F32Type | wit.F64Type =>

      case wit.ResourceType(_) =>

      case tpe @ wit.ListType(elemType, maybeLength) =>
        maybeLength match {
          case None =>
            // array
            val arrType = transformSingleType(tpe.toIRType())
            val arrayTypeRef = ArrayTypeRef.of(wit.toTypeRef(elemType))
            val arrayStructTypeID = genTypeID.forArrayClass(arrayTypeRef)

            val arr = fb.addLocal(NoOriginalName, arrType)
            val len = fb.addLocal(NoOriginalName, watpe.Int32)
            val iLocal = fb.addLocal(NoOriginalName, watpe.Int32)
            val baseAddr = fb.addLocal(NoOriginalName, watpe.Int32)

            val size = wit.elemSize(elemType)

            fb += wa.LocalTee(arr)

            fb += wa.StructGet(arrayStructTypeID, genFieldID.objStruct.arrayUnderlying)
            fb += wa.ArrayLen
            fb += wa.LocalTee(len)
            fb += wa.I32Const(size)
            fb += wa.I32Mul // required bytes to store array on linear memory

            fb += wa.Call(genFunctionID.malloc) // base address
            fb += wa.LocalSet(baseAddr)

            fb += wa.I32Const(0)
            fb += wa.LocalSet(iLocal)

            fb.block() { exit =>
              fb.loop() { loop =>
                fb += wa.LocalGet(iLocal)
                fb += wa.LocalGet(len)
                fb += wa.I32Eq
                fb += wa.BrIf(exit)

                // addr to store
                fb += wa.LocalGet(baseAddr)
                fb += wa.LocalGet(iLocal)
                fb += wa.I32Const(size)
                fb += wa.I32Mul
                fb += wa.I32Add

                // value
                fb += wa.LocalGet(arr)
                fb += wa.LocalGet(iLocal)
                fb += wa.Call(genFunctionID.arrayGetFor(ArrayTypeRef.of(wit.toTypeRef(elemType))))
                genStoreMemory(fb, elemType)

                // i := i + 1
                fb += wa.LocalGet(iLocal)
                fb += wa.I32Const(1)
                fb += wa.I32Add
                fb += wa.LocalSet(iLocal)

                fb += wa.Br(loop)
              }

            }

            fb += wa.LocalGet(baseAddr)
            fb += wa.LocalGet(len)

          case Some(value) => ???
        }

      case wit.StringType =>
        // array i16
        fb += wa.RefAsNonNull // TODO NPE if null
        fb += wa.Call(genFunctionID.cabiStoreString) // baseAddr, units

      case wit.TupleType(fields) =>
        val className = ClassName("scala.Tuple" + fields.size)
        val record = fb.addLocal(NoOriginalName, watpe.RefType.nullable(genTypeID.forClass(className)))
        fb += wa.LocalSet(record)
        for ((f, i) <- fields.zipWithIndex) {
          fb += wa.LocalGet(record)
          fb += wa.StructGet(
            genTypeID.forClass(className),
            genFieldID.forClassInstanceField(FieldName(className, SimpleFieldName(s"_${i + 1}")))
          )
          f.toIRType() match {
            case t: PrimTypeWithRef => genUnbox(fb, t)
            case _ =>
          }
          genStoreStack(fb, f)
        }

      case wit.RecordType(className, fields) =>
        // TODO: NPE if null
        val record = fb.addLocal(NoOriginalName, watpe.RefType.nullable(genTypeID.forClass(className)))
        fb += wa.LocalSet(record)
        for (f <- fields) {
          fb += wa.LocalGet(record)
          fb += wa.StructGet(
            genTypeID.forClass(className),
            genFieldID.forClassInstanceField(f.label)
          )
          genStoreStack(fb, f.tpe)
        }

      case wit.VariantType(_, cases) =>
        val tmp = fb.addLocal(NoOriginalName, watpe.RefType.anyref)
        val flattened = Flatten.flattenVariants(cases.map(t => t.tpe))
        fb += wa.LocalSet(tmp)

        fb.switchByType(watpe.Int32 +: flattened) {
          () =>
            fb += wa.LocalGet(tmp)
        } {
          cases.map { case c =>
            val classID = genTypeID.forClass(c.className)
            val index = genFieldID.forClassInstanceField(FieldName(c.className, SimpleFieldName("_index")))
            val value = genFieldID.forClassInstanceField(FieldName(c.className, SimpleFieldName("value")))
            (c.className, () => {
              val l = fb.addLocal(NoOriginalName, watpe.RefType.nullable(genTypeID.forClass(c.className)))
              fb += wa.LocalTee(l)
              fb += wa.StructGet(classID, index)
              fb += wa.LocalGet(l)
              fb += wa.StructGet(classID, value)
              genStoreStack(fb, c.tpe)
              genCoerceValues(fb, Flatten.flattenType(c.tpe), flattened)
            })
          }: _*
        } { () =>
          fb += wa.Unreachable
        }

      case _ => throw new AssertionError(s"Unexpected type: $tpe")

    }
  }

  private def genCoerceValues(
    fb: FunctionBuilder,
    types: List[watpe.Type],
    expect: List[watpe.Type]
  ): Unit = {
    val locals = types.map(t => fb.addLocal(NoOriginalName, t))
    locals.reverse.foreach { l =>
      fb += wa.LocalSet(l)
    }
    locals.zip(types).zip(expect).map { case ((l, have), want) =>
      fb += wa.LocalGet(l)
      (have, want) match {
        case (watpe.Float32, watpe.Int32) =>
          fb += wa.I32ReinterpretF32
        case (watpe.Int32, watpe.Int64) =>
          fb += wa.I64Extend32S
        case (watpe.Float32, watpe.Int64) =>
          fb += wa.I32ReinterpretF32
          fb += wa.I64Extend32S
        case (watpe.Float64, watpe.Int64) =>
          fb += wa.I64ReinterpretF64
        case _ => assert(have == want)
      }
    }
    expect.drop(types.length).foreach { t =>
      t match {
        case watpe.Int32 =>
          fb += wa.I32Const(0)
        case watpe.Int64 =>
          fb += wa.I64Const(0L)
        case watpe.Float32 =>
          fb += wa.F32Const(0)
        case watpe.Float64 =>
          fb += wa.F64Const(0.0)
        case _ =>
          throw new AssertionError(s"Illegal core wasm type: $t")
      }
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

  private def genUnbox(fb: FunctionBuilder, targetTpe: PrimType): Unit = {
    targetTpe match {
      case NothingType | NullType | UndefType =>
        throw new AssertionError(s"Unexpected unboxing to $targetTpe")
      case StringType | VoidType =>
      case p: PrimTypeWithRef =>
        fb += wa.Call(genFunctionID.unbox(p.primRef))
    }
  }
}