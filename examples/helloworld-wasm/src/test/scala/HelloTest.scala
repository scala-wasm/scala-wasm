package helloworld

import org.junit.Test
import org.junit.Assert._

class HelloTest {
  @Test def hello(): Unit =
    assertEquals("Hello World", "Hello" + " " + "World")
}
