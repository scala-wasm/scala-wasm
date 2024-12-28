package org.scalajs.linker.backend.wasmemitter.canonicalabi

import org.scalajs.ir.Types._
import org.scalajs.ir.OriginalName.NoOriginalName

import org.scalajs.linker.backend.webassembly.FunctionBuilder
import org.scalajs.linker.backend.webassembly.{Instructions => wa}
import org.scalajs.linker.backend.webassembly.{Types => watpe}
import org.scalajs.linker.backend.wasmemitter.VarGen._
import org.scalajs.linker.backend.wasmemitter.SpecialNames
import org.scalajs.linker.backend.wasmemitter.TypeTransformer._

import org.scalajs.linker.backend.webassembly.component.{Types => wit}
import org.scalajs.linker.backend.webassembly.component.Flatten


object ScalaJSToCABI {
  def genAdaptCABI(fb: FunctionBuilder, irType: Type): Unit = {
    irType match {
      // Scala.js has a same representation
      case BooleanType | ByteType | ShortType | IntType |
          CharType | LongType | FloatType | DoubleType =>

      case VoidType =>
        fb += wa.Drop // there shouldbe undef on the stack

      case tpe @ WasmComponentResultType(ok, err) =>
        val tmp = fb.addLocal("tmp", watpe.RefType(genTypeID.WasmComponentResultStruct))
        val okType = transformWIT(ok)
        val errType = transformWIT(err)
        val flattened = Flatten.flattenVariants(okType.toList ++ errType.toList)

        fb += wa.LocalTee(tmp)
        // ok => 0, err => 1
        fb += wa.RefTest(watpe.RefType(genTypeID.WasmComponentErrStruct))
        fb.block(flattened) { doneLabel =>
          fb.block(watpe.RefType(genTypeID.WasmComponentErrStruct)) { errLabel =>
            fb += wa.LocalGet(tmp)
            fb += wa.BrOnCast(
              errLabel,
              watpe.RefType(genTypeID.WasmComponentResultStruct),
              watpe.RefType(genTypeID.WasmComponentErrStruct)
            )
            // if it's VoidType, there's no value, drop
            fb += wa.RefCast(watpe.RefType(genTypeID.WasmComponentOkStruct))
            fb += wa.StructGet(
              genTypeID.WasmComponentOkStruct,
              genFieldID.forClassInstanceField(SpecialNames.wasmComponentOkValueFieldName)
            )
            genAdaptCABI(fb, ok)
            genCoerceValues(fb, Flatten.flattenType(okType), flattened)
            fb += wa.Br(doneLabel)
          }
          fb += wa.StructGet(
            genTypeID.WasmComponentErrStruct,
            genFieldID.forClassInstanceField(SpecialNames.wasmComponentErrValueFieldName)
          )
          genAdaptCABI(fb, err)
          genCoerceValues(fb, Flatten.flattenType(errType), flattened)
        }
      case _ => throw new AssertionError(s"Unexpected type: $irType")

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