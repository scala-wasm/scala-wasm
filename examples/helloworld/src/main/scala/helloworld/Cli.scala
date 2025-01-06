/* Scala.js example code
 * Public domain
 * @author  Sébastien Doeraene
 */

package helloworld

import scala.scalajs.js
import scala.scalajs.component
import component.annotation._

object Cli {
  @ComponentExport("wasi:cli/run@0.2.0#run")
  def run(): component.Result[Unit, Unit] = {
    val res = Test.add(1, 2)
    component.Ok(())
  }
}