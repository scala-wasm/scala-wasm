package java.lang.wasi.random

import scala.scalajs.component.annotation._
import scala.scalajs.component.unsigned._
import scala.scalajs.{component => cm}

/** The insecure interface for insecure pseudo-random numbers.
 *
 *  It is intended to be portable at least between Unix-family platforms and
 *  Windows.
 */
object Insecure {
  /** Return an insecure pseudo-random `u64` value.
   *
   * This function returns the same type of pseudo-random data as
   * `get-insecure-random-bytes`, represented as a `u64`.
   */
  @ComponentImport("get-insecure-random-u64", "wasi:random/insecure")
  def getInsecureRandomU64(): ULong = cm.native
}