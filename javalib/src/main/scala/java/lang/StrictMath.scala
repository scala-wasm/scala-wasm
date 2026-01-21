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

/*
 * ====================================================
 * Copyright (C) 1993 by Sun Microsystems, Inc. All rights reserved.
 *
 * Developed at SunSoft, a Sun Microsystems, Inc. business.
 * Permission to use, copy, modify, and distribute this
 * software is freely granted, provided that this notice
 * is preserved.
 * ====================================================
 */

package java.lang

import java.lang.{Double => JDouble}

object StrictMath {

  private final val SignBit = scala.Long.MinValue
  private final val SignBitInt = scala.Int.MinValue

  // Port from https://www.netlib.org/fdlibm/e_log.c
  def log(x: scala.Double): scala.Double = {
    val Ln2High = 6.93147180369123816490e-01
    val Ln2Low = 1.90821492927058770002e-10
    val Two54 = 1.80143985094819840000e+16 // 2^54
    val Lg1 = 6.666666666666735130e-01
    val Lg2 = 3.999999999940941908e-01
    val Lg3 = 2.857142874366239149e-01
    val Lg4 = 2.222219843214978396e-01
    val Lg5 = 1.818357216161805012e-01
    val Lg6 = 1.531383769920937332e-01
    val Lg7 = 1.479819860511658591e-01

    val bits0 = JDouble.doubleToRawLongBits(x)
    val high0 = (bits0 >>> 32).toInt
    val low = bits0.toInt

    val (scaledX, k0) = if (high0 < 0x100000) {
      if ((bits0 & ~SignBit) == 0L) {
        return scala.Double.NegativeInfinity // log(+-0)=-inf
      }
      if (high0 < 0) {
        return scala.Double.NaN // log(-#) = NaN
      }
      (x * Two54, -54)
    } else {
      if (high0 >= 0x7ff00000) { // x is inf or NaN
        return x + x
      }
      (x, 0)
    }

    val bits = JDouble.doubleToRawLongBits(scaledX)
    val high = (bits >>> 32).toInt
    val highMantissa = high & 0xfffff // top 20 bits of mantissa

    /* Find k and f such that `x = 2^k * (1+f)`,
     * where `sqrt(2)/2 < 1+f < sqrt(2)`.
     *
     * First, check if mantissa (1+f) is larger than √2 by computing
     * `val i = (highMantissa + 0x95f64) & 0x100000` where
     * 0x6a09c is the top 20 bits approximation of the mantissa of √2
     * and, 0x100000 - 0x6a09c = 0x95f64
     * If mantissa is larger than √2, i is 0x100000, otherwise it's 0.
     */
    val i = (highMantissa + 0x95f64) & 0x100000
    /* `(highMantissa | (i ^ 0x3ff00000)).toLong << 32)` sets the exponent to
     * 0 if the original mantissa (1+f) was smaller than √2, otherwise -1.
     * newX =
     *   2^0 * (1+f)    (when (1+f) < √2)
     *   2^(-1) * (1+f) (when (1+f) >= √2)
     *
     * Now, x = 2^k * newX
     */
    val newX =
      makeDouble((highMantissa | (i ^ 0x3ff00000)), bits)
    val k = k0 + ((high >> 20) - 1023) + (i >> 20)
    val f = newX - 1.0

    /* Fast path: newX is very close to 1.0 (`|newX - 1.0| < 2^(-20)`).
     * For example:
     * | x (f=x−1)    | high     | 2+high   | masked | < 3   |
     * |--------------+----------+----------+--------+-------|
     * | 1.0+2−20     | 3FF00001 | 3FF00003 |   3    | false |
     * | 1.0          | 3FF00000 | 3FF00002 |   2    | true  |
     * | 1.0-2^21     | 3FEFFFFF | 3FF00001 |   1    | true  |
     * | 1.0−2^20     | 3FEFFFFE | 3FF00000 |   0    | true  |
     * | 1.0−1.5×2^20 | 3FEFFFFD | 3FEFFFFF | FFFFF  | false |
     */
    if ((0xfffff & (2 + highMantissa)) < 3) {
      if (f == 0.0) {
        if (k == 0) {
          0.0
        } else {
          // ln(x) =  ln(2^k) + ln(newX) (where newX =~ 1.0)
          //       =~ k * ln2 + 0.0
          val dk = k.toDouble
          dk * Ln2High + dk * Ln2Low
        }
      } else {
        /* R = f*f*(0.5-0.33333333333333333*f);
         * if(k==0) return f-R; else {dk=(double)k;
         *   return dk*ln2_hi-((R-dk*ln2_lo)-f);}
         *
         * Third-order Maclaurin expansion of ln(1+f).
         * Since |f| < 2^(-20), the fourth term f^4/4 is approximately 2^(-82),
         * which is far below the precision of a double 2^(-53).
         */
        val r = f * f * (0.5 - 0.33333333333333333 * f)
        if (k == 0) {
          return f - r
        } else {
          val dk = k.toDouble
          /* Adding large and small values can lead to a loss of precision;
           * adjust the order of calculations accordingly.
           * log(x) = k*ln(2) + log(1+f) = k*ln2 + (f - R)
           *        = k*Ln2High + k*Ln2Low + f - R
           */
          dk * Ln2High - ((r - dk * Ln2Low) - f)
        }
      }
    } else {
      // √2/2       < 1+f < √2
      // -0.292.... <  f  < 0.4142...
      // -0.17153.. <  s  < 0.17153...
      val s = f / (2.0 + f)
      val z = s * s
      val w = z * z

      /* ln(x) = k*ln2 + ln(1+f).
       * Since `1+f = (1+s)/(1-s)`,
       * ln(1+f) = ln(1+s) - ln(1-s) = 2s + 2/3*s^3 + 2/5*s^5 + ...
       * because
       * ln(1+s) =  s - (s^2)/2 + (s^3)/3 - (s^4)/4 + ...
       * ln(1-s) = -s - (s^2)/2 - (s^3)/3 - (s^4)/4 + ...
       * and this equals 2*artanh(s).
       *
       * ln(x) = k*ln2 + 2s + s*R, where
       * R = 2/3*s^2 + 2/5*s^4 + 2/7*s^6 + ...
       *   = 2z*(1/3 + 1/5*z^2 + 1/7*z^3 + ...)
       * Note that s*R = 2*artanh(s) - 2s, and
       * R = 2*atanh(s)/s - 2
       *   = 2*atanh(√z)/√z - 2
       * Hence, 2*(1/3 + 1/5*z^2 + 1/7*z^3 + ...) =
       *        (2*atanh(√z)/√z - 2)/z
       *
       * We use Remez on [0, 0.1716^2] to generate
       * a polynomial of degree 6 to approximate (1/3 + 1/5*z^2 + 1/7*z^3 + ...).
       * The maximum error of this polynomial approximation is bounded by 2**-58.45.
       * (In the sollya script below, it was around 2^-56.629, but it is still small enough.)
       * (With d = 5, the maximum error was around 2^-49.38.)
       *
       * ```sollya
       * d = 6;
       * dom = [2^(-100), 0.1716^2];
       * f = (2 * atanh(sqrt(x)) / sqrt(x) - 2) / x;
       * p = remez(f, d, dom);
       * for n from 0 to d do {
       *    c = coeff(p, n);
       *    print("Lg" @ (n+1) @ ":", c);
       * };
       * e = x * p - (2 * atanh(sqrt(x)) / sqrt(x) - 2);
       * m = dirtyinfnorm(e, dom);
       * print(m); // 2^-56.629
       * ```
       * Lg1...Lg7 are pre-calculated coefficients (copied from fdlibm).
       */

      val t1 = w * (Lg2 + w * (Lg4 + w * Lg6))
      val t2 = z * (Lg1 + w * (Lg3 + w * (Lg5 + w * Lg7)))
      val approx = t2 + t1
      /* log(1+f) = s*R + 2s adds a very small value to a relatively large one,
       * which can lead to a loss of precision. To guarantee error in log below 1ulp,
       * we compute log by
       *   log(1+f) = sR * s2
       *     = (2*f + f*R)/(2+f)
       *     = (2*f + f^2 - f^2 + f*R)/(2+f)
       *     = f - s*(f - R)  (if f is not too large)
       *   log(1+f) = sR * 2s
       *     = sR + f - hfsq + s*hfsq
       *     = f - (hrsq - s*(hfsq+R))
       *     = f - (hfsq - s*(hfsq+R))  (better accuracy)
       * Note that 2s = f - s*f = f - hfsq + s*hfsq, where hfsq = f*f/2.
       */
      val dk = k.toDouble
      // f is somewhere between approximately 0.38 and 0.42 (f is large).
      // Use log(1+f) = f - (hfsq - s*(hfsq+R)) for better accuracy.
      if (((highMantissa - 0x6147a) | (0x6b851 - highMantissa)) > 0) {
        val hfsq = 0.5 * f * f
        if (k == 0) {
          f - (hfsq - s * (hfsq + approx))
        } else {
          dk * Ln2High - ((hfsq - (s * (hfsq + approx) + dk * Ln2Low)) - f)
        }
      } else {
        if (k == 0) {
          f - s * (f - approx)
        } else {
          dk * Ln2High - ((s * (f - approx) - dk * Ln2Low) - f)
        }
      }
    }
  }

  // Port from https://www.netlib.org/fdlibm/e_log10.c
  def log10(x: scala.Double): scala.Double = {
    val Two54 = 1.80143985094819840000e+16
    val Ivln10 = 4.34294481903251816668e-01
    val Log10Of2Hi = 3.01029995663611771306e-01
    val Log10Of2Lo = 3.69423907715893078616e-13

    val bits = JDouble.doubleToRawLongBits(x)
    val high0 = (bits >>> 32).toInt
    val low = bits.toInt

    val (scaledX, k0) = if (high0 < 0x100000) { // x < 2**-1022 (subnormal)
      if ((bits & ~SignBit) == 0L) {
        return scala.Double.NegativeInfinity // log(+-0)=-inf
      }
      if (high0 < 0) {
        return scala.Double.NaN // log(-#) = NaN
      }
      (x * Two54, -54)
    } else {
      if (high0 >= 0x7ff00000) {
        return x + x // Inf or NaN
      }
      (x, 0)
    }
    val high = (JDouble.doubleToRawLongBits(scaledX) >>> 32).toInt
    if (high >= 0x7ff00000) {
      return x + x;
    }

    /* Normalize x = 2^k * newX
     * log10(x) = log10(2^k * newX)
     *          = k * log10(2) + log10(newX)
     *          = k * log10(2) + ln(newX) / ln(10)
     * where newX ∈ [0.5, 2.0).
     */
    val exponent = k0 + (high >> 20) - 1023
    val i = ((exponent & 0x80000000) >>> 31) // if (exponent>=0) 0 else 1
    val k = (exponent + i).toDouble
    val newX = // exponent will be 0 if i=0, otherwise -1
      makeDouble(((0x3ff - i) << 20) | (high & 0xfffff), JDouble.doubleToRawLongBits(scaledX))
    k * Log10Of2Lo + k * Log10Of2Hi + Ivln10 * log(newX)
  }

  // Port from https://www.netlib.org/fdlibm/s_log1p.c
  def log1p(x: scala.Double): scala.Double = {
    val Ln2High = 6.93147180369123816490e-01
    val Ln2Low = 1.90821492927058770002e-10
    val Two54 = 1.80143985094819840000e+16
    val Lp1 = 6.666666666666735130e-01
    val Lp2 = 3.999999999940941908e-01
    val Lp3 = 2.857142874366239149e-01
    val Lp4 = 2.222219843214978396e-01
    val Lp5 = 1.818357216161805012e-01
    val Lp6 = 1.531383769920937332e-01
    val Lp7 = 1.479819860511658591e-01

    val bits = JDouble.doubleToRawLongBits(x)
    val high = (bits >>> 32).toInt
    val absHigh = high & 0x7fffffff

    val needReduction = if (high < 0x3fda827a) { // x < 0.41422
      if (absHigh >= 0x3ff00000) { // x <= -1.0
        if (x == -1.0) {
          return scala.Double.NegativeInfinity // log1p(-1) = log(0) = -inf
        } else {
          return scala.Double.NaN // log1p(x<-1) = NaN
        }
      }
      if (absHigh < 0x3e200000) { // |x| < 2^-29
        // For very small |x|, use Taylor expansion: log(1+x) = x - x^2/2 + ...
        if (absHigh < 0x3c900000) { // |x| < 2^-54
          return x
        } else {
          return x - x * x * 0.5
        }
      }
      if (high > 0 || high <= 0xbfd2bec3) {
        /* -0.2929 < x < 0.41422
         * sqrt(2)/2 < 1+x < sqrt(2), no argument reduction needed.
         */
        false
      } else {
        true
      }
    } else {
      if (high >= 0x7ff00000) {
        return x + x // x is inf or NaN
      }
      true // x >= 0.41422, need argument reduction
    }

    /* 1. Argument Reduction: find k and f such that
     * 1+x = 2^k * (1+f), where  sqrt(2)/2 < 1+f < sqrt(2).
     *
     * Note: If k=0 (needReduction=false), then f=x is exact.
     * However, if k!=0, then f may not be representable exactly.
     * In that case, a correction term is needed.
     * Let u = 1+x rounded, and c = (1+x)-u, then
     * log(1+x) - log(u) =~ c/u, because
     *   log(1+x) = log(u + c)
     *            = log(u * (1 + c/u))
     *            = log(u) + log(1 + c/u)
     *   By taylor expansion on log(1+c/u)
     *   log(1 + c/u) ≈ c/u - (c/u)²/2 + ... =~ c/u
     *   log(1+x) ≈ log(u) + c/u
     *
     * Thus, we proceed to compute log(u),
     * and add back the correction term c/u.
     * (Note: when x > 2^53, one can simply return log(x))
     *
     * Then, normalize to 1+x = 2^k * (1+f)
     * where f is close to 0, suitable for polynomial approximation.
     * We adjust the exponent field to bring the value into [sqrt(2)/2, sqrt(2)).
     */
    val (correction, k, f, newHighMantissa) = {
      if (needReduction) {
        val (exponent0, u, corr, highMantissa) = {
          if (high < 0x43400000) { // x < 2^53
            val u = 1.0 + x
            val uBits = JDouble.doubleToRawLongBits(u)
            val uHigh = (uBits >>> 32).toInt
            val exp = (uHigh >> 20) - 1023
            val corr = if (exp > 0) 1.0 - (u - x) else x - (u - 1.0)
            (exp, uBits, corr / u, uHigh & 0xfffff)
          } else {
            /* For x >= 2^53, 1+x = x in floating point (the +1 is below precision).
             * So log(1+x) = log(x), and we just use x directly with no correction needed.
             */
            val xBits = JDouble.doubleToRawLongBits(x)
            val xHigh = (xBits >>> 32).toInt
            ((xHigh >> 20) - 1023, xBits, 0.0, xHigh & 0xfffff)
          }
        }

        if (highMantissa < 0x6a09e) { // mantissa < √2
          // Set exponent to 0, so normalized value is in [1.0, sqrt(2)).
          (corr, exponent0, makeDouble(highMantissa | 0x3ff00000, u) - 1.0, highMantissa)
        } else { // mantissa >= √2
          (corr, exponent0 + 1, makeDouble(highMantissa | 0x3fe00000, u) - 1.0,
              (0x100000 - highMantissa) >> 2)
        }
      } else {
        (0.0, 0, x, 1)
      }
    }

    val hfsq = 0.5 * f * f
    // Fast path: when |f| < 2^-20, use simpler approximation.
    if (newHighMantissa == 0) { // |f| < 2^-20
      if (f == 0.0) {
        if (k == 0) {
          return 0.0
        } else {
          // log(1+x) = log(2^k * (1+f)) = k*ln(2) when f = 0
          val c = correction + k * Ln2Low
          return k * Ln2High + c
        }
      }
      /* Third-order Taylor expansion: log(1+f) ≈ f - f^2/2 + f^3/3
       * Rewritten as: f - (f^2/2)*(1 - 2f/3) for better accuracy.
       */
      val approx = hfsq * (1.0 - 0.66666666666666666 * f)
      if (k == 0) {
        return f - approx
      } else {
        return k * Ln2High - ((approx - (k * Ln2Low + correction)) - f)
      }
    }

    /* 2. Approximation of log1p(f).
     * Full polynomial approximation for log(1+f).
     * Let s = f/(2+f), then 1+f = (1+s)/(1-s), so:
     * log(1+f) = log(1+s) - log(1-s) = 2s + 2/3*s^3 + 2/5*s^5 + ... = 2s + s*R
     * where R is approximated by a polynomial in z = s^2.
     */
    val s = f / (2.0 + f)
    val z = s * s
    val approx = z * (Lp1 + z * (Lp2 + z * (Lp3 + z * (Lp4 + z * (Lp5 + z * (Lp6 + z * Lp7))))))

    /* 3. Final result: log(1+x) = k*ln(2) + log(1+f)
     * where log(1+f) = f - (hfsq - s*(hfsq+R))
     */
    if (k == 0) {
      f - (hfsq - s * (hfsq + approx))
    } else {
      k * Ln2High - ((hfsq - (s * (hfsq + approx) + (k * Ln2Low + correction))) - f)
    }
  }

  // Port from https://www.netlib.org/fdlibm/e_sqrt.c
  def sqrt(x: scala.Double): scala.Double = {
    val bits = JDouble.doubleToRawLongBits(x)
    val high0 = (bits >>> 32).toInt
    val low0 = bits.toInt

    if ((high0 & 0x7ff00000) == 0x7ff00000)
      return x * x + x // sqrt(NaN)=NaN, sqrt(+inf)=+inf, sqrt(-inf)=sNaN

    if (high0 <= 0) {
      if (((high0 & (~SignBitInt)) | low0) == 0)
        return x // sqrt(+-0) = +-0
      else if (high0 < 0)
        return scala.Double.NaN // sqrt(-ve) = NaN
    }

    /* 1. Normalization
     * Scale x to y in [1,4) with even powers of 2:
     * find an integer k such that  1 <= (y=x*2^(2k)) < 4, then
     *   sqrt(x) = 2^k * sqrt(y)
     */
    val (high, low, exp0) = {
      val rawE = high0 >> 20
      // subnormal numbers. shift until we find the leading 1 bit.
      if (rawE == 0) {
        var highTmp = high0
        var lowTmp = low0
        var expTmp = 0
        // optimization: shift in 21-bit chunks from low word to high word
        while (highTmp == 0) {
          expTmp -= 21
          highTmp |= (lowTmp >>> 11)
          lowTmp <<= 21
        }
        var i = 0
        while ((highTmp & 0x100000) == 0) {
          highTmp <<= 1
          i += 1
        }
        expTmp -= i - 1
        highTmp |= (lowTmp >>> (32 - i))
        lowTmp <<= i
        (highTmp, lowTmp, expTmp - 1023)
      } else {
        (high0, low0, rawE - 1023)
      }
    }

    /* If exponent is odd, double the mantissa to make exponent even
     * because sqrt(2^e * m) = 2^(e/2) * sqrt(m).
     * We need e to be even for integer division, so if e is odd:
     * 2^e * m = 2^(e-1) * (2*m), now (e-1) is even
     */
    val mHigh0 = (high & 0xfffff) | 0x100000 // implicit leading 1
    val mLow0 = low
    val (mHigh, mLow) = if ((exp0 & 1) == 1) {
      (mHigh0 + mHigh0 + ((mLow0 & SignBitInt) >>> 31), mLow0 + mLow0)
    } else {
      (mHigh0, mLow0)
    }
    val newExp = exp0 >> 1

    /* Step 2: Bit-by-bit computation of sqrt(mantissa) using integer arithmetic
     *
     * After Step 1, we have: `x = 2^e * m`, and `sqrt(x) = 2^(e/2) * sqrt(m)`, where m ∈ [1, 4).
     * here we compute `sqrt(m)` using digit-by-digit calculation:
     * https://en.wikipedia.org/wiki/Square_root_algorithms#Digit-by-digit_calculation
     */
    // Initialize remainder = mantissa * 2 (scaled for bit extraction)
    var remHigh = mHigh + mHigh + ((mLow & SignBitInt) >>> 31)
    var remLow = mLow + mLow

    var q = 0
    var s0 = 0 // 2 * q (for testing next bit)
    var r = 0x200000 // Current bit position (starts at bit 21)
    while (r != 0) {
      // Test if we can set this bit: (result + r)^2 still be <= original value?
      val t = s0 + r
      if (t <= remHigh) {
        s0 = t + r
        remHigh = remHigh - t
        q = q + r
      }
      // Shift remainder left by 1 for next iteration (extract next bit of precision)
      remHigh = remHigh + remHigh + ((remLow & SignBitInt) >>> 31)
      remLow = remLow + remLow
      r = r >>> 1
    }

    var q1 = 0
    var s1 = 0
    r = SignBitInt
    while (r != 0) {
      val t1 = s1 + r
      val t = s0
      if ((t < remHigh) ||
          ((t == remHigh) && Integer.unsigned_<=(t1, remLow))) {
        s1 = t1 + r
        if (((t1 & SignBitInt) == SignBitInt) && ((s1 & SignBitInt) == 0))
          s0 = s0 + 1
        remHigh = remHigh - t
        if (Integer.unsigned_<(remLow, t1))
          remHigh = remHigh - 1
        remLow = remLow - t1
        q1 = q1 + r
      }
      remHigh = remHigh + remHigh + ((remLow & SignBitInt) >>> 31)
      remLow = remLow + remLow
      r = r >>> 1
    }

    // Step 3: Rounding based on remainder
    // Original e_sqrt implementation had a rouding mode check,
    // but here we omit those checks because we always round to even.
    val (resHigh, resLow) = {
      if ((remHigh | remLow) != 0) {
        if (q1 == 0xffffffff) {
          (q + 1, 0)
        } else {
          (q, q1 + (q1 & 1))
        }
      } else {
        (q, q1)
      }
    }

    // Reconstruct the double value
    val newHigh = (resHigh >>> 1) + 0x3fe00000 + (newExp << 20)
    val newLow =
      if ((resHigh & 1) == 1) (resLow >>> 1) | SignBitInt
      else resLow >>> 1
    makeDouble(newHigh, newLow.toLong)
  }

  // Port from https://www.netlib.org/fdlibm/e_pow.c
  def pow(x: scala.Double, y: scala.Double): scala.Double = {
    val Two53 = 9007199254740992.0 // 0x43400000, 0x00000000 = 2^53
    val Bp0 = 1.0
    val Bp1 = 1.5

    // dp_h and dp_l used in log2 computation
    val DpH0 = 0.0
    val DpH1 = 5.84962487220764160156e-01 // 0x3FE2B803, 0x40000000
    val DpL0 = 0.0
    val DpL1 = 1.35003920212974897128e-08 // 0x3E4CFDEB, 0x43CFD006

    // Polynomial coefficients for log(x) computation: (3/2)*(log(x)-2s-2/3*s**3)
    val Lg1 = 5.99999999999994648725e-01 // 0x3FE33333, 0x33333303
    val Lg2 = 4.28571428578550184252e-01 // 0x3FDB6DB6, 0xDB6FABFF
    val Lg3 = 3.33333329818377432918e-01 // 0x3FD55555, 0x518F264D
    val Lg4 = 2.72728123808534006489e-01 // 0x3FD17460, 0xA91D4101
    val Lg5 = 2.30660745775561754067e-01 // 0x3FCD864A, 0x93C9DB65
    val Lg6 = 2.06975017800338417784e-01 // 0x3FCA7E28, 0x4A454EEF

    // Polynomial coefficients for exp2 approximation
    val Exp2P1 = 1.66666666666666019037e-01 // 0x3FC55555, 0x5555553E
    val Exp2P2 = -2.77777777770155933842e-03 // 0xBF66C16C, 0x16BEBD93
    val Exp2P3 = 6.61375632143793436117e-05 // 0x3F11566A, 0xAF25DE2C
    val Exp2P4 = -1.65339022054652515390e-06 // 0xBEBBBD41, 0xC5D26BF1
    val Exp2P5 = 4.13813679705723846039e-08 // 0x3E663769, 0x72BEA4D0

    // ln(2) and 1/ln(2) split into high and low parts for precision
    val Ln2 = 6.93147180559945286227e-01 // 0x3FE62E42, 0xFEFA39EF
    val Ln2High = 6.93147182464599609375e-01 // 0x3FE62E43, 0x00000000
    val Ln2Low = -1.90465429995776804525e-09 // 0xBE205C61, 0x0CA86C39

    val InvLn2 = 1.44269504088896338700e+00 // 0x3FF71547, 0x652B82FE = 1/ln(2)
    val InvLn2High = 1.44269502162933349609e+00 // 0x3FF71547, 0x60000000 = 24 bits of 1/ln(2)
    val InvLn2Low = 1.92596299112661746887e-08 // 0x3E54AE0B, 0xF85DDF44 = tail of 1/ln(2)

    // 2/(3*ln(2)) split into high and low parts for computing log2
    val Cp = 9.61796693925975554329e-01 // 0x3FEEC709, 0xDC3A03FD
    val CpHigh = 9.61796700954437255859e-01 // 0x3FEEC709, 0xE0000000
    val CpLow = -7.02846165095275826516e-09 // 0xBE3E2FE0, 0x145B01F5

    // Overflow threshold: -(1024 - log2(ovfl+.5ulp))
    val OverflowThreshold = 8.0085662595372944372e-017

    val xBits = JDouble.doubleToRawLongBits(x)
    val xHigh = (xBits >>> 32).toInt
    val xLow = xBits.toInt

    val yBits = JDouble.doubleToRawLongBits(y)
    val yHigh = (yBits >>> 32).toInt
    val yLow = yBits.toInt

    val xAbsHigh = xHigh & 0x7fffffff
    val yAbsHigh = yHigh & 0x7fffffff

    // Step 0: Special cases
    // y==zero: x**0 = 1
    if (y == 0) {
      return 1.0
    }

    // +-NaN return x+y
    if (JDouble.isNaN(x) || JDouble.isNaN(y)) {
      return scala.Double.NaN
    }

    /* Determine if y is an odd integer when x < 0.
     * This is needed to handle the sign of the result correctly:
     * (-x)**y = (-1)^y * x^y so we need to know if n is odd or even.
     *
     * yIsInt = 0 ... y is not an integer
     * yIsInt = 1 ... y is an odd integer
     * yIsInt = 2 ... y is an even integer
     */
    var yIsInt = 0
    if (xHigh < 0) { // Only need to check if x is negative
      if (yAbsHigh >= 0x43400000) { // |y| >= 0x1.0p53 = 2^53
        // All doubles >= 2^53 are even integers
        yIsInt = 2
      } else if (yAbsHigh >= 0x3ff00000) { // |y| >= 1.0
        val yExponent = (yAbsHigh >> 20) - 0x3ff
        // Exponent > 20: integer bits are in low word
        if (yExponent > 20) {
          val mantissaBits = yLow >>> (52 - yExponent)
          // y is integer = no fractional bits
          if ((mantissaBits << (52 - yExponent)) == yLow) {
            yIsInt = 2 - (mantissaBits & 1) // odd if LSB is 1, even otherwise
          }
        } else if (yLow == 0) {
          val mantissaBits = yAbsHigh >>> (20 - yExponent)
          if ((mantissaBits << (20 - yExponent)) == yAbsHigh) {
            yIsInt = 2 - (mantissaBits & 1)
          }
        }
      }
    }

    // Special value of y
    if (yLow == 0) {
      if (yAbsHigh == 0x7ff00000) { // y is +-inf
        if (((xAbsHigh - 0x3ff00000) | xLow) == 0) { // x is +-1
          return scala.Double.NaN // inf**+-1 is NaN
        } else if (xAbsHigh >= 0x3ff00000) { // (|x|>1)**+-inf = inf,0
          return if (yHigh >= 0) y else 0.0
        } else { // (|x|<1)**-,+inf = inf,0
          return if (yHigh < 0) -y else 0.0
        }
      }
      if (yAbsHigh == 0x3ff00000) { // y is +-1
        if (yHigh < 0) return 1.0 / x else return x
      }
      if (yHigh == 0x40000000) {
        return x * x // y is 2
      }
      if (yHigh == 0x3fe00000) { // y is 0.5
        if (xHigh >= 0) { // x >= +0
          return StrictMath.sqrt(x)
        }
      }
    }

    val xAbs = Math.abs(x)

    // Special value of x
    if (xLow == 0) {
      if (xAbsHigh == 0x7ff00000 || xAbsHigh == 0 || xAbsHigh == 0x3ff00000) {
        var result = xAbs // x is +-0,+-inf,+-1
        if (yHigh < 0)
          result = 1.0 / result // result = (1/|x|)
        if (xHigh < 0) {
          if (((xAbsHigh - 0x3ff00000) | yIsInt) == 0) {
            result = (result - result) / (result - result) // (-1)**non-int is NaN
          } else if (yIsInt == 1) {
            result = -result // (x<0)**odd = -(|x|**odd)
          }
        }
        return result
      }
    }

    val xGteZero = (xHigh >> 31) + 1 // x >=0 ? 1 : 0

    // (x<0)**(non-integer) is NaN
    if ((xGteZero | yIsInt) == 0) {
      return (x - x) / (x - x)
    }

    /* If x < 0 and y is odd (yIsInt = 1): negative
     * Otherwise: result is positive
     */
    var resultSign = 1.0
    if ((xGteZero | (yIsInt - 1)) == 0)
      resultSign = -1.0 // (-ve)**(odd int)

    // |y| is huge
    if (yAbsHigh > 0x41e00000) { // if |y| > 2**31
      if (yAbsHigh > 0x43f00000) { // if |y| > 2**64, must o/uflow
        if (xAbsHigh <= 0x3fefffff) {
          val res =
            if (yHigh < 0) scala.Double.PositiveInfinity
            else 0.0
          return res
        }
        if (xAbsHigh >= 0x3ff00000) {
          val res =
            if (yHigh > 0) scala.Double.PositiveInfinity
            else 0.0
          return res
        }
      }
      // over/underflow if x is not close to one
      if (xAbsHigh < 0x3fefffff) {
        val res =
          if (yHigh < 0) resultSign * scala.Double.PositiveInfinity
          else resultSign * 0.0
        return res
      }
      if (xAbsHigh > 0x3ff00000) {
        val res =
          if (yHigh > 0) resultSign * scala.Double.PositiveInfinity
          else resultSign * 0.0
        return res
      }
    }

    // Step 1: Computing `log_2(|x|)` for `x^y = 2^(y * log2(x))`.
    val (log2XHigh, log2XLow) = {
      /* Fast path: when |y| > 2^31 and x is very close to 1 (within 2^-20).
       * In this case, |x-1| is tiny, so we can use a Taylor series:
       * ln(x) =~ (x-1) - (x-1)^2/2 + (x-1)^3/3 - (x-1)^4/4
       * Then log2(x) = ln(x)/ln(2).
       */
      if (yAbsHigh > 0x41e00000) { // |y| > 2^31
        val t = xAbs - 1.0 // |x|-1
        val correction = (t * t) * (0.5 - t * (0.3333333333333333333333 - t * 0.25))
        val highPart = InvLn2High * t
        val lowPart = t * InvLn2Low - correction * InvLn2
        var log2XHigh = highPart + lowPart
        log2XHigh = makeDouble((JDouble.doubleToRawLongBits(log2XHigh) >>> 32).toInt, 0L)
        val log2XLow = lowPart - (log2XHigh - highPart)
        (log2XHigh, log2XLow)
      } else {
        // Full log2 computation for general case.
        // Let x = 2^n * (1 + f)
        var xScaled = xAbs
        var n = 0 // exponent
        var xScaledHigh = (JDouble.doubleToRawLongBits(xScaled) >>> 32).toInt

        /* Handle subnormal numbers.
         * Scale by 2^53 to normalize, then adjust exponent.
         */
        if (xAbsHigh < 0x100000) { // exponent = 0
          xScaled *= Two53
          n -= 53
          val scaledBits = JDouble.doubleToRawLongBits(xScaled)
          xScaledHigh = (scaledBits >>> 32).toInt
        }
        n += ((xScaledHigh) >> 20) - 0x3ff
        // mantissa (significant 20 bits precision, enough for determine which interval to use)
        val f = xScaledHigh & 0xfffff
        // Later we swap the top 32bits of xScaled to xNormHigh.
        // Then xScaled = 2^0 * (1 + f), and 1.0 <= xScaled < 2.0
        var xNormHigh = f | 0x3ff00000

        /* Determine which breakpoint (bp) to use for log approximation.
         * ln(x) = n = ln(x/bp * bp) = ln(x/bp) + ln(bp)
         * Let x/bp = (1+s)/(1-s),
         * then   s = (x/bp - 1)/(x/bp + 1) = (x - bp)/(x + bp)
         *
         * Now, we can compute the polynomial approximation of ln(x/bp) accurately,
         * when bp is close to x, polynomial approx of ln(x) is accurate when x is near 1.
         * Note that s becomes approximately 0 as x approaches the bp.
         * ln(x/bp) = ln((1+s)/(1-s)) = 2s + 2s^3/3 + 2s^5/5 + ...
         *
         * Here, 1.0 <= xScaled < 2.0, we use the following breakpoint.
         * x near 1.0: Use breakpoint = 1.0 (index 0).
         * x near 1.5: Use breakpoint = 1.5. (index 1)
         * x near 2.0: Set x = x/2, add 1 to the exponent, and use breakpoint = 1.0.
         */
        val intervalIndex = {
          if (f <= 0x3988e) {
            0 // x < sqrt(3/2) =~ 1.225 (near 1.0)
          } else if (f < 0xbb67a) {
            1 // x < sqrt(3) ≈ 1.732 (near 1.5)
          } else { // x near 2.0
            n += 1
            xNormHigh -= 0x100000
            0
          }
        }
        xScaled = makeDouble(xNormHigh, JDouble.doubleToRawLongBits(xScaled))
        val breakpoint = if (intervalIndex == 0) Bp0 else Bp1

        // Compute s = (xScaled-bp)/(xScaled+bp)
        val numerator = xScaled - breakpoint
        val denominator = 1.0 / (xScaled + breakpoint)
        val s = numerator * denominator
        var sHigh = s
        sHigh = makeDouble((JDouble.doubleToRawLongBits(sHigh) >>> 32).toInt, 0L)

        // Compute t = x+bp
        // tHigh =~ (x + bp) (high 32 bit)
        val tHigh = makeDouble(((xNormHigh >> 1) | 0x20000000) + 0x80000 + (intervalIndex << 18), 0L)
        // tHigh =~ xScaled + bp + error(tLow)
        // tLow  =~ xScaled - tHigh + bp
        val tLow = xScaled - (tHigh - breakpoint)
        val sLow = denominator * ((numerator - sHigh * tHigh) - sHigh * tLow)

        /* Compute ln(xScaled) using polynomial approximation.
         * ln(xScaled/bp) = ln((1+s)/(1-s)) =
         *   = 2s + 2s^3/3 + 2s^5/5 + s^7/7 + ...
         *   = 2s * (1 + s^2/3 + s^4/5 + s^6/7 + ...)
         *   = 2s * (1 + s^2/3 + R(s^2))
         * where R(s^2) = s^4/5 + s^6/7 + ...
         * ln(xScaled/bp) =
         *   1 + s^2/3 + // Lower-order terms (precision is critical)
         *   polyApproxOfR(s^2)
         */
        val s2 = s * s
        // polynomial approximation of R(s^4)
        val polyApprox = s2 * s2 * (Lg1 + s2 * (Lg2 + s2 * (Lg3 + s2 * (Lg4 + s2 * (Lg5 + s2 * Lg6)))))
        val approxWithCorr = polyApprox + sLow * (sHigh + s)
        val sHigh2 = sHigh * sHigh
        // tmp = 3.0 * (1 + s^2/3 + R(s^2))
        // ln(xScaled/bp) = 2s * (1 + s^2/3 + R(s^2)) = 2s * (tmp/3)
        var tmpHigh = 3.0 + sHigh2 + approxWithCorr
        tmpHigh = makeDouble((JDouble.doubleToRawLongBits(tmpHigh) >>> 32).toInt, 0L)
        var tmpLow = approxWithCorr - ((tmpHigh - 3.0) - sHigh2)

        /* p = s * tmp
         *  = u + v
         *  = sHigh * tmpHigh + sLow * tmpHigh + tmpLow * s
         *  = (sHigh + sLow) * (tmpHigh + tmpLow)
         *  =~ s * 3(1 + s^2/3 + R(s^2))
         *  =  3s * (1 + s^2/3 + R(s^2))
         * Let Cp = 2/(3*ln(2))
         *
         * z = Cp * p + DpL (where Dp = log2(bp), DpL is low 32bit of log2(bp))
         *   =~ ln(xScaled/bp) / ln(2) + log2(bp)
         *   =  log2(xScaled/bp) + log2(bp)
         *   =  log2(xScaled)
         */
        val u = sHigh * tmpHigh
        val v = sLow * tmpHigh + tmpLow * s
        val pHigh0 = u + v // 3s * (1 + s^2/3 + R(s^2))
        var pHigh = pHigh0
        pHigh = makeDouble((JDouble.doubleToRawLongBits(pHigh) >>> 32).toInt, 0L)
        val pLow = v - (pHigh - u)
        val zHigh = CpHigh * pHigh
        val zLow = CpLow * pHigh + pLow * Cp +
          (if (intervalIndex == 0) DpL0 else DpL1)

        /* Final log2(x) computation. Remember x = 2^n * xScaled
         * log2(x) = n + log2(xScaled)
         * where DpH corrects for the breakpoint choice.
         */
        val dpH = if (intervalIndex == 0) DpH0 else DpH1
        val exponentDouble = n.toDouble
        var log2XHigh = (((zHigh + zLow) + dpH) + exponentDouble)
        log2XHigh = makeDouble((JDouble.doubleToRawLongBits(log2XHigh) >>> 32).toInt, 0L)
        val log2XLow = zLow - (((log2XHigh - exponentDouble) - dpH) - zHigh)
        (log2XHigh, log2XLow)
      }
    }

    // Step 2: Compute prd = y * log2(x).
    val yHighOnly = makeDouble((JDouble.doubleToRawLongBits(y) >>> 32).toInt, 0L)
    val prdLow = (y - yHighOnly) * log2XHigh + y * log2XLow
    val prdHigh = yHighOnly * log2XHigh
    val prd = prdLow + yHighOnly * log2XHigh
    val prdBits = JDouble.doubleToRawLongBits(prd)
    val prdHighWord = (prdBits >>> 32).toInt
    val prdLowWord = prdBits.toInt

    // Check for overflow: if y*log2(x) >= 1024, then 2^(y*log2(x)) overflows.
    if (prdHighWord >= 0x40900000) { // prd >= 1024
      if (((prdHighWord - 0x40900000) | prdLowWord) != 0) { // if prd > 1024
        return resultSign * scala.Double.PositiveInfinity
      } else if (prdLow + OverflowThreshold > prd - prdHigh) {
        return resultSign * scala.Double.PositiveInfinity
      }
    } else if ((prdHighWord & 0x7fffffff) >= 0x4090cc00) { // product <= -1075
      /* Check for underflow: if y*log2(x) <= -1075, then 2^(y*log2(x)) underflows.
       * IEEE 754 double min exponent is -1022, subnormal goes to -1074.
       * Beyond -1075 we get underflow to zero.
       */
      if (((prdHighWord - 0xc090cc00) | prdLowWord) != 0) { // product < -1075
        return resultSign * 0.0
      } else if (prdLow <= prd - prdHigh) {
        return resultSign * 0.0 // underflow
      }
    }

    /* Step 3: Compute 2^(y*log2(x)) = exp2(prd)
     *
     * Split product into integer and fractional parts
     * 2^prd = 2^integerPart * 2^fractionalPart
     * The integer part is handled by adjusting the exponent field.
     * The fractional part is computed using polynomial approximation.
     */
    val prdAbsHighWord = prdHighWord & 0x7fffffff
    val prdExponent = (prdAbsHighWord >> 20) - 0x3ff
    val (integerPart, prdHighAdjusted) = {
      if (prdAbsHighWord > 0x3fe00000) { // if |product| > 0.5, set integerPart = floor(product+0.5)
        var integerPart = prdHighWord + (0x100000 >>> (prdExponent + 1))
        val newExponent = ((integerPart & 0x7fffffff) >> 20) - 0x3ff
        val integerPartAsDouble = makeDouble(integerPart & ~(0xfffff >> newExponent), 0L)
        integerPart = ((integerPart & 0xfffff) | 0x100000) >> (20 - newExponent)
        if (prdHighWord < 0) integerPart = -integerPart
        (integerPart, prdHigh - integerPartAsDouble)
      } else {
        (0, prdHigh)
      }
    }

    // Compute 2^fractionalPart = e^(fractionalPart * ln(2))
    val fractionalPart =
      makeDouble((JDouble.doubleToRawLongBits(prdLow + prdHighAdjusted) >>> 32).toInt, 0L)
    val uExp2 = fractionalPart * Ln2High
    val vExp2 = (prdLow - (fractionalPart - prdHighAdjusted)) * Ln2 + fractionalPart * Ln2Low
    val exp2 = uExp2 + vExp2 // fractionalPart * ln(2)
    val wExp2 = vExp2 - (exp2 - uExp2) // rounding error for later correction

    /* Compute e^(exp2Approx) using polynomial approximation:
     * e^r = 1 + r + r^2/2! + r^3/3! + ... ≈ 1 + r - r^2 * P(r^2)
     * where P is a polynomial fit to (exp(r) - 1 - r) / r^2
     */
    val exp2ApproxSq = exp2 * exp2
    val exp2Poly = exp2 - exp2ApproxSq * (Exp2P1 + exp2ApproxSq * (Exp2P2 + exp2ApproxSq *
      (Exp2P3 + exp2ApproxSq * (Exp2P4 + exp2ApproxSq * Exp2P5))))
    val exp2Correction = (exp2 * exp2Poly) / (exp2Poly - 2.0) - (wExp2 + exp2 * wExp2)
    val exp2Approx = 1.0 - (exp2Correction - exp2)

    /* Combine: result = 2^integerPart * exp2Approx
     * Scale exp2Approx by 2^integerPart by adding integerPart to its exponent field.
     */
    val exp2ApproxBits = JDouble.doubleToRawLongBits(exp2Approx)
    val exp2ApproxHigh = (exp2ApproxBits >>> 32).toInt + (integerPart << 20)
    val result = if ((exp2ApproxHigh >> 20) <= 0) {
      // Result is subnormal - use scalb to handle gradual underflow
      Math.scalb(exp2Approx, integerPart)
    } else {
      makeDouble(exp2ApproxHigh, exp2ApproxBits)
    }
    resultSign * result
  }

  @inline private def makeDouble(high: Int, low: scala.Long): scala.Double =
    JDouble.longBitsToDouble((high.toLong << 32) | (low & 0xffffffffL))
}
