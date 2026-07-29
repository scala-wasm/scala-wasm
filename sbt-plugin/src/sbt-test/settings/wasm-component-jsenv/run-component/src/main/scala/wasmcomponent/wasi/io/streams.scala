package wasmcomponent.wasi.io

import wasmcomponent.wasi.io.error.Error
import wasmcomponent.wasi.io.poll.Pollable
import scala.scalajs.wit.Result
import scala.scalajs.wit.annotation.{WitResourceDrop, WitResourceImport, WitResourceMethod, WitVariant}
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.UByte

package object streams {

  @WitVariant
  sealed trait StreamError
  object StreamError {
    final class LastOperationFailed(val value: Error) extends StreamError {
      override def equals(other: Any): Boolean = other match {
        case that: LastOperationFailed => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "LastOperationFailed(" + value + ")"
    }
    object LastOperationFailed {
      def apply(value: Error): LastOperationFailed = new LastOperationFailed(value)
      def unapply(arg: LastOperationFailed): Some[Error] = Some(arg.value)
    }
    object Closed extends StreamError { override def toString(): String = "Closed" }
  }

  @WitResourceImport("wasi:io/streams@0.2.0", "output-stream")
  final class OutputStream private () extends Object {
    @WitResourceMethod("blocking-write-and-flush")
    def blockingWriteAndFlush(contents: Array[UByte]): Result[Unit, StreamError] = native
    @WitResourceDrop
    def close(): Unit = native
  }
  object OutputStream { }

}
