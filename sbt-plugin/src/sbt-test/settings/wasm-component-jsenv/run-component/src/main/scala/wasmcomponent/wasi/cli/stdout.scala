package wasmcomponent.wasi.cli

import wasmcomponent.wasi.io.streams.OutputStream
import scala.scalajs.wit.annotation.WitImport
import scala.scalajs.wit.native

package object stdout {

  @WitImport("wasi:cli/stdout@0.2.0", "get-stdout")
  def getStdout(): OutputStream = native

}
