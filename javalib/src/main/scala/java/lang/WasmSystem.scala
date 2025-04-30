package java.lang

protected[lang] object WasmSystem {
  @noinline
  def print(s: String): Unit = ()

  @noinline
  def nanoTime(): scala.Long = 0L

  @noinline
  def currentTimeMillis(): scala.Long = 0L

  @noinline
  def random(): scala.Double = 0.0
}
