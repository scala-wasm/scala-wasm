package libb

object Clock {
  def now(): scala.scalajs.wit.unsigned.ULong =
    libb.wasi.clocks.monotonic_clock.now()
}
