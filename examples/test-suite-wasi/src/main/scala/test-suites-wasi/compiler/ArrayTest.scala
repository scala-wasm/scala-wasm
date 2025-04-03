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

import testSuiteWASI.utils.AssertThrows.assertThrows
import testSuiteWASI.junit.Assert._
import testSuiteWASI.Platform._

class ArrayTest {

  private def covariantUpcast[A <: AnyRef](array: Array[_ <: A]): Array[A] =
    array.asInstanceOf[Array[A]]

  @noinline
  private def nullOf[T >: Null]: T = null

  @inline
  private def inlineNullOf[T >: Null]: T = null

  @noinline
  private def throwIllegalStateAsInt(): Int =
    throw new IllegalStateException()

  @inline
  private def throwIllegalStateAsIntInline(): Int =
    throw new IllegalStateException()


  def getArrayIndexOutOfBounds(): Unit = {
    // assumeTrue("Assuming compliant ArrayIndexOutOfBounds",
    //     hasCompliantArrayIndexOutOfBounds)

    val a = new Array[Int](5)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], a(-1))
    assertThrows(classOf[ArrayIndexOutOfBoundsException], a(5))
    assertThrows(classOf[ArrayIndexOutOfBoundsException], a(Int.MinValue))
    assertThrows(classOf[ArrayIndexOutOfBoundsException], a(Int.MaxValue))

    val b = new Array[AnyRef](5)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], b(-1))
    assertThrows(classOf[ArrayIndexOutOfBoundsException], b(5))
    assertThrows(classOf[ArrayIndexOutOfBoundsException], b(Int.MinValue))
    assertThrows(classOf[ArrayIndexOutOfBoundsException], b(Int.MaxValue))

    val c = new Array[Seq[_]](5)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], c(-1))
    assertThrows(classOf[ArrayIndexOutOfBoundsException], c(5))
    assertThrows(classOf[ArrayIndexOutOfBoundsException], c(Int.MinValue))
    assertThrows(classOf[ArrayIndexOutOfBoundsException], c(Int.MaxValue))
  }


  def setArrayIndexOutOfBounds(): Unit = {
    // assumeTrue("Assuming compliant ArrayIndexOutOfBounds",
    //     hasCompliantArrayIndexOutOfBounds)

    val a = new Array[Int](5)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], a(-1) = 1)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], a(5) = 1)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], a(Int.MinValue) = 1)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], a(Int.MaxValue) = 1)

    val b = new Array[AnyRef](5)
    val obj = new AnyRef
    assertThrows(classOf[ArrayIndexOutOfBoundsException], b(-1) = obj)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], b(5) = obj)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], b(Int.MinValue) = obj)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], b(Int.MaxValue) = obj)

    val c = new Array[Seq[_]](5)
    val seq = List(1, 2)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], c(-1) = seq)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], c(5) = seq)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], c(Int.MinValue) = seq)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], c(Int.MaxValue) = seq)

    /* IndexOutOfBoundsException is stronger than ArrayStoreException
     * (whether the latter is compliant or not).
     */
    val d: Array[AnyRef] = covariantUpcast(c)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], d(-1) = obj)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], d(5) = obj)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], d(Int.MinValue) = obj)
    assertThrows(classOf[ArrayIndexOutOfBoundsException], d(Int.MaxValue) = obj)
  }


  def setArrayStoreExceptions(): Unit = {
    // assumeTrue("Assuming compliant ArrayStores",
    //     hasCompliantArrayStores)

    val obj = new AnyRef
    val str = "foo"
    val list = List(1, 2)
    val vector = Vector(3, 4)

    val a: Array[AnyRef] = covariantUpcast(new Array[Seq[_]](5))
    a(1) = list
    assertSame(list, a(1))
    assertThrows(classOf[ArrayStoreException], a(1) = obj)
    assertSame(list, a(1))
    assertThrows(classOf[ArrayStoreException], a(2) = str)
    assertNull(a(2))
    a(3) = vector
    assertSame(vector, a(3))
    a(1) = null
    assertNull(a(1))

    val b: Array[Seq[_]] = covariantUpcast(new Array[List[Any]](5))
    b(1) = list
    assertSame(list, b(1))
    assertThrows(classOf[ArrayStoreException], b(1) = vector)
    assertSame(list, b(1))

    val c: Array[Number] = covariantUpcast(new Array[Integer](5))
    c(1) = Integer.valueOf(5)
    assertEquals(5, c(1))
    assertThrows(classOf[ArrayStoreException], c(1) = java.lang.Double.valueOf(5.5))
    assertEquals(5, c(1))
    /*
    if (executingInJVM) {
      assertThrows(classOf[ArrayStoreException], c(2) = java.lang.Double.valueOf(5.0))
      assertNull(c(2))
    } else {
      c(2) = java.lang.Double.valueOf(5.0)
      assertEquals(5.0, c(2))
    }
    val c2: Array[Object] = covariantUpcast(c)
    c2(3) = Integer.valueOf(42)
    assertThrows(classOf[ArrayStoreException], c2(3) = str)
    assertEquals(42, c2(3))
    assertEquals(42, c(3))

    val x: Array[AnyRef] = covariantUpcast(new Array[Array[Seq[_]]](5))
    x(1) = new Array[Seq[_]](1)
    x(2) = new Array[List[Any]](1)
    assertThrows(classOf[ArrayStoreException], x(3) = new Array[String](1))
    assertThrows(classOf[ArrayStoreException], x(3) = new Array[AnyRef](1))
    assertThrows(classOf[ArrayStoreException], x(3) = new Array[Int](1))
    assertThrows(classOf[ArrayStoreException], x(3) = obj)
    assertThrows(classOf[ArrayStoreException], x(3) = str)
    x(1) = null
    assertNull(x(1))

    val y: Array[AnyRef] = covariantUpcast(new Array[Array[Int]](5))
    y(1) = new Array[Int](1)
    assertThrows(classOf[ArrayStoreException], y(3) = new Array[String](1))
    assertThrows(classOf[ArrayStoreException], y(3) = new Array[AnyRef](1))
    assertThrows(classOf[ArrayStoreException], y(3) = new Array[List[Any]](1))
    assertThrows(classOf[ArrayStoreException], y(3) = obj)
    assertThrows(classOf[ArrayStoreException], y(3) = str)
    y(1) = null
    assertNull(y(1))
    */
  }


  def arraySelectSideEffecting_Issue3848(): Unit = {
    // assumeTrue("Assuming compliant ArrayIndexOutOfBounds",
    //     hasCompliantArrayIndexOutOfBounds)

    // Force unit return type so the Emitter tries to get rid of the expression.
    @noinline
    def testAccess(a: Array[Int]): Unit = a(1)

    assertThrows(classOf[ArrayIndexOutOfBoundsException], testAccess(Array()))
  }

   def negativeArraySizes(): Unit = {
    // assumeTrue("Assuming compliant negative array sizes", hasCompliantNegativeArraySizes)

    @noinline def getNegValue(): Int = -5
    val negValue = getNegValue()

    assertThrows(classOf[NegativeArraySizeException], new Array[Int](-5))
    assertThrows(classOf[NegativeArraySizeException], new Array[Int](negValue))

    assertThrows(classOf[NegativeArraySizeException], new Array[Boolean](-5))
    assertThrows(classOf[NegativeArraySizeException], new Array[Boolean](negValue))

    assertThrows(classOf[NegativeArraySizeException], new Array[AnyRef](-5))
    assertThrows(classOf[NegativeArraySizeException], new Array[AnyRef](negValue))

    assertThrows(classOf[NegativeArraySizeException], new Array[String](-5))
    assertThrows(classOf[NegativeArraySizeException], new Array[String](negValue))

    assertThrows(classOf[NegativeArraySizeException], new Array[Array[AnyRef]](-5))
    assertThrows(classOf[NegativeArraySizeException], new Array[Array[AnyRef]](negValue))

    assertThrows(classOf[NegativeArraySizeException], Array.ofDim[Int](-5, 5))
    assertThrows(classOf[NegativeArraySizeException], Array.ofDim[Int](negValue, 5))
    assertThrows(classOf[NegativeArraySizeException], Array.ofDim[Int](5, -5))
    assertThrows(classOf[NegativeArraySizeException], Array.ofDim[Int](5, negValue))

    // Force unit result type to tempt the optimizer and emitter into getting rid of the expression
    @noinline
    def testCreateNegativeSizeArray(): Unit = new Array[Int](-1)

    assertThrows(classOf[NegativeArraySizeException], testCreateNegativeSizeArray())
  }

  def genericArrayNullsShortCircuited_Issue4755(): Unit = {
    // Tests for the intrinsics for ScalaRunTime.array_{apply,update,select}.

    @inline def testGeneric[T](array: Array[T], value: T): Unit = {
      assertThrows(classOf[IllegalStateException], array(throwIllegalStateAsInt()))
      assertThrows(classOf[IllegalStateException], array(throwIllegalStateAsIntInline()))

      assertThrows(classOf[IllegalStateException], array(throwIllegalStateAsInt()) = value)
      assertThrows(classOf[IllegalStateException], array(throwIllegalStateAsIntInline()) = value)

      assertThrows(classOf[IllegalStateException], array(1) = (throw new IllegalStateException()))
    }

    @noinline def testNoInline[T](array: Array[T], value: T): Unit = {
      testGeneric(array, value)
    }

    @inline def test[T](array: Array[T], value: T): Unit = {
      testNoInline(array, value)
      testGeneric(array, value)
    }

    /* Explicitly store the result of `nullOf` in local vals typed in right
     * way, to force scalac's erasure to insert a cast and to retain that type.
     * Otherwise, after erasure, `nullOf` returns an `Object`, and `test` takes
     * an `Object`, so the `Array[X]` type never appears, and the optimization
     * that we want to test does not get triggered at all.
     * In other words, if we "inline" those `val`s, the test becomes moot.
     *
     * For `inlineNullOf`, it makes no difference since it becomes a constant
     * `null` anyway.
     */
    val nullArrayRef: Array[AnyRef] = nullOf[Array[AnyRef]]
    val nullArrayInt: Array[Int] = nullOf[Array[Int]]

    test(nullArrayRef, List(1))
    test(inlineNullOf[Array[AnyRef]], List(1))

    test(nullArrayInt, 1)
    test(inlineNullOf[Array[Int]], 1)
  }
}
