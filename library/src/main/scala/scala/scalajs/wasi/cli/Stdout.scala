package scala.scalajs.wasi.cli

package object stdout {

  // Type definitions
  type OutputStream = scala.scalajs.wasi.io.streams.OutputStream

  // Functions
  @scala.scalajs.wit.annotation.WitImport("wasi:cli/stdout@0.2.0", "get-stdout")
  def getStdout(): OutputStream = scala.scalajs.wit.native

}
