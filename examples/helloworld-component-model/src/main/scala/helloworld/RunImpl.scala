package helloworld

import helloworld.scala_wasm.helloworld.greeter.greet

object RunImpl {

  def main(args: Array[String]): Unit = {
    val greeting = greet("Scala")
    println(greeting)
  }

}
