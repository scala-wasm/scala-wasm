package helloworld

import scala.scalajs.component
import component.annotation._

object Test {
  @ComponentImport("tanishiking:test/addr@1.0.0", "add")
  def add(a: Int, b: Int): Int = component.native
}