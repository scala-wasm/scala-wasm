package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

import componentmodel.exports.Root

@WitImplementation
object RootImpl extends Root {

  override def bareMultiply(a: Int, b: Int): Int = a * b

  override def bareUppercase(text: String): String = text.toUpperCase()

}
