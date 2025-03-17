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

class StoreModuleTest {
  import StoreModuleTest._

  def scalaModuleClass(): Unit = {
    val a = ScalaObjA
    val b = ScalaObjB

    assertNotNull(a)
    assertNotNull(b)
    assertSame(a, b.a)
    assertSame(b, a.b)
  }
}

object StoreModuleTest {
  object ScalaObjA {
    val b = ScalaObjB
  }

  object ScalaObjB {
    val a = ScalaObjA
  }
}
