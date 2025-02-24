package componentmodel

import scala.scalajs.{component => cm}
import cm.annotation._
import cm.unsigned._

@ComponentImport("component:testing/basics")
object Basics extends cm.Interface {
  def roundtripU8(a: UByte): UByte = cm.native
  def roundtripS8(a: Byte): Byte = cm.native
  def roundtripU16(a: UShort): UShort = cm.native
  def roundtripS16(a: Short): Short = cm.native
  def roundtripU32(a: UInt): UInt = cm.native
  def roundtripS32(a: Int): Int = cm.native
  // def roundtripU64(a: ULong): ULong = cm.native
  // def roundtripS64(a: Long): Long = cm.native
  def roundtripF32(a: Float): Float = cm.native
  def roundtripF64(a: Double): Double = cm.native
  def roundtripChar(a: Char): Char = cm.native
}

import TestImportsHelper._
@ComponentImport("component:testing/tests")
object Tests extends cm.Interface {
  def roundtripBasics1(a: (UByte, Byte, UShort, Short, UInt, Int, Float, Double, Char)):
    (UByte, Byte, UShort, Short, UInt, Int, Float, Double, Char) = cm.native
  def roundtripListU16(a: Array[UShort]): Array[UShort] = cm.native
  def roundtripListPoint(a: Array[Point]): Array[Point] = cm.native
  def roundtripListVariant(a: Array[C1]): Array[C1] = cm.native
  def roundtripString(a: String): String = cm.native
  def roundtripPoint(a: Point): Point = cm.native
  def testC1(a: C1): Unit = cm.native
  def roundtripC1(a: C1): C1 = cm.native
  def roundtripZ1(a: Z1): Z1 = cm.native
  def roundtripEnum(a: E1): E1 = cm.native
  def roundtripTuple(a: (C1, Z1)): (C1, Z1) = cm.native
  def roundtripOption(a: cm.Option[String]): cm.Option[String] = cm.native
  def roundtripDoubleOption(a: cm.Option[cm.Option[String]]): cm.Option[cm.Option[String]] = cm.native
  def roundtripResult(a: cm.Result[Unit, Unit]): cm.Result[Unit, Unit] = cm.native
  def roundtripStringError(a: cm.Result[Float, String]): cm.Result[Float, String] = cm.native
  def roundtripEnumError(a: cm.Result[C1, E1]): cm.Result[C1, E1] = cm.native
  def roundtripF8(a: F1): F1 = cm.native
  def roundtripFlags(a: (F1, F1)): (F1, F1) = cm.native
}
