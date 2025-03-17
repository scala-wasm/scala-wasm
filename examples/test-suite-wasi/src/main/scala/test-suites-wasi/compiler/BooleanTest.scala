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

object BooleanTest {
  def bitwiseAndOrXorOperators(): Unit = {
    assertFalse(false & false)
    assertFalse(false & true)
    assertFalse(true & false)
    assertTrue(true & true)

    assertFalse(false | false)
    assertTrue(true | false)
    assertTrue(false | true)
    assertTrue(true | true)

    assertFalse(false ^ false)
    assertTrue(true ^ false)
    assertTrue(false ^ true)
    assertFalse(true ^ true)
  }
}
