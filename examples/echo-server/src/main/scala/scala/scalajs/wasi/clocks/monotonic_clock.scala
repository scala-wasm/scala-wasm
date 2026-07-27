package scala.scalajs.wasi.clocks

import scala.scalajs.wasi.io.poll.Pollable
import scala.scalajs.wit.annotation.{WitImport, WitName, WitScope}
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.ULong

package object monotonic_clock {

  type Instant = ULong
  type Duration = ULong

  @WitImport(WitScope("wasi", "clocks", "monotonic-clock", "0.2.0"), "now")
  def now(): ULong = native

  @WitImport(WitScope("wasi", "clocks", "monotonic-clock", "0.2.0"), "resolution")
  def resolution(): ULong = native

  @WitImport(WitScope("wasi", "clocks", "monotonic-clock", "0.2.0"), "subscribe-instant")
  def subscribeInstant(@WitName("when") when: ULong): Pollable = native

  @WitImport(WitScope("wasi", "clocks", "monotonic-clock", "0.2.0"), "subscribe-duration")
  def subscribeDuration(@WitName("when") when: ULong): Pollable = native

}
