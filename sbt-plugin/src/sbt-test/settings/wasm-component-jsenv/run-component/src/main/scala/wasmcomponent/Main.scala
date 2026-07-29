package wasmcomponent

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

object Main {
  @WitExport("wasi:cli/run@0.2.0", "run")
  def run(): wit.Result[Unit, Unit] = {
    println("WasmComponent run")
    new wit.Ok(())
  }
}
