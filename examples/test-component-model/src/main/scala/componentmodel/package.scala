package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._
import scala.scalajs.wit.unsigned._

package object root {

  @WitRecord(WitScope.root, "world-point")
  final case class WorldPoint(@WitName("x") x: UInt, @WitName("y") y: UInt)

  // World-level functions
  /** world level function imports */
  @WitImport(WitScope.root, "bare-add")
  def bareAdd(@WitName("a") a: Int, @WitName("b") b: Int): Int = wit.native

  @WitImport(WitScope.root, "bare-greet")
  def bareGreet(@WitName("name") name: String): String = wit.native

  @WitImport(WitScope.root, "move-point")
  def movePoint(@WitName("p") p: WorldPoint): WorldPoint = wit.native

}
