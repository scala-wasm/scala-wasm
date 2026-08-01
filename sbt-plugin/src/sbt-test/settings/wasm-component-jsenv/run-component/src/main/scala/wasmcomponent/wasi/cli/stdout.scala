package wasmcomponent.wasi.cli

package object stdout {

  // Type definitions
  type OutputStream = wasmcomponent.wasi.io.streams.OutputStream

  // Functions
  @scala.scalajs.wit.annotation.WitImport("wasi:cli/stdout@0.2.0", "get-stdout")
  def getStdout(): OutputStream = scala.scalajs.wit.native

}
