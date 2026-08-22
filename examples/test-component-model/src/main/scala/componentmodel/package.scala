package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

package object root {

  // World-level functions
  /** world level function imports */
  @WitImport(WitScope.root, "bare-add")
  def bareAdd(@WitName("a") a: Int, @WitName("b") b: Int): Int = wit.native

  @WitImport(WitScope.root, "bare-greet")
  def bareGreet(@WitName("name") name: String): String = wit.native

}
