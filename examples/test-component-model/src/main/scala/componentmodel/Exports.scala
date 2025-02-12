package componentmodel

import scala.scalajs.js
import scala.scalajs.{component => cm}
import scala.scalajs.component._
import cm.annotation._
import cm.unsigned._

@ComponentExport("component:testing/tests")
object TestsExport extends cm.Interface {
  def roundtripString(a: String): String = a
  def roundtripPoint(a: Point): Point = a

  def roundtripC1(a: C1): C1 = a
  def roundtripZ1(a: Z1): Z1 = a
  def testC1(a: C1): Unit = {}

  def roundtripEnum(a: E1): E1 = a
  def roundtripTuple(a: (C1, Z1)): (C1, Z1) = a

  def roundtripResult(a: cm.Result[Unit, Unit]): Result[Unit, Unit] = a
  def roundtripStringError(a: cm.Result[Float, String]): cm.Result[Float, String] = a
  def roundtripEnumError(a: cm.Result[C1, E1]): cm.Result[C1, E1] = a
}

@ComponentExport("component:testing/test-imports")
object TestImports extends cm.Interface {
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

    assert(roundtripEnum(E1.A) == E1.A)
    assert(roundtripEnum(E1.B) == E1.B)
    assert(roundtripEnum(E1.C) == E1.C)

    assert(roundtripTuple((C1.A(5), Z1.A(500))) == (C1.A(5), Z1.A(500)))
    assert(roundtripTuple((C1.B(200.0f), Z1.B)) == (C1.B(200.0f), Z1.B))
    assert(roundtripTuple(C1.A(4), Z1.B)._1 == C1.A(4))
    assert(roundtripTuple(C1.A(4), Z1.B)._2 == Z1.B)

    assert(cm.Err("aaa") != cm.Err("bbb"))
    assert(roundtripResult(cm.Ok(())) == cm.Ok(()))
    assert(roundtripResult(cm.Err(())) == cm.Err(()))
    assert(roundtripStringError(cm.Ok(3.0f)).value == 3.0f)
    assert(roundtripStringError(cm.Err("err")) == cm.Err("err"))
    assert(roundtripEnumError(cm.Ok(C1.A(432))) == cm.Ok(C1.A(432)))
    assert(roundtripEnumError(cm.Ok(C1.B(0.0f))) == cm.Ok(C1.B(0.0f)))
    assert(roundtripEnumError(cm.Err(E1.A)) == cm.Err(E1.A))
    assert(roundtripEnumError(cm.Err(E1.B)) == cm.Err(E1.B))
    assert(roundtripEnumError(cm.Err(E1.C)) == cm.Err(E1.C))
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

sealed trait E1 extends cm.Enum
object E1 {
  final case object A extends E1 {
    val _index = 0
  }
  final case object B extends E1 {
    val _index = 1
  }
  final case object C extends E1 {
    val _index = 2
  }
}