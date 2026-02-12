package scala.scalajs.wasi.cli

package object run {

  // Functions
  /** Run the program.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:cli/run@0.2.0", "run")
  def run(): scala.scalajs.wit.Result[Unit, Unit] = scala.scalajs.wit.native

}
