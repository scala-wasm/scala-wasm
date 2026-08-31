package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._
import scala.scalajs.wit.unsigned._

import componentmodel.component.testing.tests._

object TestsImpl {

  private def roundtrip[A](a: A): A = a

  private def ignore[A](a: A): Unit = ()

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-point")
  def roundtripPoint(@WitName("a") a: Point): Point = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-basics1")
  def roundtripBasics1(
      @WitName("a") a: wit.Tuple9[UByte, Byte, UShort, Short, UInt, Int, Float, Double,
          Char]): wit.Tuple9[UByte, Byte, UShort, Short, UInt, Int, Float, Double, Char] = {
    roundtrip(a)
  }

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-list-u16")
  def roundtripListU16(@WitName("a") a: Array[UShort]): Array[UShort] = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-list-point")
  def roundtripListPoint(@WitName("a") a: Array[Point]): Array[Point] = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-list-variant")
  def roundtripListVariant(@WitName("a") a: Array[C1]): Array[C1] = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-string")
  def roundtripString(@WitName("a") a: String): String = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-c1")
  def roundtripC1(@WitName("a") a: C1): C1 = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-z1")
  def roundtripZ1(@WitName("a") a: Z1): Z1 = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "test-c1")
  def testC1(@WitName("a") a: C1): Unit = ignore(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-enum")
  def roundtripEnum(@WitName("a") a: E1): E1 = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-tuple")
  def roundtripTuple(@WitName("a") a: wit.Tuple2[C1, Z1]): wit.Tuple2[C1, Z1] = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-option")
  def roundtripOption(@WitName("a") a: wit.Option[String]): wit.Option[String] = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-double-option")
  def roundtripDoubleOption(
      @WitName("a") a: wit.Option[wit.Option[String]]): wit.Option[wit.Option[String]] = {
    roundtrip(a)
  }

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-result")
  def roundtripResult(@WitName("a") a: wit.Result[Unit, Unit]): wit.Result[Unit, Unit] = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-string-error")
  def roundtripStringError(@WitName("a") a: wit.Result[Float, String]): wit.Result[Float, String] =
    roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-enum-error")
  def roundtripEnumError(@WitName("a") a: wit.Result[C1, E1]): wit.Result[C1, E1] = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-f1")
  def roundtripF1(@WitName("a") a: F1): F1 = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-f2")
  def roundtripF2(@WitName("a") a: F2): F2 = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-f3")
  def roundtripF3(@WitName("a") a: F3): F3 = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-flags")
  def roundtripFlags(@WitName("a") a: wit.Tuple2[F1, F1]): wit.Tuple2[F1, F1] = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-tuple2")
  def roundtripTuple2(@WitName("a") a: wit.Tuple2[Int, String]): wit.Tuple2[Int, String] =
    roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-tuple3")
  def roundtripTuple3(
      @WitName("a") a: wit.Tuple3[Int, String, Boolean]): wit.Tuple3[Int, String, Boolean] = {
    roundtrip(a)
  }

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-named-r")
  def roundtripNamedR(@WitName("a") a: NamedR): NamedR = roundtrip(a)

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-option-u32")
  def roundtripOptionU32(@WitName("a") a: OptionU32): OptionU32 = roundtrip(a)

}
