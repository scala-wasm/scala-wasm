package testSuiteWASI

object Run {
  def main(args: Array[String]): Unit = {
    CompilerTest.run()
    JavalibLangTest.run()
    JavalibUtilTest.run()
    LibraryTest.run()

    println("Test suite completed")
  }
}
