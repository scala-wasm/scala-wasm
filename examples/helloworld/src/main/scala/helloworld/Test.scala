package helloworld

import scala.scalajs.component
import component.annotation._
import component.unsigned._

object Test {
  @ComponentImport("tanishiking:test/test@0.0.1", "add")
  def add(a: Int, b: Int): Int = component.native

  @ComponentImport("tanishiking:test/test@0.0.1", "say")
  def say(content: String): Unit = component.native

  @ComponentImport("tanishiking:test/test@0.0.1", "new-counter")
  def newCounter(): Counter = component.native

  @component.native
  trait Counter extends component.Resource {
    @ComponentImport("tanishiking:test/test@0.0.1", "[method]counter.up")
    def up(): Unit = component.native

    @ComponentImport("tanishiking:test/test@0.0.1", "[method]counter.down")
    def down(): Unit = component.native

    @ComponentImport("tanishiking:test/test@0.0.1", "[method]counter.value-of")
    def valueOf(): Int = component.native
  }

  // [static]counter.new
}