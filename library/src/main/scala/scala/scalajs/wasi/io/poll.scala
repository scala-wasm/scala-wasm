package scala.scalajs.wasi.io

package object poll {

  // Resources
  /** `pollable` represents a single I/O event which may be ready, or not. */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:io/poll@0.2.0", "pollable")
  trait Pollable {

    /** Return the readiness of a pollable. This function never blocks.
     *
     *  Returns `true` when the pollable is ready, and `false` otherwise.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("ready")
    def ready(): Boolean = scala.scalajs.wit.native

    /** `block` returns immediately if the pollable is ready, and otherwise
     *  blocks until ready.
     *
     *  This function is equivalent to calling `poll.poll` on a list
     *  containing only this pollable.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("block")
    def block(): Unit = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }

  object Pollable {}

  // Functions
  /** Poll for completion on a set of pollables.
   *
   *  This function takes a list of pollables, which identify I/O sources of
   *  interest, and waits until one or more of the events is ready for I/O.
   *
   *  The result `list<u32>` contains one or more indices of handles in the
   *  argument list that is ready for I/O.
   *
   *  If the list contains more elements than can be indexed with a `u32`
   *  value, this function traps.
   *
   *  A timeout can be implemented by adding a pollable from the
   *  wasi-clocks API to the list.
   *
   *  This function does not return a `result`; polling in itself does not
   *  do any I/O so it doesn't fail. If any of the I/O sources identified by
   *  the pollables has an error, it is indicated by marking the source as
   *  being reaedy for I/O.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:io/poll@0.2.0", "poll")
  def poll(in: Array[Pollable]): Array[scala.scalajs.wit.unsigned.UInt] = scala.scalajs.wit.native

}
