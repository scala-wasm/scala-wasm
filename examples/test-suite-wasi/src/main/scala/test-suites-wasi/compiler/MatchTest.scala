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

import scala.annotation.switch

import org.junit.Assert._

class MatchTest {
  import MatchTest._

  def switchWithGuardsStat(): Unit = {
    def test(x: Int, y: Int): String = {
      var result = ""
      (x: @switch) match {
        case 1            => result = "one"
        case 2 if y < 10  => result = "two special"
        case 2            => result = "two"
        case 3 if y < 10  => result = "three special"
        case 3 if y > 100 => result = "three big special"
        case z if y > 100 => result = "big " + z
        case _            => result = "None of those"
      }
      result
    }

    assertEquals("one", test(1, 0))
    assertEquals("two special", test(2, 0))
    assertEquals("two", test(2, 50))
    assertEquals("three special", test(3, 5))
    assertEquals("three big special", test(3, 200))
    assertEquals("None of those", test(3, 50))
    assertEquals("big 5", test(5, 300))
    assertEquals("None of those", test(5, 20))
  }

  def switchWithGuardsExpr(): Unit = {
    def test(x: Int, y: Int): String = {
      (x: @switch) match {
        case 1            => "one"
        case 2 if y < 10  => "two special"
        case 2            => "two"
        case 3 if y < 10  => "three special"
        case 3 if y > 100 => "three big special"
        case z if y > 100 => "big " + z
        case _            => "None of those"
      }
    }

    assertEquals("one", test(1, 0))
    assertEquals("two special", test(2, 0))
    assertEquals("two", test(2, 50))
    assertEquals("three special", test(3, 5))
    assertEquals("three big special", test(3, 200))
    assertEquals("None of those", test(3, 50))
    assertEquals("big 5", test(5, 300))
    assertEquals("None of those", test(5, 20))
  }

  // #2554
  def matchWithNonIdentityMatchEndScalaLib(): Unit = {
    val foo: Option[Int] = Some(42)

    /* This match generates a value class boxing operation in the matchEnd (in
     * 2.11).
     */
    val result =
      "foo = " ++ (foo match { case Some(0) => "zero" case _ => "unknown" })

    assertEquals("foo = unknown", result)
  }


  // #2554
  def matchWithNonIdentityMatchEndIndependent(): Unit = {
    import scala.language.implicitConversions

    implicit def toValueClass(x: Int): ValueClass = new ValueClass(x)
    def show[T](x: ValueClassBase[T]): String = x.f().toString

    val foo: Option[Int] = Some(42)
    assertEquals("4", show(foo match { case Some(0) => 1 case _ => 2 }))
  }

}

object MatchTest {
  trait ValueClassBase[T] extends Any {
    def f(): T
  }

  class ValueClass(private val x: Int) extends AnyVal with ValueClassBase[Int] {
    def f(): Int = x * 2
  }
}
