package wasmcomponent

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

import componentmodel.exports.wasi.cli.Run

@WitImplementation
object Main extends Run {
  override def run(): wit.Result[Unit, Unit] = {
    println("WasmComponent run")
    new wit.Ok(())
  }
}
