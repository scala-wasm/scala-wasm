package componentmodel.wasi.io

package object streams {

  // Type definitions
  type Error = componentmodel.wasi.io.error.Error

  type Pollable = componentmodel.wasi.io.poll.Pollable

  @scala.scalajs.wit.annotation.WitVariant
  sealed trait StreamError

  object StreamError {
    final class LastOperationFailed(val value: Error) extends StreamError {
      override def equals(other: Any): Boolean = other match {
        case that: LastOperationFailed => this.value == that.value
        case _                         => false
      }

      override def hashCode(): Int =
        value.hashCode()

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

  // Resources
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:io/streams@0.2.0", "input-stream")
  final class InputStream private () extends Object {
    @scala.scalajs.wit.annotation.WitResourceMethod("read")
    def read(len: scala.scalajs.wit.unsigned.ULong): scala.scalajs.wit.Result[
        Array[scala.scalajs.wit.unsigned.UByte], StreamError] = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceMethod("blocking-read")
    def blockingRead(len: scala.scalajs.wit.unsigned.ULong): scala.scalajs.wit.Result[
        Array[scala.scalajs.wit.unsigned.UByte], StreamError] = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceMethod("skip")
    def skip(len: scala.scalajs.wit.unsigned.ULong): scala.scalajs.wit.Result[
        scala.scalajs.wit.unsigned.ULong, StreamError] = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceMethod("blocking-skip")
    def blockingSkip(len: scala.scalajs.wit.unsigned.ULong): scala.scalajs.wit.Result[
        scala.scalajs.wit.unsigned.ULong, StreamError] = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceMethod("subscribe")
    def subscribe(): Pollable = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }

  object InputStream {}

  @scala.scalajs.wit.annotation.WitResourceImport("wasi:io/streams@0.2.0", "output-stream")
  final class OutputStream private () extends Object {
    @scala.scalajs.wit.annotation.WitResourceMethod("check-write")
    def checkWrite(): scala.scalajs.wit.Result[scala.scalajs.wit.unsigned.ULong, StreamError] =
      scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceMethod("write")
    def write(contents: Array[scala.scalajs.wit.unsigned.UByte]): scala.scalajs.wit.Result[Unit,
        StreamError] = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceMethod("blocking-write-and-flush")
    def blockingWriteAndFlush(
        contents: Array[scala.scalajs.wit.unsigned.UByte]): scala.scalajs.wit.Result[Unit,
        StreamError] = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceMethod("flush")
    def flush(): scala.scalajs.wit.Result[Unit, StreamError] = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceMethod("blocking-flush")
    def blockingFlush(): scala.scalajs.wit.Result[Unit, StreamError] = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceMethod("subscribe")
    def subscribe(): Pollable = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceMethod("write-zeroes")
    def writeZeroes(
        len: scala.scalajs.wit.unsigned.ULong): scala.scalajs.wit.Result[Unit, StreamError] = {
      scala.scalajs.wit.native
    }

    @scala.scalajs.wit.annotation.WitResourceMethod("blocking-write-zeroes-and-flush")
    def blockingWriteZeroesAndFlush(
        len: scala.scalajs.wit.unsigned.ULong): scala.scalajs.wit.Result[Unit, StreamError] = {
      scala.scalajs.wit.native
    }

    @scala.scalajs.wit.annotation.WitResourceMethod("splice")
    def splice(src: InputStream, len: scala.scalajs.wit.unsigned.ULong): scala.scalajs.wit.Result[
        scala.scalajs.wit.unsigned.ULong, StreamError] = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceMethod("blocking-splice")
    def blockingSplice(src: InputStream,
        len: scala.scalajs.wit.unsigned.ULong): scala.scalajs.wit.Result[
        scala.scalajs.wit.unsigned.ULong, StreamError] = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }

  object OutputStream {}

}
