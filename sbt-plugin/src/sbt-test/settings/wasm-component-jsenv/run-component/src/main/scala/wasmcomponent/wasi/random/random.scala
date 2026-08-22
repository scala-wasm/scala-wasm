package wasmcomponent.wasi.random

import scala.scalajs.wit.annotation.{WitImport, WitScope}
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.ULong

package object random {

  @WitImport(WitScope("wasi", "random", "random", "0.2.0"), "get-random-u64")
  def getRandomU64(): ULong = native

}
