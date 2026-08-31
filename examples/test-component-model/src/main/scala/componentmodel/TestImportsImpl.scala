package componentmodel

import scala.scalajs.wit
import scala.scalajs.wit.annotation._
import scala.scalajs.wit.unsigned._

import componentmodel.component.testing.basics._
import componentmodel.component.testing.tests._
import componentmodel.component.testing.countable._
import componentmodel.root._

object TestImportsImpl {
  @WitExport(WitScope.unversioned("component", "testing", "test-imports"), "run")
  def run(): Unit = {
    def newCounterArray(size: Int): Array[Counter] =
      new Array[Counter](size)

    val start = Console.currentTimeMillis()

    // Test world-level function imports
    assert(5 == bareAdd(2, 3))
    assert(10 == bareAdd(7, 3))
    assert("Hello, World!" == bareGreet("World"))
    assert("Hello, Scala!" == bareGreet("Scala"))

    assert(WorldPoint(3.asInstanceOf[UInt], 4.asInstanceOf[UInt]) ==
      movePoint(WorldPoint(2.asInstanceOf[UInt], 2.asInstanceOf[UInt])))

    assert(NamedR(NamedF.b0) == roundtripNamedR(NamedR(NamedF.b0)))

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

    val counter1 = Counter(1)
    val counter2 = Counter(2)
    val counterArr = newCounterArray(2)
    assert(counterArr.length == 2)
    counterArr(0) = counter1
    counterArr(1) = counter2
    assert(counterArr(0).valueOf() == 1)
    assert(counterArr(1).valueOf() == 2)

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

    assert(wit.Some("ok") == roundtripOption(wit.Some("ok")))
    assert(wit.None == roundtripOption(wit.None))
    assert(wit.Some(wit.Some("foo")) == roundtripDoubleOption(wit.Some(wit.Some("foo"))))
    assert(wit.Some(wit.None) == roundtripDoubleOption(wit.Some(wit.None)))
    assert(wit.None == roundtripDoubleOption(wit.None))
    // assert(new wit.Err("aaa") != new wit.Err("bbb"))

    assert(wit.Ok(()) == roundtripResult(wit.Ok(())))
    assert(wit.Err(()) == roundtripResult(wit.Err(())))
    assert(wit.Ok(3.0f) == roundtripStringError(wit.Ok(3.0f)))
    assert(wit.Err("err") == roundtripStringError(wit.Err("err")))
    assert(wit.Ok(C1.A(432)) == roundtripEnumError(wit.Ok(C1.A(432))))
    assert(wit.Ok(C1.B(0.0f)) == roundtripEnumError(wit.Ok(C1.B(0.0f))))
    assert(wit.Err(E1.A) == roundtripEnumError(wit.Err(E1.A)))
    assert(wit.Err(E1.B) == roundtripEnumError(wit.Err(E1.B)))
    assert(wit.Err(E1.C) == roundtripEnumError(wit.Err(E1.C)))

    assert((F1.b3 | F1.b6 | F1.b7) == roundtripF1(F1.b3 | F1.b6 | F1.b7))
    assert((F2.b7 | F2.b8 | F2.b15) == roundtripF2(F2.b7 | F2.b8 | F2.b15))
    assert((F3.b7 | F3.b8 | F3.b15 | F3.b31) == roundtripF3(F3.b7 | F3.b8 | F3.b15 | F3.b31))

    val flagsTuple = wit.Tuple2(F1.b3 | F1.b6, F1.b2 | F1.b3 | F1.b7)
    val flagsResult = roundtripFlags(flagsTuple)
    assert(flagsResult._1 == (F1.b3 | F1.b6))
    assert(flagsResult._2 == (F1.b2 | F1.b3 | F1.b7))

    val result2: (Int, String) = roundtripTuple2((42, "hello"))
    assert(result2 == (42, "hello"))

    val result3: (Int, String, Boolean) = roundtripTuple3((123, "world", true))
    assert(result3 == (123, "world", true))

    // Test resource wrappers
    locally {
      tryCreateCounter(10) match {
        case wit.Ok(counter) => assert(10 == counter.valueOf())
        case wit.Err(_)      => throw new AssertionError("Expected Ok but got Err")
      }

      tryCreateCounter(-5) match {
        case wit.Err(_) => ()
        case wit.Ok(_)  => throw new AssertionError("Expected Err but got Ok")
      }

      maybeGetCounter() match {
        case wit.Some(counter) => assert(42 == counter.valueOf())
        case wit.None          => throw new AssertionError("Expected Some but got None")
      }
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

    testWitTypeAlias()

    val end = Console.currentTimeMillis()
    Console.println(s"elapsed: ${(end - start).toInt} ms")
  }

  def testWitTypeAlias(): Unit = {
    assert(wit.Some(7.asInstanceOf[UInt]) ==
      roundtripOptionU32(wit.Some(7.asInstanceOf[UInt])))
    assert(wit.None == roundtripOptionU32(wit.None))
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
