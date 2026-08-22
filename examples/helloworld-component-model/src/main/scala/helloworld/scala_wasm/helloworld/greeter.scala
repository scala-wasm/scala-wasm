package helloworld.scala_wasm.helloworld

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

package object greeter {

  // Functions
  @WitImport(WitScope.unversioned("scala-wasm", "helloworld", "greeter"), "greet")
  def greet(@WitName("name") name: String): String = wit.native

}
