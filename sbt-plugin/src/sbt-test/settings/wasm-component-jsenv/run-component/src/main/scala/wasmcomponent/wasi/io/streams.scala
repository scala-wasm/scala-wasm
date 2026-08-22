package wasmcomponent.wasi.io

package object streams {

  // Type definitions
  type Error = wasmcomponent.wasi.io.error.Error

  type Pollable = wasmcomponent.wasi.io.poll.Pollable

  @scala.scalajs.wit.annotation.WitVariant(
      scala.scalajs.wit.annotation.WitScope("wasi", "io", "streams", "0.2.0"),
      "stream-error")
  sealed trait StreamError
  object StreamError {
    @scala.scalajs.wit.annotation.WitName("last-operation-failed")
    final class LastOperationFailed(
        @scala.scalajs.wit.annotation.WitName("value") val value: Error)
        extends StreamError {
      override def equals(other: Any): Boolean = other match {
        case that: LastOperationFailed => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "LastOperationFailed(" + value + ")"
    }
    object LastOperationFailed {
      def apply(value: Error): LastOperationFailed = new LastOperationFailed(value)
      def unapply(arg: LastOperationFailed): Some[Error] = Some(arg.value)
    }
    @scala.scalajs.wit.annotation.WitName("closed")
    object Closed extends StreamError {
      override def toString(): String = "Closed"
    }
  }

  // Resources
  @scala.scalajs.wit.annotation.WitResourceImport(
      scala.scalajs.wit.annotation.WitScope("wasi", "io", "streams", "0.2.0"),
      "output-stream")
  final class OutputStream private () extends Object {
    @scala.scalajs.wit.annotation.WitResourceMethod("blocking-write-and-flush")
    def blockingWriteAndFlush(
        @scala.scalajs.wit.annotation.WitName("contents")
        contents: Array[scala.scalajs.wit.unsigned.UByte]): scala.scalajs.wit.Result[Unit, StreamError] =
      scala.scalajs.wit.native
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }
  object OutputStream {
  }

}
