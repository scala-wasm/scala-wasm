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

import testSuiteWASI.junit.Assert._
import testSuiteWASI.Platform._

// import org.scalajs.testsuite.utils.Platform.executingInJVM

class ThreadTest {

  def getNameAndSetName(): Unit = {
    if (!executingInJVM) {
      val t = Thread.currentThread()
      assertEquals("main", t.getName) // default name of the main thread
      t.setName("foo")
      try {
        assertEquals("foo", t.getName)
      } finally {
        t.setName("main") // don't pollute the rest of the world with this test
      }
      assertEquals("main", t.getName)
    }
  }

  def currentThreadGetStackTrace(): Unit = {
    Thread.currentThread().getStackTrace()
  }

  def getId(): Unit = {
    assertTrue(Thread.currentThread().getId > 0)
  }

  def interruptExistsAndTheStatusIsProperlyReflected(): Unit = {
    val t = Thread.currentThread()
    assertFalse(t.isInterrupted())
    assertFalse(Thread.interrupted())
    assertFalse(t.isInterrupted())
    t.interrupt()
    assertTrue(t.isInterrupted())
    assertTrue(Thread.interrupted())
    assertFalse(t.isInterrupted())
    assertFalse(Thread.interrupted())
  }
}
