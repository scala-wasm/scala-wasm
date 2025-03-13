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

/** Tests for `System.arraycopy`.
 *
 *  We test with Arrays of Ints, Booleans, Objects and Strings:
 *
 *  - `Array[Int]` is a specialized primitive array type backed by a TypedArray
 *  - `Array[Boolean]` is a specialized primitive array type backed by an Array
 *  - `Array[Object]` is a specialized reference array type (backed by an Array)
 *  - `Array[String]` is a generic reference array type (backed by an Array)
 */
class SystemArraycopyTest {
  import SystemArraycopyTest._

  import System.arraycopy

  private def covariantUpcast[A <: AnyRef](array: Array[_ <: A]): Array[A] =
    array.asInstanceOf[Array[A]]

  @noinline
  private def assertArrayRefEquals[A <: AnyRef](expected: Array[A], actual: Array[A]): Unit =
    assertArrayEquals(expected.asInstanceOf[Array[AnyRef]], actual.asInstanceOf[Array[AnyRef]])

  // TODO: assertThrows
  @noinline
  private def assertThrowsAIOOBE[U](code: => U): Unit = // ArrayIndexOutOfBoundsException =
    assertThrows(classOf[ArrayIndexOutOfBoundsException], code)

  // TODO: assertThrows
  @noinline
  private def assertThrowsASE[U](code: => U): Unit = // ArrayStoreException =
    assertThrows(classOf[ArrayStoreException], code)

  def simpleTests(): Unit = {
    val object0 = Array[Any]("[", "b", "c", "d", "e", "f", "]")
    val object1 = Array[Any](() => true, 1, "2", '3', 4.0, true, object0)

    System.arraycopy(object1, 1, object0, 1, 5)
    val executingInJVM = false
    if (executingInJVM) {
      assertEquals("[1234.0true]", object0.mkString)
    } else {
      assertEquals("[1234true]", object0.mkString)
    }

    val string0 = Array("a", "b", "c", "d", "e", "f")
    val string1 = Array("1", "2", "3", "4")

    System.arraycopy(string1, 0, string0, 3, 3)
    assertEquals("abc123", string0.mkString)

    val ab01Chars = Array("ab".toCharArray, "01".toCharArray)
    val chars = new Array[Array[Char]](32)
    System.arraycopy(ab01Chars, 0, chars, 0, 2)
    for (i <- Seq(0, 2, 4, 8, 16)) {
      System.arraycopy(chars, i / 4, chars, i, i)
    }

    assertEquals(12, chars.filter(_ == null).length)
    assertEquals("ab01ab0101ab01ab0101ab0101ab01ab0101ab01",
        chars.filter(_ != null).map(_.mkString).mkString)
  }

  def arraycopyWithRangeOverlapsForTheSameArrayInt(): Unit = {
    val array = new Array[Int](10)

    for (i <- 1 to 6)
      array(i) = i

    assertArrayEquals(Array(0, 1, 2, 3, 4, 5, 6, 0, 0, 0), array)
    arraycopy(array, 0, array, 3, 7)
    assertArrayEquals(Array(0, 1, 2, 0, 1, 2, 3, 4, 5, 6), array)

    arraycopy(array, 0, array, 1, 0)
    assertArrayEquals(Array(0, 1, 2, 0, 1, 2, 3, 4, 5, 6), array)

    arraycopy(array, 0, array, 1, 9)
    assertArrayEquals(Array(0, 0, 1, 2, 0, 1, 2, 3, 4, 5), array)

    arraycopy(array, 1, array, 0, 9)
    assertArrayEquals(Array(0, 1, 2, 0, 1, 2, 3, 4, 5, 5), array)

    arraycopy(array, 0, array, 0, 10)
    assertArrayEquals(Array(0, 1, 2, 0, 1, 2, 3, 4, 5, 5), array)

    val reversed = array.reverse
    arraycopy(reversed, 5, array, 5, 5)
    assertArrayEquals(Array(0, 1, 2, 0, 1, 1, 0, 2, 1, 0), array)
  }

  def arraycopyWithRangeOverlapsForTheSameArrayBoolean(): Unit = {
    val array = new Array[Boolean](10)

    for (i <- 1 to 6)
      array(i) = (i % 2) == 1

    assertArrayEquals(Array(false, true, false, true, false, true, false, false, false, false), array)
    arraycopy(array, 0, array, 3, 7)
    assertArrayEquals(Array(false, true, false, false, true, false, true, false, true, false), array)

    arraycopy(array, 0, array, 1, 0)
    assertArrayEquals(Array(false, true, false, false, true, false, true, false, true, false), array)

    arraycopy(array, 0, array, 1, 9)
    assertArrayEquals(Array(false, false, true, false, false, true, false, true, false, true), array)

    arraycopy(array, 1, array, 0, 9)
    assertArrayEquals(Array(false, true, false, false, true, false, true, false, true, true), array)

    arraycopy(array, 0, array, 0, 10)
    assertArrayEquals(Array(false, true, false, false, true, false, true, false, true, true), array)

    val reversed = array.reverse
    arraycopy(reversed, 5, array, 5, 5)
    assertArrayEquals(Array(false, true, false, false, true, true, false, false, true, false), array)
  }

  def arraycopyWithRangeOverlapsForTheSameArrayObject(): Unit = {
    val array = new Array[AnyRef](10)

    for (i <- 1 to 6)
      array(i) = O(i)

    assertArrayEquals(Array[AnyRef](null, O(1), O(2), O(3), O(4), O(5), O(6), null, null, null), array)
    arraycopy(array, 0, array, 3, 7)
    assertArrayEquals(Array[AnyRef](null, O(1), O(2), null, O(1), O(2), O(3), O(4), O(5), O(6)), array)

    arraycopy(array, 0, array, 1, 0)
    assertArrayEquals(Array[AnyRef](null, O(1), O(2), null, O(1), O(2), O(3), O(4), O(5), O(6)), array)

    arraycopy(array, 0, array, 1, 9)
    assertArrayEquals(Array[AnyRef](null, null, O(1), O(2), null, O(1), O(2), O(3), O(4), O(5)), array)

    arraycopy(array, 1, array, 0, 9)
    assertArrayEquals(Array[AnyRef](null, O(1), O(2), null, O(1), O(2), O(3), O(4), O(5), O(5)), array)

    arraycopy(array, 0, array, 0, 10)
    assertArrayEquals(Array[AnyRef](null, O(1), O(2), null, O(1), O(2), O(3), O(4), O(5), O(5)), array)

    val reversed = array.reverse
    arraycopy(reversed, 5, array, 5, 5)
    assertArrayEquals(Array[AnyRef](null, O(1), O(2), null, O(1), O(1), null, O(2), O(1), null), array)
  }

  def arraycopyWithRangeOverlapsForTheSameArrayString(): Unit = {
    val array = new Array[String](10)

    for (i <- 1 to 6)
      array(i) = i.toString()

    assertArrayRefEquals(Array(null, "1", "2", "3", "4", "5", "6", null, null, null), array)
    arraycopy(array, 0, array, 3, 7)
    assertArrayRefEquals(Array(null, "1", "2", null, "1", "2", "3", "4", "5", "6"), array)

    arraycopy(array, 0, array, 1, 0)
    assertArrayRefEquals(Array(null, "1", "2", null, "1", "2", "3", "4", "5", "6"), array)

    arraycopy(array, 0, array, 1, 9)
    assertArrayRefEquals(Array(null, null, "1", "2", null, "1", "2", "3", "4", "5"), array)

    arraycopy(array, 1, array, 0, 9)
    assertArrayRefEquals(Array(null, "1", "2", null, "1", "2", "3", "4", "5", "5"), array)

    arraycopy(array, 0, array, 0, 10)
    assertArrayRefEquals(Array(null, "1", "2", null, "1", "2", "3", "4", "5", "5"), array)

    val reversed = array.reverse
    arraycopy(reversed, 5, array, 5, 5)
    assertArrayRefEquals(Array(null, "1", "2", null, "1", "1", null, "2", "1", null), array)
  }

  def arraycopyNulls(): Unit = {
    // assumeTrue("Assuming compliant NullPointers",
    //     hasCompliantNullPointers)

    @noinline def assertThrowsNPE[U](body: => U): Unit =
      assertThrows(classOf[NullPointerException], body)

    @noinline def getNull(): Any = null
    val nul = getNull()

    val nullArrayRef = nul.asInstanceOf[Array[AnyRef]]
    val nullArrayInt = nul.asInstanceOf[Array[Int]]

    val arrayRef = new Array[AnyRef](10)
    val arrayInt = new Array[Int](10)

    val otherValues = List[Any](
      null,
      arrayRef,
      arrayInt,
      new Array[String](10),
      "foo",
      (),
      List(1)
    )

    for (otherValue <- otherValues) {
      assertThrowsNPE(arraycopy(nul, 0, otherValue, 0, 0))
      assertThrowsNPE(arraycopy(otherValue, 0, nul, 0, 0))

      assertThrowsNPE(arraycopy(nul, 0, otherValue, 0, 1))
      assertThrowsNPE(arraycopy(otherValue, 0, nul, 0, 1))

      assertThrowsNPE(arraycopy(nul, -1, otherValue, 0, 1))
      assertThrowsNPE(arraycopy(otherValue, -1, nul, 0, 1))

      assertThrowsNPE(arraycopy(nul, 5, otherValue, 0, 1))
      assertThrowsNPE(arraycopy(otherValue, 5, nul, 0, 1))

      assertThrowsNPE(arraycopy(nul, 15, otherValue, 0, 1))
      assertThrowsNPE(arraycopy(otherValue, 15, nul, 0, 1))

      assertThrowsNPE(arraycopy(nul, 0, otherValue, -1, 1))
      assertThrowsNPE(arraycopy(otherValue, 0, nul, -1, 1))

      assertThrowsNPE(arraycopy(nul, 0, otherValue, 5, 1))
      assertThrowsNPE(arraycopy(otherValue, 0, nul, 5, 1))

      assertThrowsNPE(arraycopy(nul, 0, otherValue, 15, 1))
      assertThrowsNPE(arraycopy(otherValue, 0, nul, 15, 1))

      assertThrowsNPE(arraycopy(nullArrayRef, 0, otherValue, 0, 1))
      assertThrowsNPE(arraycopy(otherValue, 0, nullArrayRef, 0, 1))
    }

    assertThrowsNPE(arraycopy(nullArrayRef, 0, arrayRef, 5, 1))
    assertThrowsNPE(arraycopy(arrayRef, 0, nullArrayRef, 5, 1))

    assertThrowsNPE(arraycopy(nullArrayInt, 0, arrayInt, 5, 1))
    assertThrowsNPE(arraycopy(arrayInt, 0, nullArrayInt, 5, 1))
  }

  def arraycopyNullsShortcircuited(): Unit = {
    @noinline def getNull(): Any = null
    val nul = getNull()

    val nullArrayRef = nul.asInstanceOf[Array[AnyRef]]
    val nullArrayInt = nul.asInstanceOf[Array[Int]]

    val arrayRef = new Array[AnyRef](10)
    val arrayInt = new Array[Int](10)

    def throwIllegalArgNothing(): Nothing =
      throw new IllegalArgumentException

    @noinline def assertThrowsIllegalArg[U](body: => U): Unit =
      assertThrows(classOf[IllegalArgumentException], body)

    @noinline def throwIllegalArgArrayRef(): Array[AnyRef] =
      throwIllegalArgNothing()

    @noinline def throwIllegalArgArrayInt(): Array[Int] =
      throwIllegalArgNothing()

    @noinline def throwIllegalArgInt(): Int =
      throwIllegalArgNothing()

    assertThrowsIllegalArg(arraycopy(nul, throwIllegalArgInt(), nul, 0, 0))
    assertThrowsIllegalArg(arraycopy(nul, 0, nul, 0, throwIllegalArgInt()))

    assertThrowsIllegalArg(arraycopy(nul, 0, throwIllegalArgArrayRef(), 0, 0))
    assertThrowsIllegalArg(arraycopy(nullArrayRef, 0, throwIllegalArgArrayRef(), 0, 0))
    assertThrowsIllegalArg(arraycopy(throwIllegalArgArrayRef(), 0, nul, 0, 0))
    assertThrowsIllegalArg(arraycopy(throwIllegalArgArrayRef(), 0, nullArrayRef, 0, 0))

    assertThrowsIllegalArg(arraycopy(nul, 0, throwIllegalArgArrayInt(), 0, 0))
    assertThrowsIllegalArg(arraycopy(nullArrayInt, 0, throwIllegalArgArrayInt(), 0, 0))
    assertThrowsIllegalArg(arraycopy(throwIllegalArgArrayInt(), 0, nul, 0, 0))
    assertThrowsIllegalArg(arraycopy(throwIllegalArgArrayInt(), 0, nullArrayInt, 0, 0))
  }

  def arraycopyIndexOutOfBoundsInt(): Unit = {
    // assumeTrue("Assuming compliant ArrayIndexOutOfBounds",
    //     hasCompliantArrayIndexOutOfBounds)

    val src = Array(0, 1, 2, 3, 4, 5, 6, 0, 0, 0)
    val dest = Array(11, 12, 13, 15, 15, 16)
    val original = Array(11, 12, 13, 15, 15, 16)

    assertThrowsAIOOBE(arraycopy(src, -1, dest, 3, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 8, dest, 3, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, -1, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, 4, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 11, dest, 3, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, 13, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, 3, Int.MaxValue))
    assertArrayEquals(original, dest)
  }

  def arraycopyIndexOutOfBoundsBoolean(): Unit = {
    // assumeTrue("Assuming compliant ArrayIndexOutOfBounds",
    //     hasCompliantArrayIndexOutOfBounds)

    val src = Array(false, true, false, true, false, true, false, false, false, false)
    val dest = Array(true, true, true, true, true, true)
    val original = Array(true, true, true, true, true, true)

    assertThrowsAIOOBE(arraycopy(src, -1, dest, 3, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 8, dest, 3, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, -1, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, 4, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 11, dest, 3, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, 13, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, 3, Int.MaxValue))
    assertArrayEquals(original, dest)
  }

  def arraycopyIndexOutOfBoundsObject(): Unit = {
    // assumeTrue("Assuming compliant ArrayIndexOutOfBounds",
    //     hasCompliantArrayIndexOutOfBounds)

    val src = Array[AnyRef](O(0), O(1), O(2), O(3), O(4), O(5), O(6), O(0), O(0), O(0))
    val dest = Array[AnyRef](O(11), O(12), O(13), O(15), O(15), O(16))
    val original = Array[AnyRef](O(11), O(12), O(13), O(15), O(15), O(16))

    assertThrowsAIOOBE(arraycopy(src, -1, dest, 3, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 8, dest, 3, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, -1, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, 4, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 11, dest, 3, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, 13, 4))
    assertArrayEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, 3, Int.MaxValue))
    assertArrayEquals(original, dest)
  }

  def arraycopyIndexOutOfBoundsString(): Unit = {
    // assumeTrue("Assuming compliant ArrayIndexOutOfBounds",
    //     hasCompliantArrayIndexOutOfBounds)

    val src = Array("0", "1", "2", "3", "4", "5", "6", "0", "0", "0")
    val dest = Array("11", "12", "13", "15", "15", "16")
    val original = Array("11", "12", "13", "15", "15", "16")

    assertThrowsAIOOBE(arraycopy(src, -1, dest, 3, 4))
    assertArrayRefEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 8, dest, 3, 4))
    assertArrayRefEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, -1, 4))
    assertArrayRefEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, 4, 4))
    assertArrayRefEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 11, dest, 3, 4))
    assertArrayRefEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, 13, 4))
    assertArrayRefEquals(original, dest)

    assertThrowsAIOOBE(arraycopy(src, 1, dest, 3, Int.MaxValue))
    assertArrayRefEquals(original, dest)
  }

  def earlyArrayStoreException(): Unit = {
    // assumeTrue("Assuming compliant ArrayStores", hasCompliantArrayStores)

    val ints = Array(1, 2, 3, 4, 5)
    val bools = Array(true, false, true, false)
    val objs = Array[AnyRef](O(1), O(2), O(3), O(4), O(5))
    val strs = Array("0", "1", "2", "3", "4")

    val allArrays: List[AnyRef] = List(ints, bools, objs, strs)
    val notAnArray: AnyRef = Some("not an array")
    val undefined: AnyRef = ().asInstanceOf[AnyRef]

    /* Copying to/from notAnArray or undefined
     * (undefined has dedicated code paths because `undefined.$classData`
     * throws, unlike any other non-null value).
     */

    for (a <- notAnArray :: undefined :: allArrays) {
      assertThrowsASE(arraycopy(notAnArray, 1, a, 1, 2))
      assertThrowsASE(arraycopy(notAnArray, 1, a, 1, 0))

      assertThrowsASE(arraycopy(undefined, 1, a, 1, 2))
      assertThrowsASE(arraycopy(undefined, 1, a, 1, 0))

      assertThrowsASE(arraycopy(a, 1, notAnArray, 1, 2))
      assertThrowsASE(arraycopy(a, 1, notAnArray, 1, 0))

      assertThrowsASE(arraycopy(a, 1, undefined, 1, 2))
      assertThrowsASE(arraycopy(a, 1, undefined, 1, 0))
    }

    // Also test a few cases where the optimizer sees through everything
    assertThrowsASE(arraycopy(notAnArray, 1, objs, 1, 2))
    assertThrowsASE(arraycopy(objs, 1, notAnArray, 1, 2))

    // Copying between different primitive array types, or between primitive and ref array types

    for (len <- List(2, 0, -1)) {
      assertThrowsASE(arraycopy(ints, 1, bools, 1, len))
      assertThrowsASE(arraycopy(ints, 1, objs, 1, len))
      assertThrowsASE(arraycopy(ints, 1, strs, 1, len))

      assertThrowsASE(arraycopy(bools, 1, ints, 1, len))
      assertThrowsASE(arraycopy(bools, 1, objs, 1, len))
      assertThrowsASE(arraycopy(bools, 1, strs, 1, len))

      assertThrowsASE(arraycopy(objs, 1, ints, 1, len))
      assertThrowsASE(arraycopy(objs, 1, bools, 1, len))

      assertThrowsASE(arraycopy(strs, 1, ints, 1, len))
      assertThrowsASE(arraycopy(strs, 1, bools, 1, len))
    }

    // Also test a few cases where the optimizer sees through everything
    assertThrowsASE(arraycopy(ints, 1, bools, 1, 2))
    assertThrowsASE(arraycopy(ints, 1, objs, 1, 2))
    assertThrowsASE(arraycopy(objs, 1, ints, 1, 2))
  }

  def lateArrayStoreException(): Unit = {
    // assumeTrue("Assuming compliant ArrayStores", hasCompliantArrayStores)

    // From Array[Object] to Array[O]

    val src1: Array[AnyRef] = Array[AnyRef](O(1), O(2), "3", O(4), "5", O(6))
    val dest1: Array[O] = Array[O](O(-1), O(-2), O(-3), O(-4), O(-5), O(-6), O(-7), O(-8))
    assertThrowsASE(arraycopy(src1, 0, dest1, 0, 6))
    assertArrayRefEquals(Array[O](O(1), O(2), O(-3), O(-4), O(-5), O(-6), O(-7), O(-8)), dest1)

    val src2 = src1
    val dest2: Array[O] = Array[O](O(-1), O(-2), O(-3), O(-4), O(-5), O(-6), O(-7), O(-8))
    assertThrowsASE(arraycopy(src2, 1, dest2, 3, 3))
    assertArrayRefEquals(Array[O](O(-1), O(-2), O(-3), O(2), O(-5), O(-6), O(-7), O(-8)), dest2)

    arraycopy(src2, 2, dest2, 0, 0)
    assertArrayRefEquals(Array[O](O(-1), O(-2), O(-3), O(2), O(-5), O(-6), O(-7), O(-8)), dest2)
    arraycopy(src2, 0, dest2, 4, 2)
    assertArrayRefEquals(Array[O](O(-1), O(-2), O(-3), O(2), O(1), O(2), O(-7), O(-8)), dest2)

    // From Array[SuperClass] to Array[O]

    val src3: Array[AnyRef] = covariantUpcast(Array[SuperClass](O(1), O(2), P(3), O(4), P(5), O(6)))
    val dest3: Array[O] = Array[O](O(-1), O(-2), O(-3), O(-4), O(-5), O(-6), O(-7), O(-8))
    assertThrowsASE(arraycopy(src3, 0, dest3, 0, 6))
    assertArrayRefEquals(Array[O](O(1), O(2), O(-3), O(-4), O(-5), O(-6), O(-7), O(-8)), dest3)

    val src4 = src3
    val dest4: Array[O] = Array[O](O(-1), O(-2), O(-3), O(-4), O(-5), O(-6), O(-7), O(-8))
    assertThrowsASE(arraycopy(src4, 1, dest4, 3, 3))
    assertArrayRefEquals(Array[O](O(-1), O(-2), O(-3), O(2), O(-5), O(-6), O(-7), O(-8)), dest4)

    arraycopy(src4, 2, dest4, 0, 0)
    assertArrayRefEquals(Array[O](O(-1), O(-2), O(-3), O(2), O(-5), O(-6), O(-7), O(-8)), dest4)
    arraycopy(src4, 0, dest4, 4, 2)
    assertArrayRefEquals(Array[O](O(-1), O(-2), O(-3), O(2), O(1), O(2), O(-7), O(-8)), dest4)

    // From Array[P] to Array[O], succeeds with 0 elements to copy

    val src5: Array[AnyRef] = covariantUpcast(Array[P](P(1), P(2), P(3)))
    val dest5: Array[O] = Array[O](O(-1), O(-2), O(-3), O(-4), O(-5))
    arraycopy(src5, 2, dest5, 1, 0)
    assertArrayRefEquals(Array[O](O(-1), O(-2), O(-3), O(-4), O(-5)), dest5)
  }
}

object SystemArraycopyTest {
  abstract class SuperClass

  private final case class O(x: Int) extends SuperClass

  private final case class P(x: Int) extends SuperClass
}
