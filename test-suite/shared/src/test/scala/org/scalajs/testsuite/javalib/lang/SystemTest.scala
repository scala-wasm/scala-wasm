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

package org.scalajs.testsuite.javalib.lang

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalajs.testsuite.utils.AssertThrows.assertThrows
import org.scalajs.testsuite.utils.Platform._

import scala.scalajs.LinkingInfo.{linkTimeIf, moduleKind}
import scala.scalajs.LinkingInfo.ModuleKind.{MinimalWasmModule, WasmComponent}

class SystemTest {

  @Test def setIn(): Unit = {
    linkTimeIf(moduleKind != MinimalWasmModule && moduleKind != WasmComponent) {
      val savedIn = System.in
      try {
        val testIn = new java.io.ByteArrayInputStream(Array[Byte]())
        System.setIn(testIn)
        assertTrue(System.in eq testIn)
      } finally {
        System.setIn(savedIn)
      }
    } {
      assumeTrue("System.in requires JS interop", false)
    }
  }

  @Test def setOut(): Unit = {
    linkTimeIf(moduleKind != MinimalWasmModule && moduleKind != WasmComponent) {
      val savedOut = System.out
      try {
        val testOut = new java.io.PrintStream(new java.io.ByteArrayOutputStream)
        System.setOut(testOut)
        assertTrue(System.out eq testOut)
      } finally {
        System.setOut(savedOut)
      }
    } {
      assumeTrue("System.out requires JS interop", false)
    }
  }

  @Test def setErr(): Unit = {
    linkTimeIf(moduleKind != MinimalWasmModule && moduleKind != WasmComponent) {
      val savedErr = System.err
      try {
        val testErr = new java.io.PrintStream(new java.io.ByteArrayOutputStream)
        System.setErr(testErr)
        assertTrue(System.err eq testErr)
      } finally {
        System.setErr(savedErr)
      }
    } {
      assumeTrue("System.err requires JS interop", false)
    }
  }

  @Test def currentTimeMillis(): Unit = {
    linkTimeIf(moduleKind != MinimalWasmModule && moduleKind != WasmComponent) {
      // Test that the "scale" (order of magnitude) of currentTimeMillis() is correct
      val result = System.currentTimeMillis()
      assertTrue(result.toString(), result >= 1360059308000L) // timestamp of the first commit of Scala.js
      assertTrue(result.toString(), result <= 2937896108000L) // 50 years later
    } {
      assumeTrue("System.currentTimeMillis requires JS interop", false)
    }
  }

  @Test def nanoTime(): Unit = {
    linkTimeIf(moduleKind != MinimalWasmModule && moduleKind != WasmComponent) {
      /* nanoTime() can return arbitrary results; even negative values.
       * It is supposed to be monotonic, but apparently it sometimes incorrectly
       * goes back in time: https://bugs.java.com/bugdatabase/view_bug?bug_id=6458294
       *
       * So the only thing we can test is that it links.
       */
      System.nanoTime()
      ()
    } {
      assumeTrue("System.nanoTime requires JS interop", false)
    }
  }

  @Test def identityHashCode(): Unit = {
    class HasIDHashCode

    val x1 = new HasIDHashCode
    val x2 = new HasIDHashCode
    val x1FirstHash = x1.hashCode()
    assertEquals(x1FirstHash, x1.hashCode())
    if (!executingInJVM)
      assertNotEquals(x1.hashCode(), x2.hashCode())
    assertEquals(x1FirstHash, x1.hashCode())

    assertEquals(x1FirstHash, System.identityHashCode(x1))
    assertEquals(x2.hashCode(), System.identityHashCode(x2))
  }

  @Test def identityHashCodeNotEqualHashCodeForList(): Unit = {
    val list1 = List(1, 3, 5)
    val list2 = List(1, 3, 5)
    assertEquals(list2, list1)
    assertEquals(list2.hashCode(), list1.hashCode())
    if (!executingInJVM)
      assertNotEquals(System.identityHashCode(list1), System.identityHashCode(list2))
  }

  @Test def identityHashCodeOfNull(): Unit =
    assertEquals(0, System.identityHashCode(null))

  @Test def lineSeparator(): Unit = {
    val lineSep = System.lineSeparator()

    if (!executingInJVM)
      assertEquals("\n", lineSep)
    else
      assertTrue(Set("\n", "\r", "\r\n").contains(lineSep))
  }

  @Test def getenvReturnsUnmodifiableMap(): Unit = {
    assertTrue(System.getenv().isInstanceOf[java.util.Map[String, String]])

    assertThrows(classOf[Exception], System.getenv.put("", ""))
  }

  @Test def getenvLinksAndDoesNotThrow(): Unit =
    assertEquals(null, System.getenv(":${PATH}"))
}
