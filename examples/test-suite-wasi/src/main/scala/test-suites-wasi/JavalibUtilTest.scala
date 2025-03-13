package testSuiteWASI

import javalib.util._

object JavalibUtilTest {
  def run(): Unit = {
    locally {
      val test = new ArraysTest
      import test._

      // requires assertArrayEquals
      // Arrays.sort

      // Arrays.fill
      // TODO: Closure in WrappedString
      fillBoolean()
      fillBooleanWithStartAndEndIndex()
      // fillByte()
      // fillByteWithStartAndEndIndex()
      // fillShort()
      // fillShortWithStartAndEndIndex()
      fillInt()
      fillIntWithStartAndEndIndex()
      fillLong()
      fillLongWithStartAndEndIndex()
      fillFloat()
      fillFloatWithStartAndEndIndex()
      // TODO: assert fail
      // fillDouble()
      // fillDoubleWithStartAndEndIndex()
      fillAnyRef()
      fillAnyRefWithStartAndEndIndex()

      binarySearchWithStartAndEndIndexOnLong()
      binarySearchOnLong()
      binarySearchWithStartAndEndIndexOnInt()
      binarySearchOnInt()
      // TODO: Closure
      // binarySearchWithStartAndEndIndexOnShort()
      // binarySearchOnShort()

      binarySearchWithStartAndEndIndexOnChar()
      binarySearchOnChar()
      binarySearchWithStartAndEndIndexOnDouble()
      binarySearchOnDouble()
      binarySearchWithStartAndEndIndexOnFloat()
      binarySearchOnFloat()

      // TODO: type mismatch: expected (ref $type), found (ref extern)
      // binarySearchWithStartAndEndIndexOnAnyRef()
      // binarySearchOnAnyRef()
      // binarySearchWithStartAndEndIndexOnSpecificAnyRefWithNullComparator()
      // binarySearchOnSpecificAnyRefWithNullComparator()
      // binarySearchWithStartAndEndIndexOnSpecificAnyRefWithComparator()
      // binarySearchOnSpecificAnyRefWithComparator()

      // TODO: assertThrows
      // binarySearchIllegalArgumentException
      // binarySearchArrayIndexOutOfBoundsException

      // copyOf

      asList()

      // TODO: Closure in WrappedString
      // hashCode tests

      equalsBooleans()
      // TODO: Closure in WrappedString
      // equalsBytes()
      // equalsChars()

      // equalsShorts()
      // equalsInts()
      // equalsLongs()
      // equalsFloats()
      // equalsDoubles()

      // TODO: Closure in WrappedString
      deepEquals()

      toStringAnyRef()

      // TODO: JSArrayConstr in deepToString
      // deepToString()
    }

    locally {
      val test = new ObjectsTest
      import test._
      testEquals()
      testDeepEquals()
      testHashCode()
      hash()
      testToString()
      compare()
      requireNonNull()
      isNull()
      nonNull()

    }
  }
}