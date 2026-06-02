package example

import scala.scalajs.wasi.random.random

import org.junit.Test

class RuntimeTest {
  @Test def canUseAppWitImportUnderTest(): Unit = {
    random.getRandomU64()
  }
}
