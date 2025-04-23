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

package testSuiteWASI.javalib.lang

import java.lang.{Byte => JByte}

import testSuiteWASI.utils.AssertThrows.assertThrows
import org.junit.Assert._

// import org.scalajs.testsuite.utils.AssertThrows.assertThrows

/** Tests the implementation of the java standard library Byte
 */
class ByteTest {

  def compareToJavaByte(): Unit = {
    def compare(x: Byte, y: Byte): Int =
      new JByte(x).compareTo(new JByte(y))

    assertTrue(compare(0.toByte, 5.toByte) < 0)
    assertTrue(compare(10.toByte, 9.toByte) > 0)
    assertTrue(compare(-2.toByte, -1.toByte) < 0)
    assertEquals(0, compare(3.toByte, 3.toByte))
  }

  def compareTo(): Unit = {
    def compare(x: Any, y: Any): Int =
      x.asInstanceOf[Comparable[Any]].compareTo(y)

    assertTrue(compare(0.toByte, 5.toByte) < 0)
    assertTrue(compare(10.toByte, 9.toByte) > 0)
    assertTrue(compare(-2.toByte, -1.toByte) < 0)
    assertEquals(0, compare(3.toByte, 3.toByte))
  }

  def toUnsignedInt(): Unit = {
    assertEquals(0, JByte.toUnsignedInt(0.toByte))
    assertEquals(42, JByte.toUnsignedInt(42.toByte))
    assertEquals(214, JByte.toUnsignedInt(-42.toByte))
    assertEquals(128, JByte.toUnsignedInt(Byte.MinValue))
    assertEquals(127, JByte.toUnsignedInt(Byte.MaxValue))
  }

  def toUnsignedLong(): Unit = {
    assertEquals(0L, JByte.toUnsignedLong(0.toByte))
    assertEquals(42L, JByte.toUnsignedLong(42.toByte))
    assertEquals(214L, JByte.toUnsignedLong(-42.toByte))
    assertEquals(128L, JByte.toUnsignedLong(Byte.MinValue))
    assertEquals(127L, JByte.toUnsignedLong(Byte.MaxValue))
  }

  def parseString(): Unit = {
    def test(s: String, v: Byte): Unit = {
      assertEquals(v, JByte.parseByte(s))
      assertEquals(v, JByte.valueOf(s).byteValue())
      assertEquals(v, new JByte(s).byteValue())
      assertEquals(v, JByte.decode(s))
    }

    test("0", 0)
    test("5", 5)
    test("127", 127)
    test("-100", -100)
  }

  def parseStringInvalidThrows(): Unit = {
    def test(s: String): Unit = {
      assertThrows(classOf[NumberFormatException], JByte.parseByte(s))
      assertThrows(classOf[NumberFormatException], JByte.decode(s))
    }

    test("abc")
    test("")
    test("200") // out of range
  }

  def parseStringBase16(): Unit = {
    def test(s: String, v: Byte): Unit = {
      assertEquals(v, JByte.parseByte(s, 16))
      assertEquals(v, JByte.valueOf(s, 16).intValue())
      // TODO
      // assertEquals(v, JByte.decode(IntegerTest.insertAfterSign("0x", s)))
      // assertEquals(v, JByte.decode(IntegerTest.insertAfterSign("0X", s)))
      // assertEquals(v, JByte.decode(IntegerTest.insertAfterSign("#", s)))
    }

    test("0", 0x0)
    test("5", 0x5)
    test("7f", 0x7f)
    test("-24", -0x24)
    test("30", 0x30)
    test("-9", -0x9)
  }

  def decodeStringBase8(): Unit = {
    def test(s: String, v: Byte): Unit = {
      assertEquals(v, JByte.decode(s))
    }

    test("00", 0)
    test("0123", 83)
    test("-012", -10)
  }

  def decodeInvalidThrows(): Unit = {
    def test(s: String): Unit =
      assertThrows(classOf[NumberFormatException], JByte.decode(s))

    // sign after another sign or after a base prefix
    test("++0")
    test("--0")
    test("0x+1")
    test("0X-1")
    test("#-1")
    test("0-1")

    // empty string after sign or after base prefix
    test("")
    test("+")
    test("-")
    test("-0x")
    test("+0X")
    test("#")

    // integer too large
    test("0x80")
    test("-0x81")
    test("0200")
    test("-0201")
  }
}
