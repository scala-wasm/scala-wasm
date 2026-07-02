package wasmcomponent

import org.junit.Assert._
import org.junit.Test

class ComponentTest {
  @Test
  def testRunsInWasmComponent(): Unit =
    assertEquals(42, 40 + 2)
}
