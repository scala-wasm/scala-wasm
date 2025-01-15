package org.scalajs.linker.backend.wasmemitter.canonicalabi

import org.scalajs.ir.{Types => jstpe}
import org.scalajs.ir.{WasmInterfaceTypes => wit}
import org.scalajs.ir.OriginalName.NoOriginalName
import org.scalajs.ir.Names._

import org.scalajs.linker.backend.webassembly.FunctionBuilder
import org.scalajs.linker.backend.webassembly.{Instructions => wa}
import org.scalajs.linker.backend.webassembly.{Types => watpe}
import org.scalajs.linker.backend.webassembly.Types.{FunctionType => Sig}
import org.scalajs.linker.backend.webassembly.component.Flatten

import org.scalajs.linker.backend.wasmemitter.WasmContext
import org.scalajs.linker.backend.wasmemitter.VarGen._
import org.scalajs.linker.backend.wasmemitter.SpecialNames
import org.scalajs.linker.backend.wasmemitter.TypeTransformer._


object ScalaJSToCABI {
  def genAdaptCABI(fb: FunctionBuilder, tpe: wit.WasmInterfaceType)(implicit ctx: WasmContext): Unit = {
    tpe match {
      case wit.VoidType =>
        fb += wa.Drop
      // Scala.js has a same representation
      case wit.BoolType | wit.S8Type | wit.S16Type | wit.S32Type | wit.S64Type |
          wit.U8Type | wit.U16Type | wit.U32Type | wit.U64Type |
          wit.F32Type | wit.F64Type =>

      // case VoidType =>
      //   fb += wa.Drop // there shouldbe undef on the stack

      case wit.ResourceType(_) =>

      case wit.StringType =>
        // array i16
        val str = fb.addLocal(NoOriginalName, watpe.RefType(true, genTypeID.i16Array))
        val baseAddr = fb.addLocal(NoOriginalName, watpe.Int32)
        val iLocal = fb.addLocal(NoOriginalName, watpe.Int32)
        fb += wa.LocalTee(str)

        // required bytes
        fb += wa.ArrayLen
        fb += wa.I32Const(2)
        fb += wa.I32Mul
        fb += wa.Call(genFunctionID.malloc)
        fb += wa.LocalSet(baseAddr)

        // i := 0
        fb += wa.I32Const(0)
        fb += wa.LocalSet(iLocal)

        fb.block() { exit =>
          fb.loop() { loop =>
            fb += wa.LocalGet(iLocal)
            fb += wa.LocalGet(str)
            fb += wa.ArrayLen
            fb += wa.I32Eq
            fb.ifThen() {
              fb += wa.Br(exit)
            }
            // store
            // position (baseAddr + i * 2)
            fb += wa.LocalGet(baseAddr)
            fb += wa.LocalGet(iLocal)
            fb += wa.I32Const(2)
            fb += wa.I32Mul
            fb += wa.I32Add

            // value
            fb += wa.LocalGet(str)
            fb += wa.LocalGet(iLocal)
            fb += wa.ArrayGetU(genTypeID.i16Array) // i32 here
            fb += wa.I32Store16() // store 2 bytes

            // i := i + 1
            fb += wa.LocalGet(iLocal)
            fb += wa.I32Const(1)
            fb += wa.I32Add
            fb += wa.LocalSet(iLocal)
            fb += wa.Br(loop)
          }
        }
        fb += wa.LocalGet(baseAddr) // offset
        fb += wa.LocalGet(str)
        fb += wa.ArrayLen
        fb += wa.I32Const(2)
        fb += wa.I32Mul // byte length

      case wit.VariantType(_, cases) =>
        val tmp = fb.addLocal(NoOriginalName, watpe.RefType.anyref)
        val flattened = Flatten.flattenVariants(cases.map(t => t.tpe))
        fb += wa.LocalSet(tmp)

        fb.switchByType(watpe.Int32 +: flattened) {
          () =>
            fb += wa.LocalGet(tmp)
        } {
          cases.map { case c =>
            (c.className, () => {
              val l = fb.addLocal(NoOriginalName, watpe.RefType(genTypeID.forClass(c.className)))
              fb += wa.LocalTee(l)
              fb += wa.StructGet(
                genTypeID.forClass(c.className),
                genFieldID.forClassInstanceField(
                  FieldName(c.className, SimpleFieldName("_index"))
                )
              )
              fb += wa.LocalGet(l)
              fb += wa.StructGet(
                genTypeID.forClass(c.className),
                genFieldID.forClassInstanceField(
                  FieldName(c.className, SimpleFieldName("value"))
                )
              )
              genAdaptCABI(fb, c.tpe)
              genCoerceValues(fb, Flatten.flattenType(c.tpe), flattened)
            })
          }: _*
        } { () =>
          fb += wa.Unreachable
        }


      // case tpe @ WasmComponentResultType(ok, err) =>
      //   val tmp = fb.addLocal("tmp", watpe.RefType(genTypeID.WasmComponentResultStruct))
      //   val okType = transformWIT(ok)
      //   val errType = transformWIT(err)
      //   val flattened = Flatten.flattenVariants(okType.toList ++ errType.toList)

      //   fb += wa.LocalTee(tmp)
      //   // ok => 0, err => 1
      //   fb += wa.RefTest(watpe.RefType(genTypeID.WasmComponentErrStruct))
      //   fb.block(flattened) { doneLabel =>
      //     fb.block(watpe.RefType(genTypeID.WasmComponentErrStruct)) { errLabel =>
      //       fb += wa.LocalGet(tmp)
      //       fb += wa.BrOnCast(
      //         errLabel,
      //         watpe.RefType(genTypeID.WasmComponentResultStruct),
      //         watpe.RefType(genTypeID.WasmComponentErrStruct)
      //       )
      //       // if it's VoidType, there's no value, drop
      //       fb += wa.RefCast(watpe.RefType(genTypeID.WasmComponentOkStruct))
      //       fb += wa.StructGet(
      //         genTypeID.WasmComponentOkStruct,
      //         genFieldID.forClassInstanceField(SpecialNames.wasmComponentOkValueFieldName)
      //       )
      //       genAdaptCABI(fb, ok)
      //       genCoerceValues(fb, Flatten.flattenType(okType), flattened)
      //       fb += wa.Br(doneLabel)
      //     }
      //     fb += wa.StructGet(
      //       genTypeID.WasmComponentErrStruct,
      //       genFieldID.forClassInstanceField(SpecialNames.wasmComponentErrValueFieldName)
      //     )
      //     genAdaptCABI(fb, err)
      //     genCoerceValues(fb, Flatten.flattenType(errType), flattened)
      //   }
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
}