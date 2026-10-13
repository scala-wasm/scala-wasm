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

package java.security

import wasmsystemtest.wasi.random.random

/** A `SecureRandom` on top of `wasi:random/random`.
 *
 *  The javalib does not provide `SecureRandom`, on JS either: a library
 *  does (e.g., scalajs-java-securerandom on JS). This one is an example
 *  for WebAssembly components. `java.util.UUID.randomUUID()` uses it.
 */
class SecureRandom extends java.util.Random(0L) {

  override protected def next(bits: Int): Int =
    (random.getRandomU64() >>> (64 - bits)).toInt

  override def nextBytes(bytes: Array[Byte]): Unit = {
    val randomBytes = random.getRandomBytes(bytes.length.toLong)
    System.arraycopy(randomBytes, 0, bytes, 0, bytes.length)
  }
}
