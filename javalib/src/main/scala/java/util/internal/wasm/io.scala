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

import scala.scalajs.js

object io {
  def printImpl(message: String, newLine: Boolean): Unit = {
    MemoryAllocator.withAllocator { allocator =>
      val chars = message.toCharArray()
      val bytes = new Array[Byte](chars.length)
      var i = 0
      while (i < chars.length) {
        bytes(i) = chars(i).toByte
        i += 1
      }
      wasiPrintImpl(allocator, bytes, newLine)
    }
  }
  private def wasiPrintImpl(
      allocator: MemoryAllocator,
      bytes: Array[Byte],
      newLine: Boolean
  ) = {
    val size = bytes.length
    val memorySize = size + (if (newLine) 1 else 0)
    val segment = allocator.allocate(memorySize)
    var offset = 0
    while (offset < bytes.length) {
      segment.setByte(offset, bytes(offset))
      offset += 1
    }
    if (newLine) {
      segment.setByte(memorySize - 1, 0x0A)
    }

    val iovs = allocator.allocate(8)
    iovs.setInt(0, segment.start)
    iovs.setInt(4, memorySize)

    val rp0 = allocator.allocate(4)

    val ret = wasip1.fdWrite(
      descriptor = wasip1.STDOUT,
      iovs = iovs.start,
      iovsLen = 1,
      rp = rp0.start
    )

    if (ret != 0) {
      // TODO: check return pointer's error code
    }
  }
}
