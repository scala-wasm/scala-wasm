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
    out.blockingWriteAndFlush("Hello, Scala!".getBytes()) match {
      case _: Err[_] =>
        component.Err(())
      case _: Ok[_] =>
        component.Ok(())
    }
  }
}