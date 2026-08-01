package componentmodel.component.testing

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

package object basics {

  // Functions
  @WitImport(WitScope.unversioned("component", "testing", "basics"), "roundtrip-u8")
  def roundtripU8(@WitName("a") a: wit.unsigned.UByte): wit.unsigned.UByte = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "basics"), "roundtrip-s8")
  def roundtripS8(@WitName("a") a: Byte): Byte = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "basics"), "roundtrip-u16")
  def roundtripU16(@WitName("a") a: wit.unsigned.UShort): wit.unsigned.UShort = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "basics"), "roundtrip-s16")
  def roundtripS16(@WitName("a") a: Short): Short = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "basics"), "roundtrip-u32")
  def roundtripU32(@WitName("a") a: wit.unsigned.UInt): wit.unsigned.UInt = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "basics"), "roundtrip-s32")
  def roundtripS32(@WitName("a") a: Int): Int = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "basics"), "roundtrip-u64")
  def roundtripU64(@WitName("a") a: wit.unsigned.ULong): wit.unsigned.ULong = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "basics"), "roundtrip-s64")
  def roundtripS64(@WitName("a") a: Long): Long = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "basics"), "roundtrip-f32")
  def roundtripF32(@WitName("a") a: Float): Float = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "basics"), "roundtrip-f64")
  def roundtripF64(@WitName("a") a: Double): Double = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "basics"), "roundtrip-char")
  def roundtripChar(@WitName("a") a: Char): Char = wit.native

}
