package java.lang.wasi.cli

import scala.scalajs.component.annotation._

import wasi.io.Streams.OutputStream
import scala.scalajs.{component => cm}

/*
 * @since(version = 0.2.0)
 * interface stdout {
 *   @since(version = 0.2.0)
 *   use wasi:io/streams@0.2.5.{output-stream};
 *
 *   @since(version = 0.2.0)
 *   get-stdout: func() -> output-stream;
 * }
 */

object Stdout {
  @ComponentImport("get-stdout", "wasi:cli/stdout@0.2.0")
  def getStdout(): OutputStream = cm.native
}