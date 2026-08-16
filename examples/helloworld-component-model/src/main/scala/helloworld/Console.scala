package helloworld

import scala.scalajs.wasi.cli.stdout
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
}
