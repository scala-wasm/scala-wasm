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

package org.scalajs.wasmlibintf

import wasmsystemtest.wasi.cli.{stderr, stdout}
import wasmsystemtest.wasi.clocks.{monotonic_clock, wall_clock}
import wasmsystemtest.wasi.io.streams.OutputStream
import wasmsystemtest.wasi.random.{random => wasiRandom}
import scala.scalajs.wit.unsigned.UByte

/** A WASI 0.2 implementation of the javalib's `WasmSystem`.
 *
 *  It is an example of what a library provides to plug `System.out`,
 *  `System.err`, `System.currentTimeMillis()`, `System.nanoTime()` and
 *  `Math.random()` into a WebAssembly component.
 */
object WasmSystem {

  /** `blocking-write-and-flush` accepts at most 4096 bytes per call. */
  private final val MaxChunk = 4096

  def print(s: String, isErr: scala.Boolean): Unit = {
    val stream: OutputStream =
      if (isErr) stderr.getStderr()
      else stdout.getStdout()
    try {
      val bytes = s.getBytes().asInstanceOf[Array[UByte]]
      var off = 0
      while (off < bytes.length) {
        val len = Math.min(MaxChunk, bytes.length - off)
        val chunk =
          if (off == 0 && len == bytes.length) bytes
          else java.util.Arrays.copyOfRange(bytes, off, off + len)
        if (!stream.blockingWriteAndFlush(chunk).isOk)
          throw new RuntimeException("Failed to write to a standard stream")
        off += len
      }
    } finally {
      stream.close()
    }
  }

  def currentTimeMillis(): scala.Long = {
    val d = wall_clock.now()
    d.seconds * 1000L + Integer.toUnsignedLong(d.nanoseconds) / 1000000L
  }

  def nanoTime(): scala.Long =
    monotonic_clock.now()

  def random(): scala.Double = {
    // The 53 high bits of a random u64, scaled to [0.0, 1.0)
    (wasiRandom.getRandomU64() >>> 11).toDouble * (1.0 / (1L << 53))
  }
}
