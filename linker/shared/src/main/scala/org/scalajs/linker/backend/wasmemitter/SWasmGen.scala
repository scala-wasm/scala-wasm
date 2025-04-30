/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package org.scalajs.linker.backend.wasmemitter

import org.scalajs.ir.Types._
import org.scalajs.ir.WellKnownNames._

import org.scalajs.linker.backend.webassembly._
import org.scalajs.linker.backend.webassembly.Instructions._

import VarGen._
import org.scalajs.linker.backend.webassembly.Types.HeapType
import org.scalajs.linker.backend.webassembly.Identitities.LocalID

/** Scala.js-specific Wasm generators that are used across the board. */
object SWasmGen {

  def genZeroOf(tpe: Type)(implicit ctx: WasmContext): Instr = {
    tpe match {
      case BooleanType | CharType | ByteType | ShortType | IntType =>
        I32Const(0)

      case LongType   => I64Const(0L)
      case FloatType  => F32Const(0.0f)
      case DoubleType => F64Const(0.0)
      case StringType =>
        if (ctx.coreSpec.wasmFeatures.targetPureWasm)
          GlobalGet(genGlobalID.emptyStringArray)
        else
          ctx.stringPool.getEmptyStringInstr()
      case UndefType  => GlobalGet(genGlobalID.undef)

      case ClassType(className, nullable)
          if ctx.getClassInfo(className).isWasmComponentResource =>
        I32Const(0)
      case ClassType(BoxedStringClass, true) =>
        if (ctx.coreSpec.wasmFeatures.targetPureWasm)
          RefNull(HeapType(genTypeID.wasmString))
        else
          RefNull(Types.HeapType.NoExtern)

      case AnyType | ClassType(_, true) | ArrayType(_, true) | ClosureType(_, _, true) | NullType =>
        RefNull(Types.HeapType.None)

      case NothingType | VoidType | ClassType(_, false) | ArrayType(_, false) |
          ClosureType(_, _, false) | AnyNotNullType | _:RecordType =>
        throw new AssertionError(s"Unexpected type for field: ${tpe.show()}")
    }
  }

  def genZeroOf(tpe: Types.Type): Instr = tpe match {
    case Types.Int32                   => I32Const(0)
    case Types.Int64                   => I64Const(0L)
    case Types.Float32                 => F32Const(0.0f)
    case Types.Float64                 => F64Const(0.0)
    case Types.RefType(true, heapType) => RefNull(heapType)

    case Types.RefType(false, _) =>
      throw new AssertionError(s"Illegal Wasm type for genZeroOf: $tpe")
  }

  def genLoadTypeData(fb: FunctionBuilder, typeRef: TypeRef): Unit = typeRef match {
    case typeRef: NonArrayTypeRef  => genLoadNonArrayTypeData(fb, typeRef)
    case typeRef: ArrayTypeRef     => genLoadArrayTypeData(fb, typeRef)
    case typeRef: TransientTypeRef => throw new IllegalArgumentException(typeRef.toString())
  }

  def genLoadNonArrayTypeData(fb: FunctionBuilder, typeRef: NonArrayTypeRef): Unit = {
    fb += GlobalGet(genGlobalID.forVTable(typeRef))
  }

  def genLoadArrayTypeData(fb: FunctionBuilder, arrayTypeRef: ArrayTypeRef): Unit = {
    genLoadNonArrayTypeData(fb, arrayTypeRef.base)
    fb += I32Const(arrayTypeRef.dimensions)
    fb += Call(genFunctionID.arrayTypeData)
  }

  def genArrayValue(fb: FunctionBuilder, arrayTypeRef: ArrayTypeRef,
      length: Int, targetPureWasm: Boolean)(
      genElems: => Unit): Unit = {
    genLoadArrayTypeData(fb, arrayTypeRef) // vtable
    if (targetPureWasm) fb += I32Const(0)

    // Create the underlying array
    genElems
    val underlyingArrayType = genTypeID.underlyingOf(arrayTypeRef)
    fb += ArrayNewFixed(underlyingArrayType, length)

    // Create the array object
    fb += StructNew(genTypeID.forArrayClass(arrayTypeRef))
  }

  /** Generates code that forwards an exception from a function call that always throws.
   *
   *  After this codegen, the stack is in a stack-polymorphic context.
   *
   *  This method assumes that there is no enclosing exception handler in the
   *  current function.
   */
  def genForwardThrowAlwaysAsReturn(fb: FunctionBuilder, fakeResult: List[Instr])(
      implicit ctx: WasmContext): Unit = {
    if (ctx.coreSpec.wasmFeatures.exceptionHandling) {
      fb += Unreachable
    } else {
      fb ++= fakeResult
      fb += Return
    }
  }

  /** Generates code that possibly forwards an exception from the previous function call.
   *
   *  The stack is not altered by this codegen.
   *
   *  This method assumes that there is no enclosing exception handler in the
   *  current function.
   */
  def genForwardThrowAsReturn(fb: FunctionBuilder, fakeResult: List[Instr])(
      implicit ctx: WasmContext): Unit = {
    if (!ctx.coreSpec.wasmFeatures.exceptionHandling) {
      fb += GlobalGet(genGlobalID.isThrowing)
      fb.ifThen() {
        fb ++= fakeResult
        fb += Return
      }
    }
  }

  def genWasmStringFromCharCode(fb: FunctionBuilder): Unit = {
    fb += ArrayNewFixed(genTypeID.i16Array, 1)
    fb += I32Const(1)
    fb += RefNull(HeapType(genTypeID.wasmString))
    fb += StructNew(genTypeID.wasmString)
  }

  def genWasmStringFromArray(fb: FunctionBuilder, array: LocalID): Unit = {
    fb += LocalGet(array)
    fb += LocalGet(array)
    fb += ArrayLen
    fb += RefNull(HeapType(genTypeID.wasmString))
    fb += StructNew(genTypeID.wasmString)
  }

}
