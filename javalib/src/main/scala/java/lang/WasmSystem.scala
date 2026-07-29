package java.lang

// TODO: Remove WasmSystem, this is only for MinimalWasmModule
// but we shouldn't special handle them in emitter.
protected[lang] object WasmSystem {
  @noinline def print(s: String): Unit = throw new AssertionError("stub")
  @noinline def nanoTime(): scala.Long = throw new AssertionError("stub")
  @noinline def currentTimeMillis(): scala.Long = throw new AssertionError("stub")
  @noinline def random(): scala.Double = throw new AssertionError("stub")
}
