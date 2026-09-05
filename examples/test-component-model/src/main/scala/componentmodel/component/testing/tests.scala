package componentmodel.component.testing

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

package object tests {

  // Type definitions
  @WitRecord(WitScope.unversioned("component", "testing", "tests"), "point")
  final class Point(@WitName("x") val x: Int, @WitName("y") val y: Int) {
    override def equals(other: Any): Boolean = other match {
      case that: Point => this.x == that.x && this.y == that.y
      case _           => false
    }

    override def hashCode(): Int = {
      var result = 1
      result = 31 * result + x.hashCode()
      result = 31 * result + y.hashCode()
      result
    }

    override def toString(): String = "Point(" + x + ", " + y + ")"
  }

  object Point {
    def apply(x: Int, y: Int): Point = new Point(x, y)
    def unapply(arg: Point): Some[(Int, Int)] = Some((arg.x, arg.y))
  }

  @WitVariant(WitScope.unversioned("component", "testing", "tests"), "c1")
  sealed trait C1

  object C1 {
    @WitName("a")
    final class A(@WitName("value") val value: Int) extends C1 {
      override def equals(other: Any): Boolean = other match {
        case that: A => this.value == that.value
        case _       => false
      }

      override def hashCode(): Int =
        value.hashCode()

      override def toString(): String = "A(" + value + ")"
    }

    object A {
      def apply(value: Int): A = new A(value)
      def unapply(arg: A): Some[Int] = Some(arg.value)
    }

    @WitName("b")
    final class B(@WitName("value") val value: Float) extends C1 {
      override def equals(other: Any): Boolean = other match {
        case that: B => this.value == that.value
        case _       => false
      }

      override def hashCode(): Int =
        value.hashCode()

      override def toString(): String = "B(" + value + ")"
    }

    object B {
      def apply(value: Float): B = new B(value)
      def unapply(arg: B): Some[Float] = Some(arg.value)
    }
  }

  @WitVariant(WitScope.unversioned("component", "testing", "tests"), "z1")
  sealed trait Z1

  object Z1 {
    @WitName("a")
    final class A(@WitName("value") val value: Int) extends Z1 {
      override def equals(other: Any): Boolean = other match {
        case that: A => this.value == that.value
        case _       => false
      }

      override def hashCode(): Int =
        value.hashCode()

      override def toString(): String = "A(" + value + ")"
    }

    object A {
      def apply(value: Int): A = new A(value)
      def unapply(arg: A): Some[Int] = Some(arg.value)
    }

    @WitName("b")
    object B extends Z1 {
      override def toString(): String = "B"
    }
  }

  @WitEnum(WitScope.unversioned("component", "testing", "tests"), "e1")
  sealed trait E1

  object E1 {
    @WitName("a")
    object A extends E1 {
      override def toString(): String = "A"
    }

    @WitName("b")
    object B extends E1 {
      override def toString(): String = "B"
    }

    @WitName("c")
    object C extends E1 {
      override def toString(): String = "C"
    }
  }

  @WitFlags(WitScope.unversioned("component", "testing", "tests"), "f1",
      Array("b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7"))
  final class F1(val value: Int) {
    def |(other: F1): F1 = new F1(value | other.value)
    def &(other: F1): F1 = new F1(value & other.value)
    def ^(other: F1): F1 = new F1(value ^ other.value)
    def unary_~ : F1 = new F1(~value)
    def contains(other: F1): Boolean = (value & other.value) == other.value

    override def equals(other: Any): Boolean = other match {
      case that: F1 => this.value == that.value
      case _        => false
    }

    override def hashCode(): Int =
      value.hashCode()

    override def toString(): String = "F1(" + value + ")"
  }

  object F1 {
    def apply(value: Int): F1 = new F1(value)
    def unapply(arg: F1): Some[Int] = Some(arg.value)
    val b0 = new F1(1 << 0)
    val b1 = new F1(1 << 1)
    val b2 = new F1(1 << 2)
    val b3 = new F1(1 << 3)
    val b4 = new F1(1 << 4)
    val b5 = new F1(1 << 5)
    val b6 = new F1(1 << 6)
    val b7 = new F1(1 << 7)
  }

  @WitFlags(WitScope.unversioned("component", "testing", "tests"), "f2",
      Array("b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7",
          "b8", "b9", "b10", "b11", "b12", "b13", "b14", "b15"))
  final class F2(val value: Int) {
    def |(other: F2): F2 = new F2(value | other.value)
    def &(other: F2): F2 = new F2(value & other.value)
    def ^(other: F2): F2 = new F2(value ^ other.value)
    def unary_~ : F2 = new F2(~value)
    def contains(other: F2): Boolean = (value & other.value) == other.value

    override def equals(other: Any): Boolean = other match {
      case that: F2 => this.value == that.value
      case _        => false
    }

    override def hashCode(): Int =
      value.hashCode()

    override def toString(): String = "F2(" + value + ")"
  }

  object F2 {
    def apply(value: Int): F2 = new F2(value)
    def unapply(arg: F2): Some[Int] = Some(arg.value)
    val b0 = new F2(1 << 0)
    val b1 = new F2(1 << 1)
    val b2 = new F2(1 << 2)
    val b3 = new F2(1 << 3)
    val b4 = new F2(1 << 4)
    val b5 = new F2(1 << 5)
    val b6 = new F2(1 << 6)
    val b7 = new F2(1 << 7)
    val b8 = new F2(1 << 8)
    val b9 = new F2(1 << 9)
    val b10 = new F2(1 << 10)
    val b11 = new F2(1 << 11)
    val b12 = new F2(1 << 12)
    val b13 = new F2(1 << 13)
    val b14 = new F2(1 << 14)
    val b15 = new F2(1 << 15)
  }

  @WitFlags(WitScope.unversioned("component", "testing", "tests"), "f3",
      Array("b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7",
          "b8", "b9", "b10", "b11", "b12", "b13", "b14", "b15",
          "b16", "b17", "b18", "b19", "b20", "b21", "b22", "b23",
          "b24", "b25", "b26", "b27", "b28", "b29", "b30", "b31"))
  final class F3(val value: Int) {
    def |(other: F3): F3 = new F3(value | other.value)
    def &(other: F3): F3 = new F3(value & other.value)
    def ^(other: F3): F3 = new F3(value ^ other.value)
    def unary_~ : F3 = new F3(~value)
    def contains(other: F3): Boolean = (value & other.value) == other.value

    override def equals(other: Any): Boolean = other match {
      case that: F3 => this.value == that.value
      case _        => false
    }

    override def hashCode(): Int =
      value.hashCode()

    override def toString(): String = "F3(" + value + ")"
  }

  object F3 {
    def apply(value: Int): F3 = new F3(value)
    def unapply(arg: F3): Some[Int] = Some(arg.value)
    val b0 = new F3(1 << 0)
    val b1 = new F3(1 << 1)
    val b2 = new F3(1 << 2)
    val b3 = new F3(1 << 3)
    val b4 = new F3(1 << 4)
    val b5 = new F3(1 << 5)
    val b6 = new F3(1 << 6)
    val b7 = new F3(1 << 7)
    val b8 = new F3(1 << 8)
    val b9 = new F3(1 << 9)
    val b10 = new F3(1 << 10)
    val b11 = new F3(1 << 11)
    val b12 = new F3(1 << 12)
    val b13 = new F3(1 << 13)
    val b14 = new F3(1 << 14)
    val b15 = new F3(1 << 15)
    val b16 = new F3(1 << 16)
    val b17 = new F3(1 << 17)
    val b18 = new F3(1 << 18)
    val b19 = new F3(1 << 19)
    val b20 = new F3(1 << 20)
    val b21 = new F3(1 << 21)
    val b22 = new F3(1 << 22)
    val b23 = new F3(1 << 23)
    val b24 = new F3(1 << 24)
    val b25 = new F3(1 << 25)
    val b26 = new F3(1 << 26)
    val b27 = new F3(1 << 27)
    val b28 = new F3(1 << 28)
    val b29 = new F3(1 << 29)
    val b30 = new F3(1 << 30)
    val b31 = new F3(1 << 31)
  }

  @WitFlags(WitScope.unversioned("component", "testing", "tests"), "named-f", Array("b0"))
  final class NamedF(val value: Int) {
    override def equals(other: Any): Boolean = other match {
      case that: NamedF => this.value == that.value
      case _            => false
    }

    override def hashCode(): Int = value.hashCode()
  }

  object NamedF {
    val b0 = new NamedF(1 << 0)
  }

  @WitRecord(WitScope.unversioned("component", "testing", "tests"), "named-r")
  final class NamedR(@WitName("b") val b: NamedF) {
    override def equals(other: Any): Boolean = other match {
      case that: NamedR => this.b == that.b
      case _            => false
    }

    override def hashCode(): Int = b.hashCode()
  }

  object NamedR {
    def apply(b: NamedF): NamedR = new NamedR(b)
    def unapply(arg: NamedR): Some[NamedF] = Some(arg.b)
  }

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
      @WitName("a") a: wit.Option[wit.Option[String]]): wit.Option[
      wit.Option[String]] = wit.native

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
