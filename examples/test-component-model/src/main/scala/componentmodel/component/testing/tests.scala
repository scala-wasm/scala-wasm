package componentmodel.component.testing

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

package object tests {

  // Type definitions
  @WitRecord(WitScope.unversioned("component", "testing", "tests"), "point")
  final case class Point(@WitName("x") x: Int, @WitName("y") y: Int)

  @WitVariant(WitScope.unversioned("component", "testing", "tests"), "c1")
  sealed trait C1

  object C1 {
    @WitName("a")
    final case class A(@WitName("value") value: Int) extends C1

    @WitName("b")
    final case class B(@WitName("value") value: Float) extends C1
  }

  @WitVariant(WitScope.unversioned("component", "testing", "tests"), "z1")
  sealed trait Z1

  object Z1 {
    @WitName("a")
    final case class A(@WitName("value") value: Int) extends Z1

    @WitName("b")
    case object B extends Z1
  }

  @WitEnum(WitScope.unversioned("component", "testing", "tests"), "e1")
  sealed trait E1

  object E1 {
    @WitName("a")
    case object A extends E1

    @WitName("b")
    case object B extends E1

    @WitName("c")
    case object C extends E1
  }

  @WitFlags(WitScope.unversioned("component", "testing", "tests"), "f1",
      Array("b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7"))
  final case class F1(value: Int) {
    def |(other: F1): F1 = F1(value | other.value)
    def &(other: F1): F1 = F1(value & other.value)
    def ^(other: F1): F1 = F1(value ^ other.value)
    def unary_~ : F1 = F1(~value)
    def contains(other: F1): Boolean = (value & other.value) == other.value
  }

  object F1 {
    val b0 = F1(1 << 0)
    val b1 = F1(1 << 1)
    val b2 = F1(1 << 2)
    val b3 = F1(1 << 3)
    val b4 = F1(1 << 4)
    val b5 = F1(1 << 5)
    val b6 = F1(1 << 6)
    val b7 = F1(1 << 7)
  }

  @WitFlags(WitScope.unversioned("component", "testing", "tests"), "f2",
      Array("b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7",
          "b8", "b9", "b10", "b11", "b12", "b13", "b14", "b15"))
  final case class F2(value: Int) {
    def |(other: F2): F2 = F2(value | other.value)
    def &(other: F2): F2 = F2(value & other.value)
    def ^(other: F2): F2 = F2(value ^ other.value)
    def unary_~ : F2 = F2(~value)
    def contains(other: F2): Boolean = (value & other.value) == other.value
  }

  object F2 {
    val b0 = F2(1 << 0)
    val b1 = F2(1 << 1)
    val b2 = F2(1 << 2)
    val b3 = F2(1 << 3)
    val b4 = F2(1 << 4)
    val b5 = F2(1 << 5)
    val b6 = F2(1 << 6)
    val b7 = F2(1 << 7)
    val b8 = F2(1 << 8)
    val b9 = F2(1 << 9)
    val b10 = F2(1 << 10)
    val b11 = F2(1 << 11)
    val b12 = F2(1 << 12)
    val b13 = F2(1 << 13)
    val b14 = F2(1 << 14)
    val b15 = F2(1 << 15)
  }

  @WitFlags(WitScope.unversioned("component", "testing", "tests"), "f3",
      Array("b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7",
          "b8", "b9", "b10", "b11", "b12", "b13", "b14", "b15",
          "b16", "b17", "b18", "b19", "b20", "b21", "b22", "b23",
          "b24", "b25", "b26", "b27", "b28", "b29", "b30", "b31"))
  final case class F3(value: Int) {
    def |(other: F3): F3 = F3(value | other.value)
    def &(other: F3): F3 = F3(value & other.value)
    def ^(other: F3): F3 = F3(value ^ other.value)
    def unary_~ : F3 = F3(~value)
    def contains(other: F3): Boolean = (value & other.value) == other.value
  }

  object F3 {
    val b0 = F3(1 << 0)
    val b1 = F3(1 << 1)
    val b2 = F3(1 << 2)
    val b3 = F3(1 << 3)
    val b4 = F3(1 << 4)
    val b5 = F3(1 << 5)
    val b6 = F3(1 << 6)
    val b7 = F3(1 << 7)
    val b8 = F3(1 << 8)
    val b9 = F3(1 << 9)
    val b10 = F3(1 << 10)
    val b11 = F3(1 << 11)
    val b12 = F3(1 << 12)
    val b13 = F3(1 << 13)
    val b14 = F3(1 << 14)
    val b15 = F3(1 << 15)
    val b16 = F3(1 << 16)
    val b17 = F3(1 << 17)
    val b18 = F3(1 << 18)
    val b19 = F3(1 << 19)
    val b20 = F3(1 << 20)
    val b21 = F3(1 << 21)
    val b22 = F3(1 << 22)
    val b23 = F3(1 << 23)
    val b24 = F3(1 << 24)
    val b25 = F3(1 << 25)
    val b26 = F3(1 << 26)
    val b27 = F3(1 << 27)
    val b28 = F3(1 << 28)
    val b29 = F3(1 << 29)
    val b30 = F3(1 << 30)
    val b31 = F3(1 << 31)
  }

  @WitFlags(WitScope.unversioned("component", "testing", "tests"), "named-f", Array("b0"))
  final case class NamedF(value: Int)

  object NamedF {
    val b0 = NamedF(1 << 0)
  }

  @WitRecord(WitScope.unversioned("component", "testing", "tests"), "named-r")
  final case class NamedR(@WitName("b") b: NamedF)

  // Functions
  /** roundtrip-basics0: func(a: tuple<u32, s32>)
   *    -> tuple<u32, s32>;
   */
  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-basics1")
  def roundtripBasics1(
      @WitName("a") a: wit.Tuple9[wit.unsigned.UByte, Byte, wit.unsigned.UShort, Short,
          wit.unsigned.UInt, Int, Float, Double, Char]): wit.Tuple9[wit.unsigned.UByte, Byte,
      wit.unsigned.UShort, Short, wit.unsigned.UInt, Int, Float, Double, Char] = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-string")
  def roundtripString(@WitName("a") a: String): String = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-point")
  def roundtripPoint(@WitName("a") a: Point): Point = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-list-u16")
  def roundtripListU16(@WitName("a") a: Array[wit.unsigned.UShort]): Array[wit.unsigned.UShort] =
    wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-list-point")
  def roundtripListPoint(@WitName("a") a: Array[Point]): Array[Point] = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-list-variant")
  def roundtripListVariant(@WitName("a") a: Array[C1]): Array[C1] = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "test-c1")
  def testC1(@WitName("a") a: C1): Unit = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-c1")
  def roundtripC1(@WitName("a") a: C1): C1 = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-z1")
  def roundtripZ1(@WitName("a") a: Z1): Z1 = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-enum")
  def roundtripEnum(@WitName("a") a: E1): E1 = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-tuple")
  def roundtripTuple(@WitName("a") a: wit.Tuple2[C1, Z1]): wit.Tuple2[C1, Z1] = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-option")
  def roundtripOption(@WitName("a") a: wit.Option[String]): wit.Option[String] =
    wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-double-option")
  def roundtripDoubleOption(
      @WitName("a") a: wit.Option[wit.Option[String]]): wit.Option[wit.Option[String]] = {
    wit.native
  }

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-f1")
  def roundtripF1(@WitName("a") a: F1): F1 = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-f2")
  def roundtripF2(@WitName("a") a: F2): F2 = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-f3")
  def roundtripF3(@WitName("a") a: F3): F3 = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-flags")
  def roundtripFlags(@WitName("a") a: wit.Tuple2[F1, F1]): wit.Tuple2[F1, F1] = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-result")
  def roundtripResult(@WitName("a") a: wit.Result[Unit, Unit]): wit.Result[Unit, Unit] = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-string-error")
  def roundtripStringError(@WitName("a") a: wit.Result[Float, String]): wit.Result[Float, String] =
    wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-enum-error")
  def roundtripEnumError(@WitName("a") a: wit.Result[C1, E1]): wit.Result[C1, E1] = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-tuple2")
  def roundtripTuple2(@WitName("a") a: wit.Tuple2[Int, String]): wit.Tuple2[Int, String] =
    wit.native

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-tuple3")
  def roundtripTuple3(
      @WitName("a") a: wit.Tuple3[Int, String, Boolean]): wit.Tuple3[Int, String, Boolean] = {
    wit.native
  }

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-named-r")
  def roundtripNamedR(@WitName("a") a: NamedR): NamedR = wit.native

  // Named WIT type alias
  @WitAlias(WitScope.unversioned("component", "testing", "tests"), "option-u32")
  type OptionU32 = wit.Option[wit.unsigned.UInt]

  @WitImport(WitScope.unversioned("component", "testing", "tests"), "roundtrip-option-u32")
  def roundtripOptionU32(@WitName("a") a: OptionU32): OptionU32 = wit.native

}
