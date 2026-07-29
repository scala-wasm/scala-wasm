package wasmcomponent

import scala.scalajs.wit
import scala.scalajs.wit.annotation._
import wasmcomponent.wasi.cli.stdout

object Main {
  @WitExport("wasi:cli/run@0.2.0", "run")
  def run(): wit.Result[Unit, Unit] = {
    printLine("WasmComponent run")
    new wit.Ok(())
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
