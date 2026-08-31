package componentmodel.wasi.clocks

import scala.scalajs.wit.annotation.{WitImport, WitName, WitRecord, WitScope}
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.{UInt, ULong}

package object wall_clock {

  @WitRecord(WitScope("wasi", "clocks", "wall-clock", "0.2.0"), "datetime")
  final case class Datetime(
      @WitName("seconds") seconds: ULong,
      @WitName("nanoseconds") nanoseconds: UInt)

  @WitImport(WitScope("wasi", "clocks", "wall-clock", "0.2.0"), "now")
  def now(): Datetime = native

  @WitImport(WitScope("wasi", "clocks", "wall-clock", "0.2.0"), "resolution")
  def resolution(): Datetime = native

}
