package componentmodel.wasi.clocks

import scala.scalajs.wit.annotation.{WitImport, WitName, WitRecord, WitScope}
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.{UInt, ULong}

package object wall_clock {

  @WitRecord(WitScope("wasi", "clocks", "wall-clock", "0.2.0"), "datetime")
  final class Datetime(
      @WitName("seconds") val seconds: ULong,
      @WitName("nanoseconds") val nanoseconds: UInt) {
    override def equals(other: Any): Boolean = other match {
      case that: Datetime => this.seconds == that.seconds && this.nanoseconds == that.nanoseconds
      case _              => false
    }

    override def hashCode(): Int = {
      var result = 1
      result = 31 * result + seconds.hashCode()
      result = 31 * result + nanoseconds.hashCode()
      result
    }

    override def toString(): String = "Datetime(" + seconds + ", " + nanoseconds + ")"
  }

  object Datetime {
    def apply(seconds: ULong, nanoseconds: UInt): Datetime = new Datetime(seconds, nanoseconds)

    def unapply(arg: Datetime): Some[(ULong, UInt)] =
      Some((arg.seconds, arg.nanoseconds))
  }

  @WitImport(WitScope("wasi", "clocks", "wall-clock", "0.2.0"), "now")
  def now(): Datetime = native

  @WitImport(WitScope("wasi", "clocks", "wall-clock", "0.2.0"), "resolution")
  def resolution(): Datetime = native

}
