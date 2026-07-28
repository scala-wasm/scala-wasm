package componentmodel.component.testing

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

package object tests {

  // Type definitions
  @WitRecord
  final class Point(val x: Int, val y: Int) {
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

  @WitVariant
  sealed trait C1

  object C1 {
    final class A(val value: Int) extends C1 {
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

    final class B(val value: Float) extends C1 {
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

  @WitVariant
  sealed trait Z1

  object Z1 {
    final class A(val value: Int) extends Z1 {
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

    object B extends Z1 {
      override def toString(): String = "B"
    }
  }

  @WitVariant
  sealed trait E1

  object E1 {
    object A extends E1 {
      override def toString(): String = "A"
    }

    object B extends E1 {
      override def toString(): String = "B"
    }

    object C extends E1 {
      override def toString(): String = "C"
    }
  }

  @WitFlags(8)
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

  @WitFlags(16)
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

  @WitFlags(32)
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

  // Functions
  /** roundtrip-basics0: func(a: tuple<u32, s32>)
   *    -> tuple<u32, s32>;
   */
  @WitImport("component:testing/tests", "roundtrip-basics1")
  def roundtripBasics1(a: wit.Tuple9[wit.unsigned.UByte, Byte, wit.unsigned.UShort, Short,
          wit.unsigned.UInt, Int, Float, Double, Char]): wit.Tuple9[wit.unsigned.UByte, Byte,
      wit.unsigned.UShort, Short, wit.unsigned.UInt, Int, Float, Double, Char] = wit.native

  @WitImport("component:testing/tests", "roundtrip-string")
  def roundtripString(a: String): String = wit.native

  @WitImport("component:testing/tests", "roundtrip-point")
  def roundtripPoint(a: Point): Point = wit.native

  @WitImport("component:testing/tests", "roundtrip-list-u16")
  def roundtripListU16(a: Array[wit.unsigned.UShort]): Array[wit.unsigned.UShort] = wit.native

  @WitImport("component:testing/tests", "roundtrip-list-point")
  def roundtripListPoint(a: Array[Point]): Array[Point] = wit.native

  @WitImport("component:testing/tests", "roundtrip-list-variant")
  def roundtripListVariant(a: Array[C1]): Array[C1] = wit.native

  @WitImport("component:testing/tests", "test-c1")
  def testC1(a: C1): Unit = wit.native

  @WitImport("component:testing/tests", "roundtrip-c1")
  def roundtripC1(a: C1): C1 = wit.native

  @WitImport("component:testing/tests", "roundtrip-z1")
  def roundtripZ1(a: Z1): Z1 = wit.native

  @WitImport("component:testing/tests", "roundtrip-enum")
  def roundtripEnum(a: E1): E1 = wit.native

  @WitImport("component:testing/tests", "roundtrip-tuple")
  def roundtripTuple(a: wit.Tuple2[C1, Z1]): wit.Tuple2[C1, Z1] = wit.native

  @WitImport("component:testing/tests", "roundtrip-option")
  def roundtripOption(a: java.util.Optional[String]): java.util.Optional[String] = wit.native

  @WitImport("component:testing/tests", "roundtrip-double-option")
  def roundtripDoubleOption(a: java.util.Optional[java.util.Optional[String]]): java.util.Optional[
      java.util.Optional[String]] = wit.native

  @WitImport("component:testing/tests", "roundtrip-f1")
  def roundtripF1(a: F1): F1 = wit.native

  @WitImport("component:testing/tests", "roundtrip-f2")
  def roundtripF2(a: F2): F2 = wit.native

  @WitImport("component:testing/tests", "roundtrip-f3")
  def roundtripF3(a: F3): F3 = wit.native

  @WitImport("component:testing/tests", "roundtrip-flags")
  def roundtripFlags(a: wit.Tuple2[F1, F1]): wit.Tuple2[F1, F1] = wit.native

  @WitImport("component:testing/tests", "roundtrip-result")
  def roundtripResult(a: wit.Result[Unit, Unit]): wit.Result[Unit, Unit] = wit.native

  @WitImport("component:testing/tests", "roundtrip-string-error")
  def roundtripStringError(a: wit.Result[Float, String]): wit.Result[Float, String] = wit.native

  @WitImport("component:testing/tests", "roundtrip-enum-error")
  def roundtripEnumError(a: wit.Result[C1, E1]): wit.Result[C1, E1] = wit.native

  @WitImport("component:testing/tests", "roundtrip-tuple2")
  def roundtripTuple2(a: wit.Tuple2[Int, String]): wit.Tuple2[Int, String] = wit.native

  @WitImport("component:testing/tests", "roundtrip-tuple3")
  def roundtripTuple3(a: wit.Tuple3[Int, String, Boolean]): wit.Tuple3[Int, String, Boolean] =
    wit.native

}
