package wasmcomponent.wasi.io

package object poll {

  // Resources
  @scala.scalajs.wit.annotation.WitResourceImport(
      scala.scalajs.wit.annotation.WitScope("wasi", "io", "poll", "0.2.0"),
      "pollable")
  final class Pollable private () extends Object {
    @scala.scalajs.wit.annotation.WitResourceMethod("ready")
    def ready(): Boolean = scala.scalajs.wit.native
    @scala.scalajs.wit.annotation.WitResourceMethod("block")
    def block(): Unit = scala.scalajs.wit.native
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }
  object Pollable {
  }

  // Functions
  @scala.scalajs.wit.annotation.WitImport(
      scala.scalajs.wit.annotation.WitScope("wasi", "io", "poll", "0.2.0"),
      "poll")
  def poll(
      @scala.scalajs.wit.annotation.WitName("in") in: Array[Pollable]
  ): Array[scala.scalajs.wit.unsigned.UInt] = scala.scalajs.wit.native

}
