/* Scala.js example code
 * Public domain
 * @author  Sébastien Doeraene
 */

package helloworld

import scala.scalajs.js
import scala.scalajs.component
import component.annotation._

object Cli {
  @ComponentExport("wasi:cli/run@0.2.2#run")
  def run(): component.Result[Unit, Unit] = {
    Test.add(0, 2)
    component.Ok(())
  }
}