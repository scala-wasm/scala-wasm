package wasmcomponent

import org.junit.Assert._
import org.junit.Test

class ComponentTest {
  @Test
  def testBasic(): Unit =
    assertEquals(42, 40 + 2)

  @Test
  def canUseRandomPrint(): Unit = {
    val r = wasmcomponent.wasi.random.random.getRandomU64()
    libio.Println.println("test random: " + r)
  }

  @Test
  def canUseClock(): Unit = {
    val t1 = libclock.Clock.now()
    val t2 = libclock.Clock.now()
    libio.Println.println("test clock: " + t2)
    assertTrue("monotonic clock", t2 >= t1)
  }
}
