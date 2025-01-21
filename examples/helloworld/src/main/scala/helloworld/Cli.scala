/* Scala.js example code
 * Public domain
 * @author  Sébastien Doeraene
 */

package helloworld

import scala.scalajs.js
import scala.scalajs.component
import component.annotation._
import component.unsigned._
import java.nio.charset.StandardCharsets
// import helloworld.Test._

/*
object helloworld {
  def main(args: Array[String]): Unit = {
    println("hello")
  }
}

object Foo {
  @js.annotation.JSExportTopLevel("foo")
  def foo(): Unit = {
    Hoge.hoge() match {
      case Foo(x) => println(x)
      case Bar(x) => println(x)
      case Baz(x) => println(x)
    }
    Test.parse(100) match {
      case Test.FloatValue(value) =>
      case Test.NumValue(value) => Test.add(value, value) // 200
      case Test.StrValue(value) =>
    }
  }
  sealed trait Hoge
  case class Foo(x: Int) extends Hoge
  case class Bar(x: String) extends Hoge
  case class Baz(x: Float) extends Hoge

  object Hoge {
    def hoge(): Hoge = {
      Foo(1)
    }
  }
}
  */

@ComponentExport("wasi:cli/run@0.2.0")
object Run extends component.Interface {
  def run(): component.Result[Unit, Unit] = {
    // val res = Test.add(1, 2)
    // Test.printNumber(res)

    // Test.say("Hello from Scala!")
    // // val err = component.Err(())

    // val counter = Test.newCounter()
    // counter.up()
    // counter.up()
    // counter.down()
    // val value = counter.valueOf()
    // Test.printNumber(value) // 1

    Test.parse(0) match {
      case Test.FloatValue(value) =>
      case Test.NumValue(value) => Test.printNumber(value) // 100
      case Test.StrValue(value) => Test.say(value)
    }

    // val out = Stdio.getStdout()
    // // hello
    // val hello: Array[UByte] = new Array[UByte](5)
    // hello.update(0, new UByte(104))
    // hello.update(0, new UByte(101))
    // hello.update(0, new UByte(108))
    // hello.update(0, new UByte(108))
    // hello.update(0, new UByte(111))
    //  Array[Byte](104, 101, 108, 108, 111)

    // out.test(0)
    // out.blockingWriteAndFlush(hello)


    /*
    Hoge.hoge() match {
      case Foo(x) => println(x)
      case Bar(x) => println(x)
      case Baz(x) => println(x)
    }
    */
    component.Ok(())
  }

}

sealed trait Hoge
case class Foo(x: Int) extends Hoge
case class Bar(x: String) extends Hoge
case class Baz(x: Float) extends Hoge

object Hoge {
  def hoge(): Hoge = {
    Foo(1)
  }
}
