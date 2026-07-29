package scala.scalajs.wasi.io

import scala.scalajs.wit.annotation.{WitResourceDrop, WitResourceImport, WitResourceMethod}
import scala.scalajs.wit.native

package object error {

  @WitResourceImport("wasi:io/error@0.2.0", "error")
  final class Error private () extends Object {
    @WitResourceMethod("to-debug-string")
    def toDebugString(): String = native

    @WitResourceDrop
    def close(): Unit = native
  }

  object Error {}

}
