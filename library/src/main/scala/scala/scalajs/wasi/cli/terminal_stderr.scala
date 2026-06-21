package scala.scalajs.wasi.cli

package object terminal_stderr {

  // Type definitions
  type TerminalOutput = scala.scalajs.wasi.cli.terminal_output.TerminalOutput

  // Functions
  /** If stderr is connected to a terminal, return a `terminal-output` handle
   *  allowing further interaction with it.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:cli/terminal-stderr@0.2.0", "get-terminal-stderr")
  def getTerminalStderr(): java.util.Optional[TerminalOutput] = scala.scalajs.wit.native

}
