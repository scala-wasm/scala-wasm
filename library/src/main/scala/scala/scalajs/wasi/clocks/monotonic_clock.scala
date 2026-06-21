package scala.scalajs.wasi.clocks

package object monotonic_clock {

  // Type definitions
  type Pollable = scala.scalajs.wasi.io.poll.Pollable

  type Instant = scala.scalajs.wit.unsigned.ULong

  type Duration = scala.scalajs.wit.unsigned.ULong

  // Functions
  /** Read the current value of the clock.
   *
   *  The clock is monotonic, therefore calling this function repeatedly will
   *  produce a sequence of non-decreasing values.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:clocks/monotonic-clock@0.2.0", "now")
  def now(): scala.scalajs.wit.unsigned.ULong = scala.scalajs.wit.native

  /** Query the resolution of the clock. Returns the duration of time
   *  corresponding to a clock tick.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:clocks/monotonic-clock@0.2.0", "resolution")
  def resolution(): scala.scalajs.wit.unsigned.ULong = scala.scalajs.wit.native

  /** Create a `pollable` which will resolve once the specified instant
   *  occured.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:clocks/monotonic-clock@0.2.0", "subscribe-instant")
  def subscribeInstant(when: scala.scalajs.wit.unsigned.ULong): Pollable = scala.scalajs.wit.native

  /** Create a `pollable` which will resolve once the given duration has
   *  elapsed, starting at the time at which this function was called.
   *  occured.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:clocks/monotonic-clock@0.2.0", "subscribe-duration")
  def subscribeDuration(when: scala.scalajs.wit.unsigned.ULong): Pollable = scala.scalajs.wit.native

}
