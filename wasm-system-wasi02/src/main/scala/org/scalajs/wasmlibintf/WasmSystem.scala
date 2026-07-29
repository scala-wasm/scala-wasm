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

import org.scalajs.wasmsystem.wasi.cli.{stderr, stdout}
import org.scalajs.wasmsystem.wasi.clocks.{monotonic_clock, wall_clock}
import org.scalajs.wasmsystem.wasi.io.streams.OutputStream
import org.scalajs.wasmsystem.wasi.random.insecure

import scala.scalajs.wit.Ok

/** WASI 0.2 (Preview 2) implementation of the `WasmSystem`. */
object WasmSystem {

  /** `blocking-write-and-flush` accepts at most 4096 bytes per call. */
  private final val MaxChunk = 4096

  private lazy val out: OutputStream = stdout.getStdout()
  private lazy val err: OutputStream = stderr.getStderr()

  def print(s: String, isErr: scala.Boolean): Unit = {
    val stream = if (isErr) err else out
    val bytes = s.getBytes()
    var off = 0
    while (off < bytes.length) {
      val len = Math.min(MaxChunk, bytes.length - off)
      val chunk =
        if (off == 0 && len == bytes.length) bytes
        else java.util.Arrays.copyOfRange(bytes, off, off + len)
      stream.blockingWriteAndFlush(chunk) match {
        case _: Ok[_] => ()
        case _        => throw new RuntimeException("WASI write to standard stream failed")
      }
      off += len
    }
  }

  def nanoTime(): scala.Long =
    monotonic_clock.now()

  def currentTimeMillis(): scala.Long = {
    val d = wall_clock.now()
    d.seconds * 1000L + d.nanoseconds / 1000000L
  }

  def random(): scala.Double = {
    // Map a 64-bit value to [0.0, 1.0), take the 53 high bits and scale by 2^-53.
    val i = insecure.getInsecureRandomU64()
    (i >>> 11).toDouble * (1.0 / (1L << 53))
  }
}
