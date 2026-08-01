package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

object RootImpl {

  @WitExport(WitScope.root, "bare-multiply")
  def bareMultiply(@WitName("a") a: Int, @WitName("b") b: Int): Int = a * b

  @WitExport(WitScope.root, "bare-uppercase")
  def bareUppercase(@WitName("text") text: String): String = text.toUpperCase()

}
