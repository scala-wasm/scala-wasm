package scala.scalajs.wasi.cli

package object terminal_input {

  // Resources
  /** The input side of a terminal. */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:cli/terminal-input@0.2.0", "terminal-input")
  trait TerminalInput {
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }

  object TerminalInput {}

}
