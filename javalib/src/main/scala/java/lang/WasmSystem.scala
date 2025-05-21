package java.lang

import java.lang.wasi.clocks
import java.lang.wasi.{random => wrandom}
import java.lang.wasi.cli
import java.lang.wasi.io
import java.lang.wasi.io.Streams.StreamError

protected[lang] object WasmSystem {
  @noinline
  def print(s: String): Unit = {
    cli.Stdout.getStdout().blockingWriteAndFlush(s.getBytes())
    // if (!result.isOk) {
    //   val err = result.value.asInstanceOf[StreamError]
    //   err match {
    //     case StreamError.Closed =>
    //       throw new RuntimeException("closed")
    //     case failed: StreamError.LastOperationFailed =>
    //       throw new RuntimeException(failed.value.toDebugString())
    //   }
    // }
  }

  @noinline
  def nanoTime(): scala.Long = {
    val d = clocks.WallClock.now()
    d.seconds * 1000000000 + d.nanoseconds
  }

  @noinline
  def currentTimeMillis(): scala.Long = {
    val d = clocks.WallClock.now()
    d.seconds * 1000 + d.nanoseconds / 1000
  }

  @noinline
  def random(): scala.Double = {
    val i = wrandom.Insecure.getInsecureRandomU64()
    // Maps a 64-bit unsigned integer i to a value within the [0.0, 1.0) range.
    // (i >>> 11) extracts the 53 most significant 53 bits [0, 2^53-1)
    // and (1.0 / (1L << 53)) is a scaling factor 2^-53.
    // by multiplying them, equally spaced values in the interval [0.0, 1.0)
    (i >>> 11) * (1.0 / (1L << 53))
  }
}
