package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

package object root {

  // World-level functions
  /** world level function imports */
  @WitImport("$root", "bare-add")
  def bareAdd(a: Int, b: Int): Int = wit.native

  @WitImport("$root", "bare-greet")
  def bareGreet(name: String): String = wit.native

}
