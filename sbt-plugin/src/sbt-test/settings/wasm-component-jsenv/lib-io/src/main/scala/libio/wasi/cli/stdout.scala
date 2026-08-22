package libio.wasi.cli

import libio.wasi.io.streams.OutputStream
import scala.scalajs.wit.annotation.{WitImport, WitScope}
import scala.scalajs.wit.native

package object stdout {

  @WitImport(WitScope("wasi", "cli", "stdout", "0.2.0"), "get-stdout")
  def getStdout(): OutputStream = native

}
