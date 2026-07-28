package componentmodel.component.testing

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

package object countable {

  // Resources
  @WitResourceImport("component:testing/countable", "counter")
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
    def apply(i: Int): Counter = wit.native

    /** TODO: make a and b borrow<bounter>
     *  otherwise ownership moves, and cannot use a,b afterwards
     */
    @WitResourceStaticMethod("sum")
    def sum(a: Counter, b: Counter): Counter = wit.native
  }

  // Functions
  @WitImport("component:testing/countable", "try-create-counter")
  def tryCreateCounter(value: Int): wit.Result[Counter, String] = wit.native

  @WitImport("component:testing/countable", "maybe-get-counter")
  def maybeGetCounter(): java.util.Optional[Counter] = wit.native

}
