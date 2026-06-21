package scala.scalajs.wasi.cli

package object terminal_stdin {

  // Type definitions
  type TerminalInput = scala.scalajs.wasi.cli.terminal_input.TerminalInput

  // Functions
  /** If stdin is connected to a terminal, return a `terminal-input` handle
   *  allowing further interaction with it.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:cli/terminal-stdin@0.2.0", "get-terminal-stdin")
  def getTerminalStdin(): java.util.Optional[TerminalInput] = scala.scalajs.wit.native

}
