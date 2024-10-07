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

package java.util.internal.wasm

/** Memory segment on Wasm linear memory. */
private[java] final class MemorySegment(val start: Int, val size: Int) {

  def getByte(offset: Int): Byte = {
    validate(offset, 1)
    _loadByte(offset + start)
  }

  def getInt(offset: Int): Int = {
    validate(offset, 4)
    _loadInt(offset + start)
  }

  def setByte(offset: Int, value: Byte): Unit = {
    validate(offset, 1)
    _storeByte(offset + start, value)
  }

  def setInt(offset: Int, value: Int): Unit = {
    validate(offset, 4)
    _storeInt(offset + start, value)
  }

  // TODO
  // OptimizerCore can't find them to transform if we make them private
  // Maybe we should move the validation code into the generated code.
  // Wasm intrinsic
  def _loadByte(offset: Int): Byte = throw new Error("stub")
  // Wasm intrinsic
  def _loadInt(offset: Int): Int = throw new Error("stub")
  // Wasm intrinsic
  def _storeByte(offset: Int, value: Byte): Unit = throw new Error("stub")
  // Wasm intrinsic
  def _storeInt(offset: Int, value: Int): Unit = throw new Error("stub")

  private def validate(offset: Int, requiredBytes: Int): Unit =
    if (!(offset + requiredBytes >= 0 && offset + requiredBytes <= size)) {
      throw new OutOfMemoryError(s"") // TODO: support intToString in pure wasm
      // throw new OutOfMemoryError(s"MemorySegment.validate($requiredBytes)
      // failed, can't available $requiredBytes bytes")
  }
}
