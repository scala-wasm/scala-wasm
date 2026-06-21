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

package java.lang.dectoflt

import java.lang.Math
import java.math.BigInteger

sealed trait FloatingPointFormat {
  type Repr

  val ExpBits: Int
  val SigBits: Int
  val CeilLog5OfMaxSig: Int
  val MaxSig: Long
  val PositiveInfinity: Repr

  final val ExplicitSigBits: Int = SigBits - 1
  final val MaxSigBigInt: BigInteger = BigInteger.valueOf(MaxSig)

  def powerOfTen(i: Int): Repr
  def nextDown(v: Repr): Repr
  def nextUp(v: Repr): Repr
  def mul(a: Repr, b: Repr): Repr
  def div(a: Repr, b: Repr): Repr

  /** Decompose the given floating-point value into a normalized mantissa and a power of 2. */
  def frexp(v: Repr): (Long, Int)
  def fromLong(v: Long): Repr
  def reinterpretBits(v: BigInteger): Repr
  def toIEEE754(v: FloatingPoint): Repr
}

object Binary32 extends FloatingPointFormat {
  type Repr = Float

  final val ExpBits = 8
  final val SigBits = 24

  final val MaxSig = (1L << SigBits) - 1
  final val CeilLog5OfMaxSig = 11
  final val PositiveInfinity = Float.PositiveInfinity

  private final val PowerOfTens = Array(
      1.0f,
      10.0f,
      100.0f,
      1000.0f,
      10000.0f,
      100000.0f,
      1000000.0f,
      10000000.0f,
      100000000.0f,
      1000000000.0f,
      10000000000.0f
  )

  def powerOfTen(i: Int): Repr = PowerOfTens(i)
  def nextDown(v: Repr): Repr = Math.nextDown(v)
  def nextUp(v: Repr): Repr = Math.nextUp(v)
  def mul(a: Repr, b: Repr): Repr = a * b
  def div(a: Repr, b: Repr): Repr = a / b

  def frexp(v: Repr): (Long, Int) = {
    val bits = java.lang.Float.floatToRawIntBits(v)
    val m = (bits & 0x7fffff + (1 << ExplicitSigBits)).toLong
    val exp = ((bits >>> ExplicitSigBits) & 0xff) - ((1 << (ExpBits - 1)) - 1) - ExplicitSigBits
    (m, exp)
  }

  def reinterpretBits(v: BigInteger): Repr =
    java.lang.Float.intBitsToFloat(v.intValue())

  def fromLong(v: Long): Repr = v.toFloat

  def toIEEE754(v: FloatingPoint): Repr = {
    val (sig, exponent) = v.roundNormal(this)
    // Remove the leading implicit bit
    // It is safe cast sig.toInt, because Float has 23 sig bits
    val encodedSig: Int = sig.toInt - (1 << (ExplicitSigBits))
    // Adjust the exponent for exponent bias and mantissa shift
    val encodedExp: Int = exponent + ((1 << (ExpBits - 1)) - 1) + ExplicitSigBits
    // combine bits
    val bits = (encodedExp << ExplicitSigBits) | encodedSig
    java.lang.Float.intBitsToFloat(bits)
  }
}

object Binary64 extends FloatingPointFormat {
  type Repr = Double

  final val ExpBits = 11
  // final val ExplicitSigBits = 52
  final val SigBits = 53
  final val MaxSig: Long = (1L << SigBits) - 1;
  final val CeilLog5OfMaxSig = 23

  private final val PowerOfTens = Array(
      1.0,
      10.0,
      100.0,
      1000.0,
      10000.0,
      100000.0,
      1000000.0,
      10000000.0,
      100000000.0,
      1000000000.0,
      10000000000.0,
      100000000000.0,
      1000000000000.0,
      10000000000000.0,
      100000000000000.0,
      1000000000000000.0,
      10000000000000000.0,
      100000000000000000.0,
      1000000000000000000.0,
      10000000000000000000.0,
      100000000000000000000.0,
      1000000000000000000000.0,
      10000000000000000000000.0
  )

  final val PositiveInfinity = Double.PositiveInfinity

  def powerOfTen(i: Int): Repr = PowerOfTens(i)
  def nextDown(v: Repr): Repr = Math.nextDown(v)
  def nextUp(v: Repr): Repr = Math.nextUp(v)
  def mul(a: Repr, b: Repr): Repr = a * b
  def div(a: Repr, b: Repr): Repr = a / b

  def frexp(v: Repr): (Long, Int) = {
    val bits = java.lang.Double.doubleToLongBits(v)
    val m = bits & 0xfffffffffffffL + (1 << ExplicitSigBits)
    val exp =
      ((bits >>> ExplicitSigBits) & 0x7ff).toInt - ((1 << (ExpBits - 1)) - 1) - ExplicitSigBits
    (m, exp)
  }

  def reinterpretBits(v: BigInteger): Repr =
    java.lang.Double.longBitsToDouble(v.longValue())

  def fromLong(v: Long): Repr = v.toDouble

  def toIEEE754(v: FloatingPoint): Repr = {
    val (sig, exponent) = v.roundNormal(this)
    // Remove the leading implicit bit
    val encodedSig: Long = sig - (1L << (ExplicitSigBits))
    // Adjust the exponent for exponent bias and mantissa shift
    val encodedExp: Long = exponent + ((1 << (ExpBits - 1)) - 1) + ExplicitSigBits
    // combine bits
    val bits = encodedExp << ExplicitSigBits | encodedSig
    java.lang.Double.longBitsToDouble(bits)
  }
}
