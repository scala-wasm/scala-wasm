/* Scala.js example code
 * Public domain
 * @author  Sébastien Doeraene
 */

package helloworld

import scala.scalajs.js
import scala.scalajs.component
import component.annotation._

/*
object helloworld {
  def main(args: Array[String]): Unit = {
    val res = Test.add(1, 2)
    val f = foo(res)
    println(f.x)
  }

  private def foo(x: Int): Base = {
    if (x < 0) A(x)
    else B(x)
  }
  sealed trait Base {
    def x: Int
  }
  case class A(x: Int) extends Base
  case class B(x: Int) extends Base
}
*/

object Cli {
  @ComponentExport("wasi:cli/run@0.2.0#run")
  def run(): component.Result[Unit, Unit] = {
    val res = Test.add(1, 2)
    Test.say("foo")
    // val err = component.Err(())

    val counter = Test.newCounter()
    counter.up()
    counter.up()
    counter.down()
    val value = counter.valueOf()
    Test.add(value, value) // 2

    // val out = Stdio.getStdout()
    // out.test(0)
    component.Ok(())
  }

}