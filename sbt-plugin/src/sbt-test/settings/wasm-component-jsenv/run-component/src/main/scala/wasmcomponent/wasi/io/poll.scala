package wasmcomponent.wasi.io

import scala.scalajs.wit.annotation.{WitResourceDrop, WitResourceImport, WitResourceMethod}
import scala.scalajs.wit.native

package object poll {

  @WitResourceImport("wasi:io/poll@0.2.0", "pollable")
  final class Pollable private () extends Object {
    @WitResourceMethod("ready")
    def ready(): Boolean = native
    @WitResourceMethod("block")
    def block(): Unit = native
    @WitResourceDrop
    def close(): Unit = native
  }
  object Pollable { }

}
