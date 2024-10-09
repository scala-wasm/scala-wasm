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

private[java] class MemoryAllocator {
  // Wasm intrinsic
  def allocate(size: Int): MemorySegment = throw new Error("stub")
  // Wasm intrinsic
  def free(): Unit = throw new Error("stub")
}

private[java] object MemoryAllocator {
  def withAllocator(block: MemoryAllocator => Unit): Unit = {
    val allocator = new MemoryAllocator()
    try {
      block(allocator)
    } finally {
      allocator.free()
    }
  }
}
