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

package testSuiteWASI.compiler

import testSuiteWASI.junit.Assert._

object CharTest {
  def toIntNegativeToPositive(): Unit = {
    assertEquals(-3.toByte.toChar.toInt, 65533)
    assertEquals(-100.toShort.toChar.toInt, 65436)
    assertEquals(-66000.toChar.toInt, 65072)
    assertEquals(-4567L.toChar.toInt, 60969)
    assertEquals(-5.3f.toChar.toInt, 65531)
    assertEquals(-7.9.toChar.toInt, 65529)
  }

  def toIntOverflow(): Unit = {
    assertEquals(347876543.toChar.toInt, 11455)
    assertEquals(34234567876543L.toChar.toInt, 57279)
  }

  def multiplyOverflow(): Unit = {
    def test(a: Char, b: Char, expected: Int): Unit =
      assertEquals(a * b, expected)

    // note: expected values are constant-folded by the compiler on the JVM
    test(Char.MaxValue, Char.MaxValue, Char.MaxValue * Char.MaxValue)
  }

  def doNotBoxSeveralTimesInBlock(): Unit = {
    @noinline def test(x: Any): Unit =
      assertEquals('A', x)

    test({
      test('A')
      'A'
    }: Char)
  }

  def doNotBoxSeveralTimesInIf(): Unit = {
    @noinline def test(x: Any): Unit =
      assertEquals('A', x)

    @noinline def cond: Boolean = true

    test((if (cond) 'A' else 'B'): Char)
  }
}
