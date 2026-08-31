package componentmodel

import componentmodel.wasi.cli.stdout
import componentmodel.wasi.clocks.wall_clock
import scala.scalajs.wit.{Err, Ok}
import scala.scalajs.wit.unsigned.UByte

object Console {
  def println(s: String): Unit = {
    val out = stdout.getStdout()
    try {
      val bytes = (s + "\n").getBytes().asInstanceOf[Array[UByte]]
      out.blockingWriteAndFlush(bytes) match {
        case Ok(_)  => ()
        case Err(e) => throw new RuntimeException(s"Failed to write to stdout: $e")
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
