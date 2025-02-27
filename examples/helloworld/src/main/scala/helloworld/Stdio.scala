package helloworld

import scala.scalajs.component
import component.unsigned._
import component.annotation._

// @since(version = 0.2.0)
// interface stdout {
//   @since(version = 0.2.0)
//   use wasi:io/streams@0.2.3.{output-stream};
//
//   @since(version = 0.2.0)
//   get-stdout: func() -> output-stream;
// }
@ComponentImport("wasi:cli/stdout@0.2.0")
object Stdio extends component.Interface {
  def getStdout(): OutputStream = component.native
}

@component.native
@ComponentImport("wasi:io/streams@0.2.0")
trait OutputStream extends component.Resource {
  // @ComponentImport("wasi:io/error@0.2.0", "[method]error.to-debug-string")
  // def toDebugString(): Int = component.native

  // def test(a: Int): Int = component.native

  // @since(version = 0.2.0)
  // blocking-write-and-flush: func(
  //     contents: list<u8>
  // ) -> result<_, stream-error>;
  def blockingWriteAndFlush(contents: Array[UByte]): component.Result[Unit, StreamError]
}

/*
variant stream-error {
    /// The last operation (a write or flush) failed before completion.
    ///
    /// More information is available in the `error` payload.
    ///
    /// After this, the stream will be closed. All future operations return
    /// `stream-error::closed`.
    last-operation-failed(error),
    /// The stream is closed: no more input will be accepted by the
    /// stream. A closed output-stream will return this error on all
    /// future operations.
    closed
}
*/
sealed trait StreamError extends component.Variant
final class LastOperationFailed(val value: Error) extends StreamError {
    type T = Error
    val _index = 0
}
final case object Closed extends StreamError {
    val value = ()
    type T = Unit
    val _index = 1
}

// resource error {
//     /// Returns a string that is suitable to assist humans in debugging
//     /// this error.
//     ///
//     /// WARNING: The returned string should not be consumed mechanically!
//     /// It may change across platforms, hosts, or other implementation
//     /// details. Parsing this string is a major platform-compatibility
//     /// hazard.
//     @since(version = 0.2.0)
//     to-debug-string: func() -> string;
// }
@component.native
@ComponentImport("wasi:io/error@0.2.0")
trait Error extends component.Resource {
  def toDebugString(): String = component.native
}