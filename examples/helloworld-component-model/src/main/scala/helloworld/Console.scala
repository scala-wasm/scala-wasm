package helloworld

import helloworld.wasi.cli.stdout
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
}
