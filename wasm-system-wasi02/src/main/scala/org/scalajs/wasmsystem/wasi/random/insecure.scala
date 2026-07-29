package org.scalajs.wasmsystem.wasi.random

import scala.scalajs.wit.annotation.WitImport
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.ULong

package object insecure {

  /** Return an insecure pseudo-random `u64` value. */
  @WitImport("wasi:random/insecure@0.2.0", "get-insecure-random-u64")
  def getInsecureRandomU64(): ULong = native

}
