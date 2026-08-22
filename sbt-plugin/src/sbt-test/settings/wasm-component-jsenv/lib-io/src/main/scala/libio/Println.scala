package libio

import scala.scalajs.wit.unsigned.UByte

object Println {
  def println(message: String): Unit = {
    val out = libio.wasi.cli.stdout.getStdout()
    try {
      out.blockingWriteAndFlush(
          (message + "\n").getBytes().asInstanceOf[Array[UByte]])
    } finally {
      out.close()
    }
  }
}
