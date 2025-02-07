package helloworld

import scala.scalajs.component
import component.annotation._
import component.unsigned._

@ComponentImport("tanishiking:test/test@0.0.1")
object Test extends component.Interface {
  // func(content: string, width: u32)
  def ferrisSay(content: String, width: UInt): String = component.native
  // (import "tanishiking:test/test@0.0.1", "ferris-say" i32, i32, i32)

  def newCounter(): Counter = component.native

  def getOrigin(): Point = component.native

  @ComponentRecord
  final case class Point(x: Int, y: Int)

}
//   def add(a: UInt, b: UInt): UInt = component.native
//   def say(content: String): Unit = component.native
//   def printNumber(x: Int): Unit = component.native
//   def newCounter(): Counter = component.native
//   def parse(i: Int): Tree = component.native
//   sealed trait Tree extends component.Variant
//   final case class NumValue(val value: Int) extends Tree {
//     type T = Int
//     val _index: Int = 0
//   }
//   final case class FloatValue(val value: Float) extends Tree {
//     type T = Float
//     val _index: Int = 1
//   }
//   final case class StrValue(val value: String) extends Tree {
//     type T = String
//     val _index: Int = 2
//   }
//
//   // [static]counter.new
// }

@component.native
@ComponentImport("tanishiking:test/test@0.0.1")
trait Counter extends component.Resource {
  def up(): Unit = component.native
  // (import "tanishiking:test/test@...", "[method]counter.up")

  def down(): Unit = component.native

  def valueOf(): Int = component.native
}
