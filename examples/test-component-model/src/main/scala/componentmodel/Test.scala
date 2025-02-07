package componentmodel

import scala.scalajs.component
import component.annotation._
import component.unsigned._

@ComponentImport("component:testing/basics")
object Basics extends component.Interface {
  def roundtripU8(a: UByte): UByte = component.native
  def roundtripS8(a: Byte): Byte = component.native
  def roundtripU16(a: UShort): UShort = component.native
  def roundtripS16(a: Short): Short = component.native
  def roundtripU32(a: UInt): UInt = component.native
  def roundtripS32(a: Int): Int = component.native
  // def roundtripU64(a: ULong): ULong = component.native
  // def roundtripS64(a: Long): Long = component.native
  def roundtripF32(a: Float): Float = component.native
  def roundtripF64(a: Double): Double = component.native
  def roundtripChar(a: Char): Char = component.native
}

@ComponentImport("component:testing/tests")
object Tests extends component.Interface {
  def roundtripString(a: String): String = component.native
  def roundtripPoint(a: Point): Point = component.native
}