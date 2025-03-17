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
      fillBoolean()
      fillBooleanWithStartAndEndIndex()
      // TODO: JSArrayConstr (Array[Short](...))
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
      // TODO: JSArrayConstr
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

      // TODO: JSArrayConstr
      // hashCode tests

      equalsBooleans()
      // TODO: JSArrayConstr
      // equalsBytes()
      // equalsChars()

      // equalsShorts()
      // equalsInts()
      // equalsLongs()
      // equalsFloats()
      // equalsDoubles()

      // TODO: JSArrayConstr
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