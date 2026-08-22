package libclock.wasi.clocks

import scala.scalajs.wit.annotation.{WitImport, WitScope}
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.ULong

package object monotonic_clock {

  @WitImport(WitScope("wasi", "clocks", "monotonic-clock", "0.2.0"), "now")
  def now(): ULong = native

}
