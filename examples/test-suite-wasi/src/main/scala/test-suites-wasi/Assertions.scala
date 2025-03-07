package testSuiteWASI

object Assertions {
  def fail(msg: String): Unit = assert(false, msg)

  def assertTrue(assertion: Boolean): Unit =
    assert(assertion)
  def assertTrue(msg: String, assertion: Boolean): Unit =
    assert(assertion, msg)
  def assertFalse(assertion: Boolean): Unit =
    assert(!assertion)
  def assertFalse(msg: String, assertion: Boolean): Unit =
    assert(!assertion, msg)

  def assertEquals(msg: String, a: Any, b: Any): Unit =
    assert(a == b, msg)
  def assertEquals(a: Any, b: Any): Unit =
    assert(a == b)
  def assertNotEquals(msg: String, a: Any, b: Any): Unit =
    assert(a != b, msg)
  def assertNotEquals(a: Any, b: Any): Unit =
    assert(a != b)

  def assertSame(msg: String, a: Any, b: Any): Unit =
    assert(a.asInstanceOf[AnyRef] eq b.asInstanceOf[AnyRef], msg)
  def assertSame(a: Any, b: Any): Unit =
    assert(a.asInstanceOf[AnyRef] eq b.asInstanceOf[AnyRef])
  def assertNotSame(a: Any, b: Any): Unit =
    assert(a.asInstanceOf[AnyRef] ne b.asInstanceOf[AnyRef])
  def assertNotSame(msg: String, a: Any, b: Any): Unit =
    assert(a.asInstanceOf[AnyRef] ne b.asInstanceOf[AnyRef], msg)

  def assertNotNull(a: AnyRef): Unit =
    assert(a != null)
  def assertNull(a: AnyRef): Unit =
    assert(a == null)

  // TODO
  def assertThrows[T <: Throwable, U](expectedThrowable: Class[T], code: => U): Unit = ()
  def assertThrowsNPEIfCompliant[U](code: => U): Unit = ()

}