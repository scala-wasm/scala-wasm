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

  def roundtripOption(a: cm.Option[String]): cm.Option[String] = a
  def roundtripDoubleOption(a: cm.Option[cm.Option[String]]): cm.Option[cm.Option[String]] = a

  def roundtripResult(a: cm.Result[Unit, Unit]): cm.Result[Unit, Unit] = a
  def roundtripStringError(a: cm.Result[Float, String]): cm.Result[Float, String] = a
  def roundtripEnumError(a: cm.Result[C1, E1]): cm.Result[C1, E1] = a
}

@ComponentExport("component:testing/test-imports")
object TestImports extends cm.Interface {
  def run(): Unit = {
    import Basics._
    import Tests._
    assert(1 == roundtripU8(1))
    assert(0 == roundtripS8(0))
    assert(0 == roundtripU16(0))
    assert(128 == roundtripS16(128))
    assert(0 == roundtripU32(0))
    assert(30 == roundtripS32(30))
    // assert(30 == roundtripU64(30))
    // assert(30 == roundtripS64(30))

    assert(0.0f == roundtripF32(0.0f))
    assert(0.0 == roundtripF64(0.0))

    assert('a' == roundtripChar('a'))

    assert("foo" == roundtripString("foo"))
    assert("" == roundtripString(""))
    assert(Point(0, 5) == roundtripPoint(Point(0, 5)))

    testC1(C1.A(5))
    assert(C1.A(4) == roundtripC1(C1.A(4)))
    assert(C1.B(0.0f) == roundtripC1(C1.B(0.0f)))
    assert(Z1.A(0) == roundtripZ1(Z1.A(0)))
    assert(Z1.A(100) == roundtripZ1(Z1.A(100)))
    assert(Z1.B == roundtripZ1(Z1.B))

    assert(E1.A == roundtripEnum(E1.A))
    assert(E1.B == roundtripEnum(E1.B))
    assert(E1.C == roundtripEnum(E1.C))

    assert((C1.A(5), Z1.A(500)) == roundtripTuple((C1.A(5), Z1.A(500))))
    assert((C1.B(200.0f), Z1.B) == roundtripTuple((C1.B(200.0f), Z1.B)))
    assert(C1.A(4) == roundtripTuple(C1.A(4), Z1.B)._1)
    assert(Z1.B == roundtripTuple(C1.A(4), Z1.B)._2)

    assert(cm.Some("ok") == roundtripOption(cm.Some("ok")))
    assert(cm.None == roundtripOption(cm.None))
    assert(cm.Some(cm.Some("foo")) == roundtripDoubleOption(cm.Some(cm.Some("foo"))))
    assert(cm.Some(cm.None) == roundtripDoubleOption(cm.Some(cm.None)))
    assert(cm.None == roundtripDoubleOption(cm.None))
    assert(cm.Err("aaa") != cm.Err("bbb"))

    assert(cm.Ok(()) == roundtripResult(cm.Ok(())))
    assert(cm.Err(()) == roundtripResult(cm.Err(())))
    // assert(cm.Ok(3.0f) == roundtripStringError(cm.Ok(3.0f)))
    assert(cm.Err("err") == roundtripStringError(cm.Err("err")))
    assert(cm.Ok(C1.A(432)) == roundtripEnumError(cm.Ok(C1.A(432))))
    assert(cm.Ok(C1.B(0.0f)) == roundtripEnumError(cm.Ok(C1.B(0.0f))))
    assert(cm.Err(E1.A) == roundtripEnumError(cm.Err(E1.A)))
    assert(cm.Err(E1.B) == roundtripEnumError(cm.Err(E1.B)))
    assert(cm.Err(E1.C) == roundtripEnumError(cm.Err(E1.C)))
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