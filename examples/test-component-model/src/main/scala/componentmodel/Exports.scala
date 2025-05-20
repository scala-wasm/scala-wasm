package componentmodel

import scala.scalajs.js
import scala.scalajs.{component => cm}
import scala.scalajs.component._
import scala.scalajs.component.annotation._
import scala.scalajs.component.unsigned._

import java.util.Optional

object TestsExport {
  @ComponentExport("roundtrip-basics1", "component:testing/tests")
  def roundtripBasics1(a: (UByte, Byte, UShort, Short, UInt, Int, Float, Double, Char)):
      (UByte, Byte, UShort, Short, UInt, Int, Float, Double, Char) = a

  @ComponentExport("roundtrip-list-u16", "component:testing/tests")
  def roundtripListU16(a: Array[UShort]): Array[UShort] = a

  @ComponentExport("roundtrip-list-point", "component:testing/tests")
  def roundtripListPoint(a: Array[Point]): Array[Point] = a

  @ComponentExport("roundtrip-list-variant", "component:testing/tests")
  def roundtripListVariant(a: Array[C1]): Array[C1] = a

  @ComponentExport("roundtrip-string", "component:testing/tests")
  def roundtripString(a: String): String = a

  @ComponentExport("roundtrip-point", "component:testing/tests")
  def roundtripPoint(a: Point): Point = a

  @ComponentExport("roundtrip-c1", "component:testing/tests")
  def roundtripC1(a: C1): C1 = a

  @ComponentExport("roundtrip-z1", "component:testing/tests")
  def roundtripZ1(a: Z1): Z1 = a

  @ComponentExport("test-c1", "component:testing/tests")
  def testC1(a: C1): Unit = {}

  @ComponentExport("roundtrip-enum", "component:testing/tests")
  def roundtripEnum(a: E1): E1 = a

  @ComponentExport("roundtrip-tuple", "component:testing/tests")
  def roundtripTuple(a: (C1, Z1)): (C1, Z1) = a

  @ComponentExport("roundtrip-option", "component:testing/tests")
  def roundtripOption(a: Optional[String]): Optional[String] = a

  @ComponentExport("roundtrip-double-option", "component:testing/tests")
  def roundtripDoubleOption(a: Optional[Optional[String]]): Optional[Optional[String]] = a

  @ComponentExport("roundtrip-result", "component:testing/tests")
  def roundtripResult(a: cm.Result[Unit, Unit]): cm.Result[Unit, Unit] = a

  @ComponentExport("roundtrip-string-error", "component:testing/tests")
  def roundtripStringError(a: cm.Result[Float, String]): cm.Result[Float, String] = a

  @ComponentExport("roundtrip-enum-error", "component:testing/tests")
  def roundtripEnumError(a: cm.Result[C1, E1]): cm.Result[C1, E1] = a

  import TestImportsHelper._
  @ComponentExport("roundtrip-f8", "component:testing/tests")
  def roundtripF8(a: F1): F1 = a

  @ComponentExport("roundtrip-f16", "component:testing/tests")
  def roundtripF16(a: F2): F2 = a

  @ComponentExport("roundtrip-f32", "component:testing/tests")
  def roundtripF32(a: F3): F3 = a

  @ComponentExport("roundtrip-flags", "component:testing/tests")
  def roundtripFlags(a: (F1, F1)): (F1, F1) = a
}

object TestImports {
  @ComponentExport("run", "component:testing/test-imports")
  def run(): Unit = {
    import Basics._
    import Tests._
    import Countable._
    assert(1 == roundtripU8(1))
    assert(0 == roundtripS8(0))
    assert(0 == roundtripU16(0))
    assert(128 == roundtripS16(128))
    assert(0 == roundtripU32(0))
    assert(30 == roundtripS32(30))
    assert(30 == roundtripU64(30))
    assert(30 == roundtripS64(30))

    assert(0.0f == roundtripF32(0.0f))
    assert(0.0 == roundtripF64(0.0))

    assert('a' == roundtripChar('a'))

    assert(
      (127, 127, 32767, 32767, 532423, 2147483647, 0.0f, 0.0, 'x') ==
      roundtripBasics1((127, 127, 32767, 32767, 532423, 2147483647, 0.0f, 0.0, 'x'))
    )

    val arr = Array[UShort](0, 1, 2)
    assert(arr.sameElements(roundtripListU16(arr)))

    val arr2 = Array[Point](Point(0, 0), Point(3, 0))
    assert(arr2.sameElements(roundtripListPoint(arr2)))

    val arr3 = Array[C1](C1.A(0), C1.B(3))
    assert(arr3.sameElements(roundtripListVariant(arr3)))

    assert("foo" == roundtripString("foo"))
    assert("" == roundtripString(""))
    assert(Point(0, 5) == roundtripPoint(Point(0, 5)))

    testC1(C1.A(5))
    assert(C1.A(4) == roundtripC1(C1.A(4)))
    assert(C1.B(0.0f) == roundtripC1(C1.B(0.0f)))
    assert(Z1.A(0) == roundtripZ1(Z1.A(0)))
    assert(Z1.A(100) == roundtripZ1(Z1.A(100)))
    assert(Z1.B == roundtripZ1(Z1.B))

    assert(E1.A == roundtripEnum(E1.A))
    assert(E1.B == roundtripEnum(E1.B))
    assert(E1.C == roundtripEnum(E1.C))

    assert((C1.A(5), Z1.A(500)) == roundtripTuple((C1.A(5), Z1.A(500))))
    assert((C1.B(200.0f), Z1.B) == roundtripTuple((C1.B(200.0f), Z1.B)))
    assert(C1.A(4) == roundtripTuple(C1.A(4), Z1.B)._1)
    assert(Z1.B == roundtripTuple(C1.A(4), Z1.B)._2)

    assert(Optional.of("ok") == roundtripOption(Optional.of("ok")))
    assert(Optional.empty == roundtripOption(Optional.empty[String]))
    assert(Optional.of(Optional.of("foo")) == roundtripDoubleOption(Optional.of(Optional.of("foo"))))
    assert(Optional.of(Optional.empty) == roundtripDoubleOption(Optional.of(Optional.empty[String])))
    assert(Optional.empty == roundtripDoubleOption(Optional.empty[Optional[String]]))
    // assert(new cm.Err("aaa") != new cm.Err("bbb"))

    // assert(new cm.Ok(()) == roundtripResult(new cm.Ok(())))
    // assert(new cm.Err(()) == roundtripResult(new cm.Err(())))
    // assert(new cm.Ok(3.0f) == roundtripStringError(new cm.Ok(3.0f)))
    // assert(new cm.Err("err") == roundtripStringError(new cm.Err("err")))
    // assert(new cm.Ok(C1.A(432)) == roundtripEnumError(new cm.Ok(C1.A(432))))
    // assert(new cm.Ok(C1.B(0.0f)) == roundtripEnumError(new cm.Ok(C1.B(0.0f))))
    // assert(new cm.Err(E1.A) == roundtripEnumError(new cm.Err(E1.A)))
    // assert(new cm.Err(E1.B) == roundtripEnumError(new cm.Err(E1.B)))
    // assert(new cm.Err(E1.C) == roundtripEnumError(new cm.Err(E1.C)))

    import TestImportsHelper._
    assert((F1.b3 | F1.b6 | F1.b7) == roundtripF8(F1.b3 | F1.b6 | F1.b7))

    assert(
      (F1.b3 | F1.b6, F1.b2 | F1.b3 | F1.b7) ==
      roundtripFlags((F1.b3 | F1.b6, F1.b2 | F1.b3 | F1.b7))
    )

    locally {
      val c1 = Counter(0)
      c1.up()
      assert(1 == c1.valueOf())

      val c2 = Counter(100)
      c2.down()
      assert(99 == c2.valueOf())

      // val s1 = Counter.sum(c1, c2)
      // val s2 = Counter.sum(c1, c2)
      // assert(s1.valueOf() == s2.valueOf())
      // assert(100 == s.valueOf())

      // use c1 multiple times fails with
      // unknown handle index 1 (?)
      assert(100 == Counter.sum(c1, c2).valueOf())
    }
  }
}

object TestImportsHelper {
  @ComponentFlags(8)
  type F1 = Int
  object F1 {
    val b0 = 1 << 0
    val b1 = 1 << 1
    val b2 = 1 << 2
    val b3 = 1 << 3
    val b4 = 1 << 4
    val b5 = 1 << 5
    val b6 = 1 << 6
    val b7 = 1 << 7
  }

  @ComponentFlags(16)
  type F2 = Int
  object F2 {
    val b0  = 1 << 0
    val b1  = 1 << 1
    val b2  = 1 << 2
    val b3  = 1 << 3
    val b4  = 1 << 4
    val b5  = 1 << 5
    val b6  = 1 << 6
    val b7  = 1 << 7
    val b8  = 1 << 8
    val b9  = 1 << 9
    val b10 = 1 << 10
    val b11 = 1 << 11
    val b12 = 1 << 12
    val b13 = 1 << 13
    val b14 = 1 << 14
    val b15 = 1 << 15
  }

  @ComponentFlags(32)
  type F3 = Int
  object F3 {
    val b0  = 1 << 0
    val b1  = 1 << 1
    val b2  = 1 << 2
    val b3  = 1 << 3
    val b4  = 1 << 4
    val b5  = 1 << 5
    val b6  = 1 << 6
    val b7  = 1 << 7
    val b8  = 1 << 8
    val b9  = 1 << 9
    val b10 = 1 << 10
    val b11 = 1 << 11
    val b12 = 1 << 12
    val b13 = 1 << 13
    val b14 = 1 << 14
    val b15 = 1 << 15
    val b16 = 1 << 16
    val b17 = 1 << 17
    val b18 = 1 << 18
    val b19 = 1 << 19
    val b20 = 1 << 20
    val b21 = 1 << 21
    val b22 = 1 << 22
    val b23 = 1 << 23
    val b24 = 1 << 24
    val b25 = 1 << 25
    val b26 = 1 << 26
    val b27 = 1 << 27
    val b28 = 1 << 28
    val b29 = 1 << 29
    val b30 = 1 << 30
    val b31 = 1 << 31
  }

}

@ComponentRecord
final case class Point(x: Int, y: Int)

sealed trait C1 extends Variant
object C1 {
  final case class A(value: Int) extends C1 {
    type T = Int
    val _index = 0
  }
  final case class B(value: Float) extends C1 {
    type T = Float
    val _index = 1
  }
}

sealed trait Z1 extends Variant
object Z1 {
  final case class A(value: Int) extends Z1 {
    type T = Int
    val _index = 0
  }
  // if the field is typed Unit, there's no fields generated in SJSIR
  // there's only a getter
  final case object B extends Z1 {
    type T = Unit
    val value = ()
    val _index = 1
  }
}

sealed trait E1 extends cm.Enum
object E1 {
  final case object A extends E1 {
    val _index = 0
  }
  final case object B extends E1 {
    val _index = 1
  }
  final case object C extends E1 {
    val _index = 2
  }
}