package java.lang.wasi.clocks

import scala.scalajs.component.annotation._
import scala.scalajs.component.unsigned._
import scala.scalajs.{component => cm}

/** WASI Wall Clock is a clock API intended to let users query the current
 *  time. The name "wall" makes an analogy to a "clock on the wall", which
 *  is not necessarily monotonic as it may be reset.
 *
 *  It is intended to be portable at least between Unix-family platforms and
 *  Windows.
 *
 *  A wall clock is a clock which measures the date and time according to
 *  some external reference.
 *
 *  External references may be reset, so this clock is not necessarily
 *  monotonic, making it unsuitable for measuring elapsed time.
 *
 *  It is intended for reporting the current date and time for humans.
 *
 *  @see https://github.com/WebAssembly/WASI/blob/main/wasip2/clocks/wall-clock.wit
 */
object WallClock {

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
  @ComponentImport("now", "wasi:clocks/wall-clock@0.2.0")
  def now(): Datetime = cm.native

  /** Query the resolution of the clock.
   *
   *  The nanoseconds field of the output is always less than 1000000000.
   */
  @ComponentImport("resolution", "wasi:clocks/wall-clock")
  def resolution(): Datetime = cm.native

  /** A time and date in seconds plus nanoseconds. */
  @ComponentRecord
  final class Datetime(val seconds: ULong, val nanoseconds: UInt)
}