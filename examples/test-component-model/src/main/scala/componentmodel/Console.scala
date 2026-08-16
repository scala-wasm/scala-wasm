package componentmodel

import scala.scalajs.wasi.cli.stdout
import scala.scalajs.wasi.clocks.wall_clock
import scala.scalajs.wit.Ok
import scala.scalajs.wit.unsigned.UByte

object Console {
  def println(s: String): Unit = {
    val out = stdout.getStdout()
    try {
      val bytes = (s + "\n").getBytes().asInstanceOf[Array[UByte]]
      out.blockingWriteAndFlush(bytes) match {
        case _: Ok[_] => ()
        case _        => throw new RuntimeException("Failed to write to stdout")
      }
    } finally {
      out.close()
    }
  }

  def currentTimeMillis(): Long = {
    val d = wall_clock.now()
    d.seconds.toLong * 1000L + d.nanoseconds.toLong / 1000000L
  }
}
