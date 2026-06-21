package java.lang

import scala.scalajs.wasi.cli.stdout
import scala.scalajs.wasi.clocks.wall_clock
import scala.scalajs.wasi.random.insecure
import scala.scalajs.wit.unsigned.UByte

protected[lang] object WasmSystem {
  @noinline
  def print(s: String): Unit = {
    val out = stdout.getStdout()
    try {
      val bytes = (s + "\n").getBytes().asInstanceOf[Array[UByte]]
      val result = out.blockingWriteAndFlush(bytes)
      if (!result.isInstanceOf[scala.scalajs.wit.Ok[_]]) {
        throw new RuntimeException("Failed to write to stdout")
      }
    } finally {
      out.close()
    }
  }

  @noinline
  def nanoTime(): scala.Long = {
    val d = wall_clock.now()
    d.seconds.toLong * 1000000000L + d.nanoseconds.toLong
  }

  @noinline
  def currentTimeMillis(): scala.Long = {
    val d = wall_clock.now()
    d.seconds.toLong * 1000L + d.nanoseconds.toLong / 1000000L
  }

  @noinline
  def random(): scala.Double = {
    val i = insecure.getInsecureRandomU64()
    // Maps a 64-bit unsigned integer i to a value within the [0.0, 1.0) range.
    // (i >>> 11) extracts the 53 most significant 53 bits [0, 2^53-1)
    // and (1.0 / (1L << 53)) is a scaling factor 2^-53.
    // by multiplying them, equally spaced values in the interval [0.0, 1.0)
    (i.toLong >>> 11) * (1.0 / (1L << 53))
  }
}
