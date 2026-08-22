package componentmodel.component.testing

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

package object countable {

  // Resources
  @WitResourceImport(WitScope.unversioned("component", "testing", "countable"), "counter")
  final class Counter private () extends Object {
    @WitResourceMethod("up")
    def up(): Unit = wit.native

    @WitResourceMethod("down")
    def down(): Unit = wit.native

    @WitResourceMethod("value-of")
    def valueOf(): Int = wit.native

    @WitResourceDrop
    def close(): Unit = wit.native
  }

  object Counter {
    @WitResourceConstructor
    def apply(@WitName("i") i: Int): Counter = wit.native

    /** TODO: make a and b borrow<bounter>
     *  otherwise ownership moves, and cannot use a,b afterwards
     */
    @WitResourceStaticMethod("sum")
    def sum(@WitName("a") a: Counter, @WitName("b") b: Counter): Counter = wit.native
  }

  // Functions
  @WitImport(WitScope.unversioned("component", "testing", "countable"), "try-create-counter")
  def tryCreateCounter(@WitName("value") value: Int): wit.Result[Counter, String] = wit.native

  @WitImport(WitScope.unversioned("component", "testing", "countable"), "maybe-get-counter")
  def maybeGetCounter(): java.util.Optional[Counter] = wit.native

}
