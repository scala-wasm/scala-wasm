package helloworld

import scala.scalajs.wit.annotation._
import scala.scalajs.wit

import helloworld.exports.wasi.cli.Run
import helloworld.scala_wasm.helloworld.greeter.greet

/** Implementation using the new @WitImplementation pattern */
@WitImplementation
object RunImpl extends Run {

  override def run(): wit.Result[Unit, Unit] = {
    val greeting = greet("Scala")
    println(greeting)
    new wit.Ok(())
  }

}
