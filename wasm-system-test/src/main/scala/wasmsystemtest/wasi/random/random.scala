package wasmsystemtest.wasi.random

import scala.scalajs.wit.annotation.{WitImport, WitName, WitScope}
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.{UByte, ULong}

package object random {

  @WitImport(WitScope("wasi", "random", "random", "0.2.0"), "get-random-bytes")
  def getRandomBytes(@WitName("len") len: ULong): Array[UByte] = native

  @WitImport(WitScope("wasi", "random", "random", "0.2.0"), "get-random-u64")
  def getRandomU64(): ULong = native

}
