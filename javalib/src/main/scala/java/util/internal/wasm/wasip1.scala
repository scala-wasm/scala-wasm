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

private[wasm] object wasip1 {
  val STDOUT = 1
  val STDERR = 2

  // Wasm intrinsic
  /** "wasi_snapshot_preview1", "fd_write" */
  def fdWrite(descriptor: Int, iovs: Int, iovsLen: Int, rp: Int): Int = throw new Error("stub")
}
