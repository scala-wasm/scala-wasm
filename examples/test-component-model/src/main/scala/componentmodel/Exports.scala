package componentmodel

import scala.scalajs.js
import scala.scalajs.component
import scala.scalajs.component._
import component.annotation._
import component.unsigned._

@ComponentExport("component:testing/tests")
object TestsExport extends component.Interface {
  def roundtripString(a: String): String = a
  def roundtripPoint(a: Point): Point = a

  def roundtripC1(a: C1): C1 = { a }
  def roundtripZ1(a: Z1): Z1 = a
  def testC1(a: C1): Unit = {}
}

@ComponentExport("component:testing/test-imports")
object TestImports extends component.Interface {
  def run(): Unit = {
    import Basics._
    import Tests._

    assert(roundtripU8(1) == 1)
    assert(roundtripS8(0) == 0)
    assert(roundtripU16(0) == 0)
    assert(roundtripS16(128) == 128)
    assert(roundtripU32(0) == 0)
    assert(roundtripS32(30) == 30)
    // assert(roundtripU64(30) == 30)
    // assert(roundtripS64(30) == 30)

    assert(roundtripF32(0.0f) == 0.0f)
    assert(roundtripF64(0.0) == 0.0)

    assert(roundtripChar('a') == 'a')

    assert(roundtripString("foo") == "foo")
    assert(roundtripString("") == "")
    assert(roundtripPoint(Point(0, 5)) == Point(0, 5))

    testC1(C1.A(5))
    assert(roundtripC1(C1.A(4)) == C1.A(4))
    assert(roundtripC1(C1.B(0.0f)) == C1.B(0.0f))
    assert(roundtripZ1(Z1.A(0)) == Z1.A(0))
    assert(roundtripZ1(Z1.A(100)) == Z1.A(100))
    assert(roundtripZ1(Z1.B) == Z1.B)
  }
}

@ComponentRecord
final case class Point(x: Int, y: Int)

sealed trait C1 extends Variant
object C1 {
  final case class A(value: Int) extends C1 {
    type T = Int
    val _index = 0
  }
  final case class B(value: Float) extends C1 {
    type T = Float
    val _index = 1
  }
}

sealed trait Z1 extends Variant
object Z1 {
  final case class A(value: Int) extends Z1 {
    type T = Int
    val _index = 0
  }
  // if the field is typed Unit, there's no fields generated in SJSIR
  // there's only a getter
  final case object B extends Z1 {
    type T = Unit
    val value = ()
    val _index = 1
  }
}