package scala.scalajs.wasi.cli

package object exit {

  // Functions
  /** Exit the current instance and any linked instances. */
  @scala.scalajs.wit.annotation.WitImport("wasi:cli/exit@0.2.0", "exit")
  def exit(status: scala.scalajs.wit.Result[Unit, Unit]): Unit = scala.scalajs.wit.native

}
