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

import testSuiteWASI.Assertions._

class UnitTest {
  def testHashCode(): Unit = {
    assertEquals(0, ().hashCode())
    assertEquals(0, ((): Any).hashCode())
    assertEquals(0, ().##)
  }

  def testEquals(): Unit = {
    assertTrue(().asInstanceOf[AnyRef].equals(().asInstanceOf[AnyRef]))
  }

  def testEqualsOtherValues(): Unit = {
    def testAgainst(v: Any): Unit = {
      assertFalse(().asInstanceOf[AnyRef].equals(v.asInstanceOf[AnyRef]))
    }

    testAgainst(0)
    testAgainst(1)
    testAgainst(null)
    testAgainst(false)
    testAgainst("")
  }
}
