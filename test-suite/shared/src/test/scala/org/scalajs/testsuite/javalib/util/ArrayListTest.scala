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
import java.util.Arrays

import scala.reflect.ClassTag
import scala.scalajs.LinkingInfo

class ArrayListTest extends AbstractListTest {

  override def factory: ArrayListFactory = new ArrayListFactory

  @Test def ensureCapacity(): Unit = {
    // note that these methods become no ops in js
    val al = new ju.ArrayList[String]
    al.ensureCapacity(0)
    al.ensureCapacity(34)
    al.trimToSize()
  }

 @Test def constructor(): Unit = {
    val al = new ju.ArrayList()
    assertTrue(al.size() == 0)
    assertTrue(al.isEmpty())
  }

  @Test def constructorInt(): Unit = {
    val al = new ju.ArrayList(10)
    assertTrue(al.size() == 0)
    assertTrue(al.isEmpty())
    // the capacity is opaque. exposing it just for testing is avoided
  }

  @Test def constructorCollectionInteger(): Unit = {
    val ls = TrivialImmutableCollection(1, 5, 2, 3, 4)
    val al = factory.from[Int](ls)
    assertTrue(al.size() == 5)
    assertTrue(!al.isEmpty())
    assertTrue(al.get(0) == 1)
    assertTrue(al.get(1) == 5)
    assertTrue(al.get(2) == 2)
    assertTrue(al.get(3) == 3)
    assertTrue(al.get(4) == 4)
  }

  @Test def constructorCollectionString(): Unit = {
    val ls = TrivialImmutableCollection("1", "2", "3")
    val al = factory.from[String](ls)
    assertTrue(al.size() == 3)
    assertTrue(!al.isEmpty())
    assertTrue(al.get(0) == "1")
    assertTrue(al.get(1) == "2")
    assertTrue(al.get(2) == "3")
  }

  @Test def constructorNullThrowsNullPointerException(): Unit = {
    assumeTrue("assumed compliant NPEs", Platform.hasCompliantNullPointers)
    assertThrows(classOf[NullPointerException], new ju.ArrayList(null))
  }

  @Test def equalsForEmptyLists(): Unit = {
    val e1 = factory.empty
    val e2 = factory.empty
    val ne1 = factory.from[Int](TrivialImmutableCollection(1))
    assertTrue(e1 == e2)
    assertTrue(e2 == e1)
    assertTrue(e1 != ne1)
    assertTrue(ne1 != e1)
  }

  @Test def equalsForNonEmptyLists(): Unit = {
    val ne1a = factory.from[Int](TrivialImmutableCollection(1, 2, 3))
    val ne1b = factory.from[Int](TrivialImmutableCollection(1, 2, 3))
    val ne2 = factory.from[Int](TrivialImmutableCollection(1))
    assertTrue(ne1a == ne1b)
    assertTrue(ne1b == ne1a)
    assertTrue(ne1a != ne2)
    assertTrue(ne2 != ne1a)
    assertTrue(ne1b != ne2)
    assertTrue(ne2 != ne1b)
  }

  @Test def trimToSizeForNonEmptyListsWithDifferentCapacities(): Unit = {
    val al1 = factory.from[Int](TrivialImmutableCollection(1, 2, 3))
    val al2 = factory.from[Int](TrivialImmutableCollection(1, 2, 3))
    al2.ensureCapacity(100)
    val al3 = new ju.ArrayList[Int](50)
    al3.add(1)
    al3.add(2)
    al3.add(3)
    assertTrue(al1 == al2)
    assertTrue(al2 == al3)
  }

  @Test def trimToSizeForEmptyLists(): Unit = {
    val al1 = new ju.ArrayList()
    al1.trimToSize()
    val al2 = new ju.ArrayList()
    assertTrue(al1 == al2)
  }

  @Test def trimToSizeForNonEmptyLists(): Unit = {
    val al1 = factory.from[Int](TrivialImmutableCollection(1, 2, 3))
    val al2 = factory.from[Int](TrivialImmutableCollection(1, 2, 3))
    al2.ensureCapacity(100)
    val al3 = new ju.ArrayList[Int](50)
    al3.add(1)
    al3.add(2)
    al3.add(3)
    al1.trimToSize()
    al2.trimToSize()
    al3.trimToSize()
    assertTrue(al1 == al2)
    assertTrue(al2 == al3)
  }

  @Test def size(): Unit = {
    val al1 = new ju.ArrayList[Int]()
    assertTrue(al1.size() == 0)
    val al2 = factory.from[Int](TrivialImmutableCollection(1, 2, 3))
    assertTrue(al2.size() == 3)
    val al3 = new ju.ArrayList[Int](10)
    // not to be confused with its capacity.
    assertTrue(al3.size() == 0)
  }

  @Test def isEmpty(): Unit = {
    val al1 = new ju.ArrayList[Int]()
    assertTrue(al1.isEmpty())
    val al2 = factory.from[Int](TrivialImmutableCollection(1, 2, 3))
    assertTrue(!al2.isEmpty())
    val al3 = new ju.ArrayList[Int](10)
    // not to be confused with its capacity.
    assertTrue(al3.isEmpty())
  }

  @Test def indexOfAny(): Unit = {
    val al1 = factory.from[Int](TrivialImmutableCollection(1, 2, 3, 2))
    assertTrue(al1.indexOf(2) == 1)
  }

  @Test def lastIndexOfAny(): Unit = {
    val al1 = factory.from[Int](TrivialImmutableCollection(1, 2, 3, 2))
    assertTrue(al1.lastIndexOf(2) == 3)
  }

  @Test def testClone(): Unit = {
    val al1 = factory.from[Int](TrivialImmutableCollection(1, 2, 3, 2))
    val al2 = al1.clone().asInstanceOf[ju.ArrayList[Int]]
    assertTrue(al1 == al2)
    al1.add(1)
    assertTrue(al1 != al2)
    al2.add(1)
    assertTrue(al1 == al2)
  }

  @Test def cloneWithSizeNotEqualCapacity(): Unit = {
    val al1 = factory.from[Int](TrivialImmutableCollection(1, 2, 3, 2))
    al1.ensureCapacity(20)
    val al2 = al1.clone().asInstanceOf[ju.ArrayList[Int]]
    assertTrue(al1 == al2)
    al1.add(1)
    assertTrue(al1 != al2)
    al2.add(1)
    assertTrue(al1 == al2)
  }

  @Test def toArray(): Unit = {
    val al1 = factory.from[Int](TrivialImmutableCollection(1, 2, 3, 2))
    assertTrue(
      (Array(1, 2, 3, 2).map(_.asInstanceOf[AnyRef])) sameElements
        (al1.toArray())
    )
  }

  @Test def toArrayDefaultInitialCapacityThenAddElements(): Unit = {
    // Issue #1500 discovered by re2s ExecTestSuite.

    val al1 = new ju.ArrayList[String]()
    val data = Array("alpha", "omega")
    val expectedSize = data.size

    for (d <- data) al1.add(d)

    val arr1 = al1.toArray
    val arr1Size = arr1.size

    assertTrue(
      s"toArray.size: $arr1Size != expected: $expectedSize",
      arr1Size == expectedSize
    )

    // Discovering code in re2s ExecTestSuite used .deep not sameElements.
    // Should have same result as sameElements, but via different path.

    assertTrue(
      "a1.toArray.deep != data.deep",
      Arrays.deepEquals(arr1, data.asInstanceOf[Array[AnyRef]])
    )
  }

  @Test def toArrayArrayWhenArrayIsShorter(): Unit = {
    val al1 = factory.from[String](TrivialImmutableCollection("apple", "banana", "cherry"))
    val ain = Array.empty[String]
    val aout = al1.toArray(ain)
    assertTrue(ain ne aout)
    assertTrue(Array("apple", "banana", "cherry") sameElements aout)
  }

  @Test def toArrayArrayWhenArrayIsWithTheSameLengthOrLonger(): Unit = {
    val al1 = factory.from[String](TrivialImmutableCollection("apple", "banana", "cherry"))
    val ain = Array.fill(4)("foo")
    val aout = al1.toArray(ain)
    assertTrue(ain eq aout)
    assertTrue(Array("apple", "banana", "cherry", null) sameElements aout)
  }

  @Test def arrayEToArrayTWhenTSubE(): Unit = {
    class SuperClass
    class SubClass extends SuperClass
    val in = TrivialImmutableCollection(new SubClass, new SubClass)
    val al1 = factory.from[SubClass](in)
    val aout = al1.toArray(Array.empty[SuperClass])
    assertTrue(in.toArray sameElements aout)
  }

  // This test works on Scastie/JVM
  @Test def arrayEToArrayTShouldThrowArrayStoreExceptionWhenNotTSubE(): Unit = {
    class NotSuperClass
    class SubClass
    val al1 = new ju.ArrayList[SubClass]()
    assertTrue(
      al1
        .toArray(Array.empty[NotSuperClass])
        .isInstanceOf[Array[NotSuperClass]]
    )
  }

  @Test def toArrayNullThrowsNull(): Unit = {
    assumeTrue("assumed compliant NPEs", Platform.hasCompliantNullPointers)
    val al1 = factory.from[String](TrivialImmutableCollection("apple", "banana", "cherry"))
    assertThrows(classOf[NullPointerException], al1.toArray(null))
  }

  @Test def getInt(): Unit = {
    val al1 = factory.from[Int](TrivialImmutableCollection(1, 2, 3, 2))
    assertTrue(al1.get(0) == 1)
    assertTrue(al1.get(1) == 2)
    assertTrue(al1.get(2) == 3)
    assertTrue(al1.get(3) == 2)
    assertThrows(classOf[IndexOutOfBoundsException], al1.get(-1))
    assertThrows(classOf[IndexOutOfBoundsException], al1.get(4))
  }

  @Test def setInt(): Unit = {
    val al1 = factory.from[Int](TrivialImmutableCollection(1, 2, 3, 2))
    assertTrue(al1.set(1, 4) == 2)
    assertTrue(Array(1, 4, 3, 2) sameElements al1.toArray)
  }

  @Test def add(): Unit = {
    val al1 = new ju.ArrayList[Int]()
    al1.add(1)
    assertTrue(Array(1) sameElements al1.toArray)
    al1.add(2)
    assertTrue(Array(1, 2) sameElements al1.toArray)
  }

  @Test def addInt(): Unit = {
    val al1 = new ju.ArrayList[Int]()
    al1.add(0, 1)
    assertTrue(Array(1) sameElements al1.toArray)
    al1.add(0, 2)
    assertTrue(Array(2, 1) sameElements al1.toArray)
  }

  @Test def addIntWhenTheCapacityHasToBeExpanded(): Unit = {
    val al1 = new ju.ArrayList[Int](0)
    al1.add(0, 1)
    assertTrue(Array(1) sameElements al1.toArray)
    al1.add(0, 2)
    assertTrue(Array(2, 1) sameElements al1.toArray)
  }

  @Test def addAll(): Unit = {
    val l = new java.util.ArrayList[String]()
    l.add("First")
    l.add("Second")
    val l2 = new java.util.ArrayList[String]()
    l2.addAll(0, l)
    val iter = l2.iterator()
    assertTrue(iter.next() == "First")
    assertTrue(iter.next() == "Second")
    assertTrue(!iter.hasNext())
  }

  @Test def removeInt(): Unit = {
    val al1 = factory.from[Int](TrivialImmutableCollection(1, 2, 3, 2, 3))
    // remove last
    assertTrue(al1.remove(4) == 3)
    // remove head
    assertTrue(al1.remove(0) == 1)
    // remove middle
    assertTrue(al1.remove(1) == 3)
    assertThrows(classOf[IndexOutOfBoundsException], al1.remove(4))
    assertTrue(Array(2, 2) sameElements al1.toArray)
  }

  @Test def removeAny(): Unit = {
    val al1 = factory.from[Int](TrivialImmutableCollection(1, 2, 3, 2))
    assertTrue(al1.remove(2: Any) == true)
    assertTrue(Array(1, 3, 2) sameElements al1.toArray)
    assertTrue(al1.remove(4: Any) == false)
    assertTrue(Array(1, 3, 2) sameElements al1.toArray)
  }

  @Test def removeRangeFromToIndenticalInvalidIndices(): Unit = {
    assumeTrue("Assume targeting pure Wasm, JS-based impl doesn't throw.",
        LinkingInfo.targetPureWasm)

    val aList = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(-175, 24, 7, 44))
    val expected = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(-175, 24, 7, 44))

    // Yes, the indices are invalid but no exception is expected because
    // they are identical, which is tested first. That is called a 'quirk'
    // or 'implementation dependent detail' of the documented specification.

    aList.removeRangeList(-1, -1)

    assertTrue(s"result: $aList != expected: $expected", aList == expected)
  }

  @Test def removeRangeFromToInvalidIndices(): Unit = {
    assumeTrue("Assume targeting pure Wasm, JS-based impl doesn't throw.",
        LinkingInfo.targetPureWasm)
    val aList = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(175, -24, -7, -44))

    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      aList.removeRangeList(-1, 2)
    ) // fromIndex < 0

    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      // Beware that from != too in this test.
      // See 'from == to' quirk tested above.
      aList.removeRangeList(aList.size, aList.size + 2)
    ) // fromIndex >= _size

    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      aList.removeRangeList(0, aList.size + 1)
    ) // toIndex > size

    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      aList.removeRangeList(2, -1)
    ) // toIndex < fromIndex
  }

  @Test def removeRangeFromToFirstTwoElements(): Unit = {
    val aList = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(284, -27, 995, 500, 267, 904))
    val expected = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(995, 500, 267, 904))

    aList.removeRangeList(0, 2)

    assertTrue(s"result: $aList != expected: $expected", aList == expected)
  }

  @Test def removeRangeFromToFirstTwoElementsAtHead(): Unit = {
    val aList = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(284, -27, 995, 500, 267, 904))
    val expected = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(995, 500, 267, 904))

    aList.removeRangeList(0, 2)

    assertTrue(s"result: $aList != expected: $expected", aList == expected)
  }

  @Test def removeRangeFromToTwoElementsFromMiddle(): Unit = {
    val aList = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(7, 9, -1, 20))
    val expected = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(7, 20))

    aList.removeRangeList(1, 3)

    assertTrue(s"result: $aList != expected: $expected", aList == expected)
  }

  @Test def removeRangeFromToLastTwoElementsAtTail(): Unit = {
    val aList = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(50, 72, 650, 12, 7, 28, 3))
    val expected = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(50, 72, 650, 12, 7))

    aList.removeRangeList(aList.size - 2, aList.size)

    assertTrue(s"result: $aList != expected: $expected", aList == expected)
  }

  @Test def removeRangeFromToEntireListAllElements(): Unit = {
    val aList = new ArrayListRangeRemovable[Int](
        TrivialImmutableCollection(50, 72, 650, 12, 7, 28, 3))
    val expected = new ArrayListRangeRemovable[Int](TrivialImmutableCollection())

    aList.removeRangeList(0, aList.size)

    assertTrue(s"result: $aList != expected: $expected", aList == expected)
  }

  @Test def clearTest(): Unit = {
    val al1 = factory.from[Int](TrivialImmutableCollection(1, 2, 3, 2))
    al1.clear()
    assertTrue(al1.isEmpty())
    // makes sure that clear()ing an already empty list is safe
    al1.clear()
  }

  @Test def shouldThrowAnErrorWithNegativeInitialCapacity(): Unit = {
    assertThrows(classOf[IllegalArgumentException], new ju.ArrayList(-1))
  }

  @Test def containsAny(): Unit = {
    val al = factory.from[Int](TrivialImmutableCollection(1, 2, 3, 2))
    assertTrue(al.contains(1))
    assertTrue(!al.contains(5))
  }

  @Test def testToString(): Unit = {
    val al = factory.from[Int](TrivialImmutableCollection(1, 2, 3, 2))
    val expected = "[1, 2, 3, 2]"
    val result = al.toString
    assertTrue(
      s"result: ${result} != expected: ${expected}",
      result == expected
    )
  }
}

class ArrayListFactory extends AbstractListFactory {
  override def implementationName: String =
    "java.util.ArrayList"

  override def empty[E: ClassTag]: ju.ArrayList[E] =
    new ju.ArrayList[E]

  def from[E](coll: ju.Collection[E]): ju.ArrayList[E] =
    new ju.ArrayList[E](coll)
}

class ArrayListRangeRemovable[E](c: ju.Collection[_ <: E]) extends ju.ArrayList[E](c) {
  def removeRangeList(fromIndex: Int, toIndex: Int): Unit = {
    removeRange(fromIndex, toIndex)
  }
}
