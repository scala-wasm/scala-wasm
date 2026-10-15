package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._
import scala.scalajs.wit.unsigned._

import componentmodel.component.testing.tests._

object TestsImpl {

  private def roundtrip[A](a: A): A = a

  private def ignore[A](a: A): Unit = ()

  /** Same format as `many_params` in rust-exports. */
  def formatManyParams(a: UByte, b: Byte, c: UShort, d: Short, e: UInt, f: Int, g: ULong,
      h: Long, i: Float, j: Double, k: Char, l: String, m: Point, n: wit.Option[String],
      o: Array[UShort], p: C1): String = {
    val pStr = p match {
      case C1.A(x) => "a" + x
      case C1.B(x) => "b" + x.toLong
    }
    List(
      java.lang.Byte.toUnsignedInt(a).toString(),
      b.toString(),
      java.lang.Short.toUnsignedInt(c).toString(),
      d.toString(),
      Integer.toUnsignedString(e),
      f.toString(),
      java.lang.Long.toUnsignedString(g),
      h.toString(),
      i.toLong.toString(),
      j.toLong.toString(),
      k.toString(),
      l,
      s"${m.x}:${m.y}",
      n.getOrElse("none"),
      o.map(java.lang.Short.toUnsignedInt(_)).mkString(":"),
      pStr
    ).mkString(",")
  }

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

  @WitExport(WitScope.unversioned("component", "testing", "tests"), "many-params")
  def manyParams(@WitName("a") a: UByte, @WitName("b") b: Byte, @WitName("c") c: UShort,
      @WitName("d") d: Short, @WitName("e") e: UInt, @WitName("f") f: Int,
      @WitName("g") g: ULong, @WitName("h") h: Long, @WitName("i") i: Float,
      @WitName("j") j: Double, @WitName("k") k: Char, @WitName("l") l: String,
      @WitName("m") m: Point, @WitName("n") n: wit.Option[String],
      @WitName("o") o: Array[UShort], @WitName("p") p: C1): String = {
    formatManyParams(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p)
  }

}
