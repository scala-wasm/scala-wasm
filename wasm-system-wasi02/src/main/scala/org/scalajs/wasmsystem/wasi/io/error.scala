package org.scalajs.wasmsystem.wasi.io

import scala.scalajs.wit.annotation.{WitResourceDrop, WitResourceImport, WitScope}
import scala.scalajs.wit.native

package object error {

  @WitResourceImport(WitScope("wasi", "io", "error", "0.2.0"), "error")
  final class Error private () extends Object {
    @WitResourceDrop
    def close(): Unit = native
  }

  object Error {}

}
