package scala.scalajs.wasi.clocks

import scala.scalajs.wasi.io.poll.Pollable
import scala.scalajs.wit.annotation.WitImport
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.ULong

package object monotonic_clock {

  type Instant = ULong
  type Duration = ULong

  @WitImport("wasi:clocks/monotonic-clock@0.2.0", "now")
  def now(): ULong = native

  @WitImport("wasi:clocks/monotonic-clock@0.2.0", "resolution")
  def resolution(): ULong = native

  @WitImport("wasi:clocks/monotonic-clock@0.2.0", "subscribe-instant")
  def subscribeInstant(when: ULong): Pollable = native

  @WitImport("wasi:clocks/monotonic-clock@0.2.0", "subscribe-duration")
  def subscribeDuration(when: ULong): Pollable = native

}
