package libclock

/** Reads the monotonic clock through `wasi:clocks`. */
object Clock {
  def now(): Long =
    libclock.wasi.clocks.monotonic_clock.now()
}
