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

package java.lang

import scala.scalajs.LinkingInfo.ESVersion

// Copy of FloatingPointBits without JS interop
// Most of the functions depending on powsOf2,
// such as decodeIEEE754 and encodeIEEE754Exponent, have been copied.
// The difference is that whether js.Array or Array is used for powsOf2.
// If we could virtualize powsOf2, much of the implementation could be commonized.
// However, the issue is slightly complicated by the fact that many of these
// powsOf2-using functions have generic implementations for both
// binary32 and binary64 formats, and
// by the inability to depend on Function1 within javalib.
// Couldn't come up with a good way to extract the common part of functions.

/** Manipulating the bits of floating point numbers. */
private[lang] object FloatingPointBitsWasm {

  import scala.scalajs.LinkingInfo

  private val floatPowsOf2: Array[scala.Double] =
    makePowsOf2(len = 1 << 8, java.lang.Float.MIN_NORMAL.toDouble)

  private val doublePowsOf2: Array[scala.Double] =
    makePowsOf2(len = 1 << 11, java.lang.Double.MIN_NORMAL)

  private def makePowsOf2(len: Int, minNormal: scala.Double): Array[scala.Double] = {
    val r = new Array[scala.Double](len)
    r(0) = 0.0
    var i = 1
    var next = minNormal
    while (i != len - 1) {
      r(i) = next
      i += 1
      next *= 2
    }
    r(len - 1) = scala.Double.PositiveInfinity
    r
  }

  @noinline
  private def doubleHashCodePolyfill(value: scala.Double): Int =
    Long.hashCode(doubleToLongBitsPolyfillInline(value))

  def intBitsToFloat(bits: Int): scala.Float = {
    intBitsToFloatPolyfill(bits).toFloat
  }

  def floatToIntBits(value: scala.Float): Int = {
    floatToIntBitsPolyfill(value)
  }

  def longBitsToDouble(bits: scala.Long): scala.Double = {
    longBitsToDoublePolyfill(bits)
  }

  def doubleToLongBits(value: scala.Double): scala.Long = {
    doubleToLongBitsPolyfill(value)
  }

  /* --- Polyfills for floating point bit manipulations ---
   *
   * Originally inspired by
   * https://github.com/inexorabletash/polyfill/blob/3447582628b6e3ea81959c4d5987aa332c22d1ca/typedarray.js#L150-L264
   *
   * Note that if typed arrays are not supported, it is almost certain that
   * fround is not supported natively, so Float operations are extremely slow.
   *
   * We therefore do all computations in Doubles here.
   */

  private def intBitsToFloatPolyfill(bits: Int): scala.Double = {
    val ebits = 8
    val fbits = 23
    val sign = (bits >> 31) | 1 // -1 or 1
    val e = (bits >> fbits) & ((1 << ebits) - 1)
    val f = bits & ((1 << fbits) - 1)
    decodeIEEE754(ebits, fbits, floatPowsOf2, scala.Float.MinPositiveValue, sign, e, f)
  }

  private def floatToIntBitsPolyfill(floatValue: scala.Float): Int = {
    // Some constants
    val ebits = 8
    val fbits = 23

    // Force computations to be on Doubles
    val value = floatValue.toDouble

    // Determine sign bit and compute the absolute value av
    val sign = if (value < 0.0 || (value == 0.0 && 1.0 / value < 0.0)) -1 else 1
    val s = sign & scala.Int.MinValue
    val av = sign * value

    // Compute e and f
    val powsOf2 = this.floatPowsOf2 // local cache
    val e = encodeIEEE754Exponent(ebits, powsOf2, av)
    val f = encodeIEEE754MantissaBits(ebits, fbits, powsOf2, scala.Float.MinPositiveValue.toDouble, av, e)

    // Encode
    s | (e << fbits) | rawToInt(f)
  }

  private def longBitsToDoublePolyfill(bits: scala.Long): scala.Double = {
    val ebits = 11
    val fbits = 52
    val hifbits = fbits - 32
    val hi = (bits >>> 32).toInt
    val lo = Utils.toUint(bits.toInt)
    val sign = (hi >> 31) | 1 // -1 or 1
    val e = (hi >> hifbits) & ((1 << ebits) - 1)
    val f = (hi & ((1 << hifbits) - 1)).toDouble * 0x100000000L.toDouble + lo
    decodeIEEE754(ebits, fbits, doublePowsOf2, scala.Double.MinPositiveValue, sign, e, f)
  }

  @noinline
  private def doubleToLongBitsPolyfill(value: scala.Double): scala.Long =
    doubleToLongBitsPolyfillInline(value)

  @inline
  private def doubleToLongBitsPolyfillInline(value: scala.Double): scala.Long = {
    // Some constants
    val ebits = 11
    val fbits = 52
    val hifbits = fbits - 32

    // Determine sign bit and compute the absolute value av
    val sign = if (value < 0.0 || (value == 0.0 && 1.0 / value < 0.0)) -1 else 1
    val s = sign & scala.Int.MinValue
    val av = sign * value

    // Compute e and f
    val powsOf2 = this.doublePowsOf2 // local cache
    val e = encodeIEEE754Exponent(ebits, powsOf2, av)
    val f = encodeIEEE754MantissaBits(ebits, fbits, powsOf2, scala.Double.MinPositiveValue, av, e)

    // Encode
    val hi = s | (e << hifbits) | rawToInt(f / 0x100000000L.toDouble)
    val lo = rawToInt(f)
    (hi.toLong << 32) | (lo.toLong & 0xffffffffL)
  }

  @inline
  private def decodeIEEE754(ebits: Int, fbits: Int,
      powsOf2: Array[scala.Double], minPositiveValue: scala.Double,
      sign: scala.Int, e: Int, f: scala.Double): scala.Double = {

    // Some constants
    val specialExponent = (1 << ebits) - 1
    val twoPowFbits = (1L << fbits).toDouble

    if (e == specialExponent) {
      // Special
      if (f == 0.0)
        sign * scala.Double.PositiveInfinity
      else
        scala.Double.NaN
    } else if (e > 0) {
      // Normalized
      sign * powsOf2(e) * (1 + f / twoPowFbits)
    } else {
      // Subnormal
      sign * f * minPositiveValue
    }
  }

  private def encodeIEEE754Exponent(ebits: Int,
      powsOf2: Array[scala.Double], av: scala.Double): Int = {

    /* Binary search of `av` inside `powsOf2`.
     * There are exactly `ebits` iterations of this loop (11 for Double, 8 for Float).
     */
    var eMin = 0
    var eMax = 1 << ebits
    while (eMin + 1 < eMax) {
      val e = (eMin + eMax) >> 1
      if (av < powsOf2(e)) // false when av is NaN
        eMax = e
      else
        eMin = e
    }
    eMin
  }

  @inline
  private def encodeIEEE754MantissaBits(ebits: Int, fbits: Int,
      powsOf2: Array[scala.Double], minPositiveValue: scala.Double,
      av: scala.Double, e: Int): scala.Double = {

    // Some constants
    val specialExponent = (1 << ebits) - 1
    val twoPowFbits = (1L << fbits).toDouble

    if (e == specialExponent) {
      if (av != av)
        (1L << (fbits - 1)).toDouble // NaN
      else
        0.0 // Infinity
    } else {
      if (e == 0)
        av / minPositiveValue // Subnormal
      else
        ((av / powsOf2(e)) - 1.0) * twoPowFbits // Normal
    }
  }

  private def encodeIEEE754MantissaBitsCore(ebits: Int, fbits: Int,
      powsOf2: Array[scala.Double], minPositiveValue: scala.Double,
      av: scala.Double, e: Int): scala.Double = {

    // Some constants
    val specialExponent = (1 << ebits) - 1
    val twoPowFbits = (1L << fbits).toDouble

    if (e == specialExponent) {
      if (av != av)
        (1L << (fbits - 1)).toDouble // NaN
      else
        0.0 // Infinity
    } else {
      if (e == 0)
        av / minPositiveValue // Subnormal
      else
        ((av / powsOf2(e)) - 1.0) * twoPowFbits // Normal
    }
  }

  // https://tc39.es/ecma262/#sec-toint32
  // https://github.com/scala-js/scala-js/pull/1676
  private def rawToInt(x: scala.Double): Int = {
    if (Double.isNaN(x) || Double.isInfinite(x)) 0
    else {
      val number = x.toLong
      (number & 0xFFFFFFFFL).toInt
    }
  }

}
