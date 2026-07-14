package liba

object Random {
  def next(): scala.scalajs.wit.unsigned.ULong =
    liba.wasi.random.random.getRandomU64()
}
