package helloworld

import scala.scalajs.component
import component.annotation._
import component.unsigned._

object Test {
  @ComponentImport("tanishiking:test/test@0.0.1", "add")
  def add(a: Int, b: Int): Int = component.native
}