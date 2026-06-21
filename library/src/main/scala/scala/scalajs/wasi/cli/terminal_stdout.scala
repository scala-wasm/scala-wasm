package scala.scalajs.wasi.cli

package object terminal_stdout {

  // Type definitions
  type TerminalOutput = scala.scalajs.wasi.cli.terminal_output.TerminalOutput

  // Functions
  /** If stdout is connected to a terminal, return a `terminal-output` handle
   *  allowing further interaction with it.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:cli/terminal-stdout@0.2.0", "get-terminal-stdout")
  def getTerminalStdout(): java.util.Optional[TerminalOutput] = scala.scalajs.wit.native

}
