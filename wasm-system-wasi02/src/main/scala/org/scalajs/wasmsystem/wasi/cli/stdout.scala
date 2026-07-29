package org.scalajs.wasmsystem.wasi.cli

import org.scalajs.wasmsystem.wasi.io.streams.OutputStream
import scala.scalajs.wit.annotation.WitImport
import scala.scalajs.wit.native

package object stdout {

  @WitImport("wasi:cli/stdout@0.2.0", "get-stdout")
  def getStdout(): OutputStream = native

}
