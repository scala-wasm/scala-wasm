package scala.scalajs.wasi.clocks

package object wall_clock {

  // Type definitions
  /** A time and date in seconds plus nanoseconds. */
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
  }

  // Functions
  /** Read the current value of the clock.
   *
   *  This clock is not monotonic, therefore calling this function repeatedly
   *  will not necessarily produce a sequence of non-decreasing values.
   *
   *  The returned timestamps represent the number of seconds since
   *  1970-01-01T00:00:00Z, also known as [POSIX's Seconds Since the Epoch],
   *  also known as [Unix Time].
   *
   *  The nanoseconds field of the output is always less than 1000000000.
   *
   *  [POSIX's Seconds Since the Epoch]: https://pubs.opengroup.org/onlinepubs/9699919799/xrat/V4_xbd_chap04.html#tag_21_04_16
   *  [Unix Time]: https://en.wikipedia.org/wiki/Unix_time
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:clocks/wall-clock@0.2.0", "now")
  def now(): Datetime = scala.scalajs.wit.native

  /** Query the resolution of the clock.
   *
   *  The nanoseconds field of the output is always less than 1000000000.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:clocks/wall-clock@0.2.0", "resolution")
  def resolution(): Datetime = scala.scalajs.wit.native

}
