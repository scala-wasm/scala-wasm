package wasmcomponent

import wasmcomponent.wasi.random.random

object Main {
  def main(args: Array[String]): Unit = {
    val r = random.getRandomU64()
    libio.Println.println("random: " + r)
    libio.Println.println("clock: " + libclock.Clock.now())
  }
}
