package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._
import scala.scalajs.wit.unsigned._

import componentmodel.component.testing.tests._

import scala.scalajs.WitUtils.toEither

import java.util.Optional

object TestsImpl {

  private def roundtrip[A](a: A): A = a

  private def ignore[A](a: A): Unit = ()

  @WitExport("component:testing/tests", "roundtrip-point")
  def roundtripPoint(a: Point): Point = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-basics1")
  def roundtripBasics1(a: wit.Tuple9[UByte, Byte, UShort, Short, UInt, Int, Float, Double,
          Char]): wit.Tuple9[UByte, Byte, UShort, Short, UInt, Int, Float, Double, Char] = {
    roundtrip(a)
  }

  @WitExport("component:testing/tests", "roundtrip-list-u16")
  def roundtripListU16(a: Array[UShort]): Array[UShort] = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-list-point")
  def roundtripListPoint(a: Array[Point]): Array[Point] = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-list-variant")
  def roundtripListVariant(a: Array[C1]): Array[C1] = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-string")
  def roundtripString(a: String): String = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-c1")
  def roundtripC1(a: C1): C1 = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-z1")
  def roundtripZ1(a: Z1): Z1 = roundtrip(a)

  @WitExport("component:testing/tests", "test-c1")
  def testC1(a: C1): Unit = ignore(a)

  @WitExport("component:testing/tests", "roundtrip-enum")
  def roundtripEnum(a: E1): E1 = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-tuple")
  def roundtripTuple(a: wit.Tuple2[C1, Z1]): wit.Tuple2[C1, Z1] = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-option")
  def roundtripOption(a: Optional[String]): Optional[String] = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-double-option")
  def roundtripDoubleOption(a: Optional[Optional[String]]): Optional[Optional[String]] =
    roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-result")
  def roundtripResult(a: wit.Result[Unit, Unit]): wit.Result[Unit, Unit] = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-string-error")
  def roundtripStringError(a: wit.Result[Float, String]): wit.Result[Float, String] =
    roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-enum-error")
  def roundtripEnumError(a: wit.Result[C1, E1]): wit.Result[C1, E1] = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-f1")
  def roundtripF1(a: F1): F1 = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-f2")
  def roundtripF2(a: F2): F2 = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-f3")
  def roundtripF3(a: F3): F3 = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-flags")
  def roundtripFlags(a: wit.Tuple2[F1, F1]): wit.Tuple2[F1, F1] = roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-tuple2")
  def roundtripTuple2(a: wit.Tuple2[Int, String]): wit.Tuple2[Int, String] =
    roundtrip(a)

  @WitExport("component:testing/tests", "roundtrip-tuple3")
  def roundtripTuple3(
      a: wit.Tuple3[Int, String, Boolean]): wit.Tuple3[Int, String, Boolean] = {
    roundtrip(a)
  }

}
