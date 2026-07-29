package helloworld

import scala.scalajs.wasi.cli.stdout

object HelloWorld {
  def main(args: Array[String]): Unit =
    printLine("Hello world!")

  private def printLine(message: String): Unit = {
    val out = stdout.getStdout()
    try {
      out.blockingWriteAndFlush(
          (message + "\n").getBytes())
    } finally {
      out.close()
    }
  }
}
