package org.scalajs.wasmlibintf

/** No-op `WasmSystem` so JUnit suite can link as a WasmComponent.
 *  TODO: just depends on wasmsystemWasi02.
 */
object WasmSystem {
  def print(s: String, isErr: Boolean): Unit = ()
  def nanoTime(): Long = 0L
  def currentTimeMillis(): Long = 0L
  def random(): Double = 0.0
}
