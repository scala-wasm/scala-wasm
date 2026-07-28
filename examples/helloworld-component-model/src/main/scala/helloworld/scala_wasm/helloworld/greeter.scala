package helloworld.scala_wasm.helloworld

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

package object greeter {

  // Functions
  @WitImport("scala-wasm:helloworld/greeter", "greet")
  def greet(name: String): String = wit.native

}
