package componentmodel.wasi.clocks

package object wall_clock {

  // Type definitions
  @scala.scalajs.wit.annotation.WitRecord
  final class Datetime(val seconds: scala.scalajs.wit.unsigned.ULong,
      val nanoseconds: scala.scalajs.wit.unsigned.UInt) {
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
    def apply(seconds: scala.scalajs.wit.unsigned.ULong,
        nanoseconds: scala.scalajs.wit.unsigned.UInt): Datetime = new Datetime(seconds, nanoseconds)

    def unapply(
        arg: Datetime): Some[(scala.scalajs.wit.unsigned.ULong, scala.scalajs.wit.unsigned.UInt)] = {
      Some((arg.seconds, arg.nanoseconds))
    }
  }

  // Functions
  @scala.scalajs.wit.annotation.WitImport("wasi:clocks/wall-clock@0.2.0", "now")
  def now(): Datetime = scala.scalajs.wit.native

  @scala.scalajs.wit.annotation.WitImport("wasi:clocks/wall-clock@0.2.0", "resolution")
  def resolution(): Datetime = scala.scalajs.wit.native

}
