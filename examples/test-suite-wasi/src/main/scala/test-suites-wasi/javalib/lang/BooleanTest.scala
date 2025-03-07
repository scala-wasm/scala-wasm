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

import testSuiteWASI.Assertions._

import java.lang.{Boolean => JBoolean}

// import org.scalajs.testsuite.utils.AssertThrows.assertThrows
// import org.scalajs.testsuite.utils.Platform._

/** Tests the implementation of the java standard library Boolean
 */
class BooleanTest {

  def booleanValue(): Unit = {
    assertEquals(true, JBoolean.TRUE.booleanValue())
    assertEquals(false, JBoolean.FALSE.booleanValue())
  }

  def booleanValueNull(): Unit = {
    // assumeTrue("assuming compliant null pointer checks", hasCompliantNullPointers)

    assertThrows(classOf[NullPointerException], (null: JBoolean).booleanValue())
  }

  def compareTo(): Unit = {
    def compare(x: Boolean, y: Boolean): Int =
      new JBoolean(x).compareTo(new JBoolean(y))

    assertEquals(0, compare(false, false))
    assertTrue(compare(false, true) < 0)
    assertTrue(compare(true, false) > 0)
    assertEquals(0, compare(true, true))
  }

  def compareToAnyAny(): Unit = {
    def compare(x: Any, y: Any): Int =
      x.asInstanceOf[Comparable[Any]].compareTo(y)

    assertEquals(0, compare(false, false))
    assertTrue(compare(false, true) < 0)
    assertTrue(compare(true, false) > 0)
    assertEquals(0, compare(true, true))
  }

  def parseStringMethods(): Unit = {
    def test(s: String, v: Boolean): Unit = {
      assertEquals(v, JBoolean.parseBoolean(s))
      assertEquals(v, JBoolean.valueOf(s).booleanValue())
      assertEquals(v, new JBoolean(s).booleanValue())
    }

    test("false", false)
    test("true", true)
    test("TrUe", true)
    test(null, false)
    test("truee", false)
  }
}
