package helloworld

import scala.scalajs.wit.annotation._
import scala.scalajs.wit
import helloworld.exports.wasi.cli.Run

@WitImplementation
object HelloWorld extends Run {
  override def run(): wit.Result[Unit, Unit] = {
    println("Hello world!")
    new wit.Ok(())
  }
}
