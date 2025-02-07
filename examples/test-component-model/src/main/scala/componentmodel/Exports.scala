package componentmodel

import scala.scalajs.js
import scala.scalajs.component
import scala.scalajs.component._
import component.annotation._
import component.unsigned._

@ComponentExport("component:testing/tests")
object TestsExport extends component.Interface {
  def roundtripString(a: String): String = a
  def roundtripPoint(a: Point): Point = a
}

@ComponentExport("component:testing/test-imports")
object TestImports extends component.Interface {
  def run(): Unit = {
    import Basics._
    import Tests._

    assert(roundtripU8(1) == 1)
    assert(roundtripS8(0) == 0)
    assert(roundtripU16(0) == 0)
    assert(roundtripS16(128) == 128)
    assert(roundtripU32(0) == 0)
    assert(roundtripS32(30) == 30)
    // assert(roundtripU64(30) == 30)
    // assert(roundtripS64(30) == 30)

    assert(roundtripF32(0.0f) == 0.0f)
    assert(roundtripF64(0.0) == 0.0)

    assert(roundtripChar('a') == 'a')

    assert(roundtripString("foo") == "foo")
    assert(roundtripString("") == "")
    assert(roundtripPoint(Point(0, 5)) == Point(0, 5))
  }
}

@ComponentRecord
final case class Point(x: Int, y: Int)
