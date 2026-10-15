package componentmodel.component.testing

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

package object aliases {

  // Type definitions
  @WitAlias(WitScope.unversioned("component", "testing", "aliases"), "metadata")
  type Metadata = Array[wit.Tuple2[String, String]]

}
