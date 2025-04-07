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
import testSuiteWASI.Platform._

class StackTraceElementTest {

  def toStringUnmodifiedIfColumnNumberIsNotSpecified(): Unit = {
    val st = new StackTraceElement("MyClass", "myMethod", "myFile.scala", 1)
    assertEquals("MyClass.myMethod(myFile.scala:1)", st.toString)
  }
}
