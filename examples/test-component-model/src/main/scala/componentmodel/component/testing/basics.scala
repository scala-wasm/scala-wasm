package componentmodel.component.testing

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

package object basics {

  // Functions
  @WitImport("component:testing/basics", "roundtrip-u8")
  def roundtripU8(a: wit.unsigned.UByte): wit.unsigned.UByte = wit.native

  @WitImport("component:testing/basics", "roundtrip-s8")
  def roundtripS8(a: Byte): Byte = wit.native

  @WitImport("component:testing/basics", "roundtrip-u16")
  def roundtripU16(a: wit.unsigned.UShort): wit.unsigned.UShort = wit.native

  @WitImport("component:testing/basics", "roundtrip-s16")
  def roundtripS16(a: Short): Short = wit.native

  @WitImport("component:testing/basics", "roundtrip-u32")
  def roundtripU32(a: wit.unsigned.UInt): wit.unsigned.UInt = wit.native

  @WitImport("component:testing/basics", "roundtrip-s32")
  def roundtripS32(a: Int): Int = wit.native

  @WitImport("component:testing/basics", "roundtrip-u64")
  def roundtripU64(a: wit.unsigned.ULong): wit.unsigned.ULong = wit.native

  @WitImport("component:testing/basics", "roundtrip-s64")
  def roundtripS64(a: Long): Long = wit.native

  @WitImport("component:testing/basics", "roundtrip-f32")
  def roundtripF32(a: Float): Float = wit.native

  @WitImport("component:testing/basics", "roundtrip-f64")
  def roundtripF64(a: Double): Double = wit.native

  @WitImport("component:testing/basics", "roundtrip-char")
  def roundtripChar(a: Char): Char = wit.native

}
