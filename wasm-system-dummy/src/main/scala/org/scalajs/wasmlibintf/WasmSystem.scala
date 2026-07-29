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

object WasmSystem {
  // System test asserts
  // assertTrue(result.toString(), result >= 1360059308000L) // timestamp of the first commit of Scala.js
  private final val BaseMillis: scala.Long = 1360059308000L

  private var millis: scala.Long = BaseMillis
  private var nanos: scala.Long = 0L

  def print(s: String, isErr: scala.Boolean): Unit = () // no-op

  def nanoTime(): scala.Long = {
    nanos += 1000000L
    nanos
  }

  def currentTimeMillis(): scala.Long = {
    millis += 1L
    millis
  }

  def random(): scala.Double = 0.0
}
