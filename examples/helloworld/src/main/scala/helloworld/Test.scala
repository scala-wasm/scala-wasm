package helloworld

import scala.scalajs.component
import component.annotation._
import component.unsigned._

object Test {
  @ComponentImport("tanishiking:test/test@0.0.1", "add")
  def add(a: Int, b: Int): Int = component.native

  @ComponentImport("tanishiking:test/test@0.0.1", "say")
  def say(content: String): Unit = component.native

  @ComponentImport("tanishiking:test/test@0.0.1", "print-number")
  def printNumber(x: Int): Unit = component.native

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

  @ComponentImport("tanishiking:test/test@0.0.1", "parse")
  def parse(i: Int): Tree = component.native

  sealed trait Tree extends component.Variant
  final case class NumValue(val value: Int) extends Tree {
    type T = Int
    val _index: Int = 0
  }
  final case class FloatValue(val value: Float) extends Tree {
    type T = Float
    val _index: Int = 1
  }
  final case class StrValue(val value: String) extends Tree {
    type T = String
    val _index: Int = 2
  }

  // [static]counter.new
}