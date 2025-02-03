/* Scala.js example code
 * Public domain
 * @author  Sébastien Doeraene
 */

package helloworld

import scala.scalajs.js
import scala.scalajs.component
import scala.scalajs.component._
import component.annotation._
import component.unsigned._

@ComponentExport("wasi:cli/run@0.2.0")
object Run extends component.Interface {
  def run(): component.Result[Unit, Unit] = {
    val out = Stdio.getStdout()
    // @ComponentImport("tanishiking:test/test@0.0.1")
    // object Test extends component.Interface {
    //   def ferrisSay(content: String, width: UInt): String = component.native
    // }
    val ferris = Test.ferrisSay("Hello Scala!", 80)
    out.blockingWriteAndFlush(ferris.getBytes()) match {
      case _: Err[_] =>
      case _: Ok[_] =>
        new component.Ok(())
    }

    val counter = Test.newCounter()
    counter.up()
    counter.up()
    counter.down()
    val value = counter.valueOf()

    out.blockingWriteAndFlush(Array((value+ 48).toByte))

    new component.Ok(())
  }
}


    /*
    val res = Test.add(-3, 2)
    Test.printNumber(res)

    Test.say("Hello from Scala!")

    val counter = Test.newCounter()
    counter.up()
    counter.up()
    counter.down()
    val value = counter.valueOf()
    Test.printNumber(value) // 1

    Test.parse(100) match {
      case Test.FloatValue(value) =>
      case Test.NumValue(value) => Test.printNumber(value) // 100
      case Test.StrValue(value) => // Test.say(value)
    }

    val out = Stdio.getStdout()
    // hello

    val ferris = Test.ferrisSay("Hello Scala!\n", 80)

    val result = out.blockingWriteAndFlush(ferris.getBytes())
    result match {
      case _: Err[_] =>
        Test.say("err")
      case _: Ok[_] =>
        Test.say("ok")
    }

    new component.Ok(())
    */
