package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._
import scala.scalajs.wit.unsigned._

package object root {

  @WitRecord(WitScope.root, "world-point")
  final class WorldPoint(@WitName("x") val x: UInt, @WitName("y") val y: UInt) {
    override def equals(other: Any): Boolean = other match {
      case that: WorldPoint => this.x == that.x && this.y == that.y
      case _                => false
    }

    override def hashCode(): Int = {
      var result = 1
      result = 31 * result + x.hashCode()
      result = 31 * result + y.hashCode()
      result
    }
  }

  object WorldPoint {
    def apply(x: UInt, y: UInt): WorldPoint = new WorldPoint(x, y)
    def unapply(arg: WorldPoint): Some[(UInt, UInt)] = Some((arg.x, arg.y))
  }

  // World-level functions
  /** world level function imports */
  @WitImport(WitScope.root, "bare-add")
  def bareAdd(@WitName("a") a: Int, @WitName("b") b: Int): Int = wit.native

  @WitImport(WitScope.root, "bare-greet")
  def bareGreet(@WitName("name") name: String): String = wit.native

  @WitImport(WitScope.root, "move-point")
  def movePoint(@WitName("p") p: WorldPoint): WorldPoint = wit.native

}
