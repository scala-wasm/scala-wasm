package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

object RootImpl {

  @WitExport("", "bare-multiply")
  def bareMultiply(a: Int, b: Int): Int = a * b

  @WitExport("", "bare-uppercase")
  def bareUppercase(text: String): String = text.toUpperCase()

}
