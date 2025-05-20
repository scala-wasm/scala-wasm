package componentmodel

import scala.scalajs.{component => cm}
import scala.scalajs.component.annotation._
import scala.scalajs.component.unsigned._

import java.util.Optional

object Basics {
 @ComponentImport("roundtrip-u8", "component:testing/basics")
 def roundtripU8(a: UByte): UByte = cm.native

  @ComponentImport("roundtrip-s8", "component:testing/basics")
  def roundtripS8(a: Byte): Byte = cm.native

  @ComponentImport("roundtrip-u16", "component:testing/basics")
  def roundtripU16(a: UShort): UShort = cm.native

  @ComponentImport("roundtrip-s16", "component:testing/basics")
  def roundtripS16(a: Short): Short = cm.native

  @ComponentImport("roundtrip-u32", "component:testing/basics")
  def roundtripU32(a: UInt): UInt = cm.native

  @ComponentImport("roundtrip-s32", "component:testing/basics")
  def roundtripS32(a: Int): Int = cm.native

  @ComponentImport("roundtrip-u64", "component:testing/basics")
  def roundtripU64(a: ULong): ULong = cm.native

  @ComponentImport("roundtrip-s64", "component:testing/basics")
  def roundtripS64(a: Long): Long = cm.native

  @ComponentImport("roundtrip-f32", "component:testing/basics")
  def roundtripF32(a: Float): Float = cm.native

  @ComponentImport("roundtrip-f64", "component:testing/basics")
  def roundtripF64(a: Double): Double = cm.native

  @ComponentImport("roundtrip-char", "component:testing/basics")
  def roundtripChar(a: Char): Char = cm.native
}


object Countable {
  @ComponentResourceImport("counter", "component:testing/countable")
  trait Counter {
    @ComponentResourceMethod("up")
    def up(): Unit = cm.native

    @ComponentResourceMethod("down")
    def down(): Unit = cm.native

    @ComponentResourceMethod("value-of")
    def valueOf(): Int = cm.native

    @ComponentResourceDrop
    def close(): Unit = cm.native
  }
  object Counter {
    @ComponentResourceConstructor
    def apply(i: Int): Counter = cm.native

    @ComponentResourceStaticMethod("sum")
    def sum(a: Counter, b: Counter): Counter = cm.native
  }
}

import TestImportsHelper._
object Tests {
  @ComponentImport("roundtrip-basics1", "component:testing/tests")
  def roundtripBasics1(a: (UByte, Byte, UShort, Short, UInt, Int, Float, Double, Char)):
      (UByte, Byte, UShort, Short, UInt, Int, Float, Double, Char) = cm.native

  @ComponentImport("roundtrip-list-u16", "component:testing/tests")
  def roundtripListU16(a: Array[UShort]): Array[UShort] = cm.native

  @ComponentImport("roundtrip-list-point", "component:testing/tests")
  def roundtripListPoint(a: Array[Point]): Array[Point] = cm.native

  @ComponentImport("roundtrip-list-variant", "component:testing/tests")
  def roundtripListVariant(a: Array[C1]): Array[C1] = cm.native

  @ComponentImport("roundtrip-string", "component:testing/tests")
  def roundtripString(a: String): String = cm.native

  @ComponentImport("roundtrip-point", "component:testing/tests")
  def roundtripPoint(a: Point): Point = cm.native

  @ComponentImport("test-c1", "component:testing/tests")
  def testC1(a: C1): Unit = cm.native

  @ComponentImport("roundtrip-c1", "component:testing/tests")
  def roundtripC1(a: C1): C1 = cm.native

  @ComponentImport("roundtrip-z1", "component:testing/tests")
  def roundtripZ1(a: Z1): Z1 = cm.native

  @ComponentImport("roundtrip-enum", "component:testing/tests")
  def roundtripEnum(a: E1): E1 = cm.native

  @ComponentImport("roundtrip-tuple", "component:testing/tests")
  def roundtripTuple(a: (C1, Z1)): (C1, Z1) = cm.native

  @ComponentImport("roundtrip-option", "component:testing/tests")
  def roundtripOption(a: Optional[String]): Optional[String] = cm.native

  @ComponentImport("roundtrip-double-option", "component:testing/tests")
  def roundtripDoubleOption(a: Optional[Optional[String]]): Optional[Optional[String]] = cm.native

  @ComponentImport("roundtrip-result", "component:testing/tests")
  def roundtripResult(a: cm.Result[Unit, Unit]): cm.Result[Unit, Unit] = cm.native

  @ComponentImport("roundtrip-string-error", "component:testing/tests")
  def roundtripStringError(a: cm.Result[Float, String]): cm.Result[Float, String] = cm.native

  @ComponentImport("roundtrip-enum-error", "component:testing/tests")
  def roundtripEnumError(a: cm.Result[C1, E1]): cm.Result[C1, E1] = cm.native

  @ComponentImport("roundtrip-f8", "component:testing/tests")
  def roundtripF8(a: F1): F1 = cm.native

  @ComponentImport("roundtrip-flags", "component:testing/tests")
  def roundtripFlags(a: (F1, F1)): (F1, F1) = cm.native
}
