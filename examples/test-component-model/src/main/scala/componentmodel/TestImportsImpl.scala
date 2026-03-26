package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._
import scala.scalajs.wit.unsigned._

import componentmodel.component.testing.basics._
import componentmodel.component.testing.tests._
import componentmodel.component.testing.countable._
import componentmodel.root._
import componentmodel.exports.component.testing.TestImports

import scala.scalajs.WitUtils.toEither

import java.util.Optional

@WitImplementation
object TestImportsImpl extends TestImports {
  override def run(): Unit = {

    val start = System.currentTimeMillis()

    // Test world-level function imports
    assert(5 == bareAdd(2, 3))
    assert(10 == bareAdd(7, 3))
    assert("Hello, World!" == bareGreet("World"))
    assert("Hello, Scala!" == bareGreet("Scala"))

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

    val basics1Input = (127.asInstanceOf[UByte], 127.toByte, 32767.asInstanceOf[UShort],
        32767.toShort, 532423, 2147483647, 0.0f, 0.0, 'x')
    val basics1Result = roundtripBasics1(basics1Input)
    assert(basics1Result._1 == 127)
    assert(basics1Result._2 == 127)
    assert(basics1Result._3 == 32767)
    assert(basics1Result._4 == 32767)
    assert(basics1Result._5 == 532423)
    assert(basics1Result._6 == 2147483647)
    assert(basics1Result._7 == 0.0f)
    assert(basics1Result._8 == 0.0)
    assert(basics1Result._9 == 'x')

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

    val tupleResult1: (C1, Z1) = roundtripTuple((C1.A(5), Z1.A(500)))
    assert(tupleResult1 == (C1.A(5), Z1.A(500)))

    val tupleResult2: (C1, Z1) = roundtripTuple((C1.B(200.0f), Z1.B))
    assert(tupleResult2 == (C1.B(200.0f), Z1.B))

    val tupleResult3: (C1, Z1) = roundtripTuple((C1.A(4), Z1.B))
    assert(tupleResult3 == (C1.A(4), Z1.B))

    assert(Optional.of("ok") == roundtripOption(Optional.of("ok")))
    assert(Optional.empty == roundtripOption(Optional.empty[String]))
    assert(Optional.of(Optional.of("foo")) == roundtripDoubleOption(Optional.of(Optional.of("foo"))))
    assert(Optional.of(Optional.empty) == roundtripDoubleOption(Optional.of(Optional.empty[String])))
    assert(Optional.empty == roundtripDoubleOption(Optional.empty[Optional[String]]))
    // assert(new wit.Err("aaa") != new wit.Err("bbb"))

    assert(new wit.Ok(()) == roundtripResult(new wit.Ok(())))
    assert(new wit.Err(()) == roundtripResult(new wit.Err(())))
    assert(new wit.Ok(3.0f) == roundtripStringError(new wit.Ok(3.0f)))
    assert(new wit.Err("err") == roundtripStringError(new wit.Err("err")))
    assert(new wit.Ok(C1.A(432)) == roundtripEnumError(new wit.Ok(C1.A(432))))
    assert(new wit.Ok(C1.B(0.0f)) == roundtripEnumError(new wit.Ok(C1.B(0.0f))))
    assert(new wit.Err(E1.A) == roundtripEnumError(new wit.Err(E1.A)))
    assert(new wit.Err(E1.B) == roundtripEnumError(new wit.Err(E1.B)))
    assert(new wit.Err(E1.C) == roundtripEnumError(new wit.Err(E1.C)))

    assert((F1.b3 | F1.b6 | F1.b7) == roundtripF1(F1.b3 | F1.b6 | F1.b7))
    assert((F2.b7 | F2.b8 | F2.b15) == roundtripF2(F2.b7 | F2.b8 | F2.b15))
    assert((F3.b7 | F3.b8 | F3.b15 | F3.b31) == roundtripF3(F3.b7 | F3.b8 | F3.b15 | F3.b31))

    val flagsTuple = new wit.Tuple2(F1.b3 | F1.b6, F1.b2 | F1.b3 | F1.b7)
    val flagsResult = roundtripFlags(flagsTuple)
    assert(flagsResult._1 == (F1.b3 | F1.b6))
    assert(flagsResult._2 == (F1.b2 | F1.b3 | F1.b7))

    val result2: (Int, String) = roundtripTuple2((42, "hello"))
    assert(result2 == (42, "hello"))

    val result3: (Int, String, Boolean) = roundtripTuple3((123, "world", true))
    assert(result3 == (123, "world", true))

    // Test resource wrappers
    locally {
      val successResult = toEither(tryCreateCounter(10))
      successResult match {
        case Right(counter) => assert(10 == counter.valueOf())
        case Left(_)        => throw new AssertionError("Expected Right but got Left")
      }

      val errorResult = toEither(tryCreateCounter(-5))
      assert(errorResult.isLeft)

      val maybeCounter = maybeGetCounter()
      assert(maybeCounter.isPresent)
      assert(42 == maybeCounter.get().valueOf())
    }

    locally {
      val c1 = Counter(0)
      c1.up()
      assert(1 == c1.valueOf())

      val c2 = Counter(100)
      c2.down()
      assert(99 == c2.valueOf())

      // TODO: make a and b borrow<bounter>
      // otherwise ownership moves, and cannot use a,b afterwards
      val s1 = Counter.sum(c1, c2)
      assert(100 == s1.valueOf())

      // val s2 = Counter.sum(c1, c2)
      // assert(s1.valueOf() == s2.valueOf())
      assert(100 == s1.valueOf())
      // assert(100 == Counter.sum(c1, c2).valueOf())
    }

    // Test Object methods on imported resources
    TestFunctions.testResourceObjectMethods()

    val end = System.currentTimeMillis()
    println(s"elapsed: ${(end - start).toInt} ms")
  }
}

object TestFunctions {
  def testResourceObjectMethods(): Unit = {
    val c1 = Counter(42)
    val c2 = Counter(100)
    val c3 = c1 // same reference

    // Test toString - should return "resource<ClassName>@hashcode"
    val str1 = c1.toString()
    assert(str1.startsWith("resource<componentmodel.component.testing.countable.package$Counter>@"))
    val str2 = c2.toString()
    val str3 = c3.toString()
    assert(str1 == str3)
    assert(str1 != str2)
    val str4 = s"$c1 + x" // test toStringForConcat
    assert(str4.startsWith("resource<componentmodel.component.testing.countable.package$Counter>@"))

    // Test hashCode - should be based on handle value
    val hash1 = c1.hashCode()
    val hash2 = c2.hashCode()
    val hash3 = c3.hashCode()
    assert(hash1 == hash3)
    assert(hash1 != hash2)

    // Test equals - should use handle-based equality
    assert(c1.equals(c1))
    assert(c1.equals(c3))
    assert(c3.equals(c1))
    assert(!c1.equals(c2))
    assert(!c2.equals(c1))
    assert(!c1.equals(null))
    assert(!c1.equals("not a counter"))
    assert(!c1.equals(42))

    assert(c1 == c3)
    assert(c1 != c2)
    assert(c2 != c1)

    assert(c1 eq c3)
    assert(c1 ne c2)
    assert(c2 ne c1)
    assert(!(c1 eq c2))
    assert(!(c1 ne c3))

    val cls1 = c1.getClass()
    val cls2 = c2.getClass()
    val cls3 = c3.getClass()
    assert(cls1 == cls2)
    assert(cls1 == cls3)
    assert(cls2 == cls3)
  }
}
