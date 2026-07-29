package componentmodel.wasi.io

import scala.scalajs.wit.annotation.{
  WitImport,
  WitResourceDrop,
  WitResourceImport,
  WitResourceMethod
}
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.UInt

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

  object Pollable {}

  @WitImport("wasi:io/poll@0.2.0", "poll")
  def poll(in: Array[Pollable]): Array[UInt] = native

}
