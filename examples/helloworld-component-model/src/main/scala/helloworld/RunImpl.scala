package helloworld

import helloworld.scala_wasm.helloworld.greeter.greet
import helloworld.wasi.cli.stdout

object RunImpl {
  def main(args: Array[String]): Unit = {
    val greeting = greet("Scala")
    printLine(greeting)
  }

  private def printLine(message: String): Unit = {
    val out = stdout.getStdout()
    try {
      out.blockingWriteAndFlush((message + "\n").getBytes())
    } finally {
      out.close()
    }
  }
}
