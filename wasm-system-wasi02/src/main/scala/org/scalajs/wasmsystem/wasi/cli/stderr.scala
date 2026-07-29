package org.scalajs.wasmsystem.wasi.cli

import org.scalajs.wasmsystem.wasi.io.streams.OutputStream
import scala.scalajs.wit.annotation.WitImport
import scala.scalajs.wit.native

package object stderr {

  @WitImport("wasi:cli/stderr@0.2.0", "get-stderr")
  def getStderr(): OutputStream = native

}
