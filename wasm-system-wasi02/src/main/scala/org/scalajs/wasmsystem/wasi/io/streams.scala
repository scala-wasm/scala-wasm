package org.scalajs.wasmsystem.wasi.io

import org.scalajs.wasmsystem.wasi.io.error.Error
import scala.scalajs.wit.Result
import scala.scalajs.wit.annotation.{
  WitName,
  WitResourceDrop,
  WitResourceImport,
  WitResourceMethod,
  WitScope,
  WitVariant
}
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.UByte

package object streams {

  @WitVariant(WitScope("wasi", "io", "streams", "0.2.0"), "stream-error")
  sealed trait StreamError

  object StreamError {
    @WitName("last-operation-failed")
    final class LastOperationFailed(val value: Error) extends StreamError {
      override def equals(other: Any): Boolean = other match {
        case that: LastOperationFailed => this.value == that.value
        case _                         => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "LastOperationFailed(" + value + ")"
    }

    object LastOperationFailed {
      def apply(value: Error): LastOperationFailed = new LastOperationFailed(value)
      def unapply(arg: LastOperationFailed): Some[Error] = Some(arg.value)
    }

    @WitName("closed")
    object Closed extends StreamError {
      override def toString(): String = "Closed"
    }
  }

  @WitResourceImport(WitScope("wasi", "io", "streams", "0.2.0"), "output-stream")
  final class OutputStream private () extends Object {
    @WitResourceMethod("blocking-write-and-flush")
    def blockingWriteAndFlush(
        @WitName("contents") contents: Array[UByte]): Result[Unit, StreamError] = native

    @WitResourceDrop
    def close(): Unit = native
  }

  object OutputStream {}

}
