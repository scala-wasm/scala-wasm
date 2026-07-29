package scala.scalajs.wasi.io

import scala.scalajs.wasi.io.error.Error
import scala.scalajs.wasi.io.poll.Pollable
import scala.scalajs.wit.Result
import scala.scalajs.wit.annotation.{
  WitResourceDrop,
  WitResourceImport,
  WitResourceMethod,
  WitVariant
}
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.{UByte, ULong}

package object streams {

  @WitVariant
  sealed trait StreamError

  object StreamError {
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

    object Closed extends StreamError {
      override def toString(): String = "Closed"
    }
  }

  @WitResourceImport("wasi:io/streams@0.2.0", "input-stream")
  final class InputStream private () extends Object {
    @WitResourceMethod("read")
    def read(len: ULong): Result[Array[UByte], StreamError] = native

    @WitResourceMethod("blocking-read")
    def blockingRead(len: ULong): Result[Array[UByte], StreamError] = native

    @WitResourceMethod("skip")
    def skip(len: ULong): Result[ULong, StreamError] = native

    @WitResourceMethod("blocking-skip")
    def blockingSkip(len: ULong): Result[ULong, StreamError] = native

    @WitResourceMethod("subscribe")
    def subscribe(): Pollable = native

    @WitResourceDrop
    def close(): Unit = native
  }

  object InputStream {}

  @WitResourceImport("wasi:io/streams@0.2.0", "output-stream")
  final class OutputStream private () extends Object {
    @WitResourceMethod("check-write")
    def checkWrite(): Result[ULong, StreamError] = native

    @WitResourceMethod("write")
    def write(contents: Array[UByte]): Result[Unit, StreamError] = native

    @WitResourceMethod("blocking-write-and-flush")
    def blockingWriteAndFlush(contents: Array[UByte]): Result[Unit, StreamError] = native

    @WitResourceMethod("flush")
    def flush(): Result[Unit, StreamError] = native

    @WitResourceMethod("blocking-flush")
    def blockingFlush(): Result[Unit, StreamError] = native

    @WitResourceMethod("subscribe")
    def subscribe(): Pollable = native

    @WitResourceMethod("write-zeroes")
    def writeZeroes(len: ULong): Result[Unit, StreamError] = native

    @WitResourceMethod("blocking-write-zeroes-and-flush")
    def blockingWriteZeroesAndFlush(len: ULong): Result[Unit, StreamError] = native

    @WitResourceMethod("splice")
    def splice(src: InputStream, len: ULong): Result[ULong, StreamError] = native

    @WitResourceMethod("blocking-splice")
    def blockingSplice(src: InputStream, len: ULong): Result[ULong, StreamError] = native

    @WitResourceDrop
    def close(): Unit = native
  }

  object OutputStream {}

}
