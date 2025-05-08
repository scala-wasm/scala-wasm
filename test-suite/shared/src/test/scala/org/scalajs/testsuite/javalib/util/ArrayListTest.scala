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

package org.scalajs.testsuite.javalib.util

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalajs.testsuite.utils.AssertThrows.assertThrows
import org.scalajs.testsuite.utils.Platform

import java.{util => ju}

import scala.reflect.ClassTag

class ArrayListTest extends AbstractListTest {

  override def factory: ArrayListFactory = new ArrayListFactory

  @Test def ensureCapacity(): Unit = {
    // note that these methods become no ops in js
    val al = new ju.ArrayList[String]
    al.ensureCapacity(0)
    al.ensureCapacity(34)
    al.trimToSize()
  }

  @Test def constructorInitialCapacity(): Unit = {
    val al1 = new ju.ArrayList(0)
    assertTrue(al1.size() == 0)
    assertTrue(al1.isEmpty())

    val al2 = new ju.ArrayList(2)
    assertTrue(al2.size() == 0)
    assertTrue(al2.isEmpty())

    assertThrows(classOf[IllegalArgumentException], new ju.ArrayList(-1))
  }

  @Test def constructorNullThrowsNullPointerException(): Unit = {
    assumeTrue("assumed compliant NPEs", Platform.hasCompliantNullPointers)
    assertThrows(classOf[NullPointerException], new ju.ArrayList(null))
  }

  @Test def testClone(): Unit = {
    val al1 = factory.fromElements[Int](1, 2)
    val al2 = al1.clone().asInstanceOf[ju.ArrayList[Int]]
    al1.add(100)
    al2.add(200)
    assertTrue(Array[Int](1, 2, 100).sameElements(al1.toArray()))
    assertTrue(Array[Int](1, 2, 200).sameElements(al2.toArray()))
  }

  @Test def removeRangeFromIdenticalIndices(): Unit = {
    val al = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(-175, 24, 7, 44))
    val expected = Array[Int](-175, 24, 7, 44)
    al.removeRangeList(0, 0)
    assertTrue(al.toArray().sameElements(expected))
    al.removeRangeList(1, 1)
    assertTrue(al.toArray().sameElements(expected))
    al.removeRangeList(al.size, al.size) // no op
    assertTrue(al.toArray().sameElements(expected))
  }

  @Test def removeRangeFromToInvalidIndices(): Unit = {
    val al = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(175, -24, -7, -44))

    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      al.removeRangeList(-1, 2)
    ) // fromIndex < 0
    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      al.removeRangeList(0, al.size + 1)
    ) // toIndex > size
    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      al.removeRangeList(2, -1)
    ) // toIndex < fromIndex
  }

  @Test def removeRangeFromToFirstTwoElements(): Unit = {
    val al = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(284, -27, 995, 500, 267, 904))
    val expected = Array[Int](995, 500, 267, 904)
    al.removeRangeList(0, 2)
    assertTrue(al.toArray().sameElements(expected))
  }

  @Test def removeRangeFromToTwoElementsFromMiddle(): Unit = {
    val al = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(7, 9, -1, 20))
    val expected = Array[Int](7, 20)
    al.removeRangeList(1, 3)
    assertTrue(al.toArray().sameElements(expected))
  }

  @Test def removeRangeFromToLastTwoElementsAtTail(): Unit = {
    val al = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(50, 72, 650, 12, 7, 28, 3))
    val expected = Array[Int](50, 72, 650, 12, 7)
    al.removeRangeList(al.size - 2, al.size)
    assertTrue(al.toArray().sameElements(expected))
  }
}

class ArrayListFactory extends AbstractListFactory {
  override def implementationName: String =
    "java.util.ArrayList"

  override def empty[E: ClassTag]: ju.ArrayList[E] =
    new ju.ArrayList[E]

  override def fromElements[E: ClassTag](coll: E*): ju.ArrayList[E] =
    new ju.ArrayList[E](TrivialImmutableCollection(coll: _*))
}

class ArrayListRangeRemovable[E](c: ju.Collection[_ <: E]) extends ju.ArrayList[E](c) {
  def removeRangeList(fromIndex: Int, toIndex: Int): Unit = {
    removeRange(fromIndex, toIndex)
  }
}
