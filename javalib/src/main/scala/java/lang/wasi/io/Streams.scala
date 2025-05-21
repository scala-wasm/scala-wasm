package java.lang.wasi.io

import scala.scalajs.{component => cm}
import scala.scalajs.component.annotation._
import scala.scalajs.component.unsigned._

import wasi.io.Error.{Error => WasiIOError}

object Streams {

  /** An output bytestream.
   *
   *  `output-stream`s are *non-blocking* to the extent practical on
   *  underlying platforms. Except where specified otherwise, I/O operations also
   *  always return promptly, after the number of bytes that can be written
   *  promptly, which could even be zero. To wait for the stream to be ready to
   *  accept data, the `subscribe` function to obtain a `pollable` which can be
   *  polled for using `wasi:io/poll`.
   *
   *  Dropping an `output-stream` while there's still an active write in
   *  progress may result in the data being lost. Before dropping the stream,
   *  be sure to fully flush your writes.
   */
  @ComponentResourceImport("output-stream", "wasi:io/streams@0.2.0")
  trait OutputStream {

    /** Perform a write of up to 4096 bytes, and then flush the stream. Block
     *  until all of these operations are complete, or an error occurs.

     *  This is a convenience wrapper around the use of `check-write`,
     *  `subscribe`, `write`, and `flush`, and is implemented with the
     *  following pseudo-code:

     *  ```text
     *  let pollable = this.subscribe();
     *  while !contents.is_empty() {
     *      // Wait for the stream to become writable
     *      pollable.block();
     *      let Ok(n) = this.check-write(); // eliding error handling
     *      let len = min(n, contents.len());
     *      let (chunk, rest) = contents.split_at(len);
     *      this.write(chunk  );            // eliding error handling
     *      contents = rest;
     *  }
     *  this.flush();
     *  // Wait for completion of `flush`
     *  pollable.block();
     *  // Check for any errors that arose during `flush`
     *  let _ = this.check-write();         // eliding error handling
     *  ```
     */
    @ComponentResourceMethod("blocking-write-and-flush")
    def blockingWriteAndFlush(contents: Array[UByte]): cm.Result[Unit, StreamError]

    @ComponentResourceDrop
    def close(): Unit = cm.native
  }

  sealed trait StreamError extends cm.Variant
  object StreamError {
    final class LastOperationFailed(val value: WasiIOError) extends StreamError {
      type T = WasiIOError
      val _index: Int = 0
    }

    final object Closed extends StreamError {
      type T = Unit
      val value = ()
      val _index = 1
    }
  }


}