package testSuiteWASI

import library._

object LibraryTest {
  def run(): Unit = {
    locally {
      val test = new ReflectTest
      import test._
      testClassRuntimeClass()
    }
  }
}