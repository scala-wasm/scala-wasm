package example

import scala.scalajs.wasi.random.random

object Main {
  def main(args: Array[String]): Unit = {
    random.getRandomU64()
    println("run-ok")
  }
}
