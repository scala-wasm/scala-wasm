package testSuiteWASI

import org.scalajs.testsuite.javalib.util._

object JavalibUtilTest {
  def run(): Unit = {
    locally {
      val test = new ArraysTest
      import test._
      sortInt()
      sortLong()
      sortShort()
      sortByte()
      sortChar()
      sortFloat()
      sortDouble()

      sortString()
      sortStringWithNullComparator()

      sortIsStable_Issue2400()
      sortWithComparator()
      sortIsStable()

      sortIllegalArgumentException()
      sortArrayIndexOutOfBoundsException()

      fillBoolean()
      fillBooleanWithStartAndEndIndex()
      fillByte()
      fillByteWithStartAndEndIndex()
      fillShort()
      fillShortWithStartAndEndIndex()
      fillInt()
      fillIntWithStartAndEndIndex()
      fillLong()
      fillLongWithStartAndEndIndex()
      fillFloat()
      fillFloatWithStartAndEndIndex()
      fillDouble()
      fillDoubleWithStartAndEndIndex()
      fillAnyRef()
      fillAnyRefWithStartAndEndIndex()

      binarySearchWithStartAndEndIndexOnLong()
      binarySearchOnLong()
      binarySearchWithStartAndEndIndexOnInt()
      binarySearchOnInt()
      binarySearchWithStartAndEndIndexOnShort()
      binarySearchOnShort()

      binarySearchWithStartAndEndIndexOnChar()
      binarySearchOnChar()
      binarySearchWithStartAndEndIndexOnDouble()
      binarySearchOnDouble()
      binarySearchWithStartAndEndIndexOnFloat()
      binarySearchOnFloat()

      binarySearchWithStartAndEndIndexOnAnyRef()
      binarySearchOnAnyRef()
      binarySearchWithStartAndEndIndexOnSpecificAnyRefWithNullComparator()
      binarySearchOnSpecificAnyRefWithNullComparator()
      binarySearchWithStartAndEndIndexOnSpecificAnyRefWithComparator()
      binarySearchOnSpecificAnyRefWithComparator()

      binarySearchIllegalArgumentException()
      binarySearchArrayIndexOutOfBoundsException()

      copyOfInt()
      copyOfLong()
      copyOfShort()
      copyOfByte()
      copyOfChar()
      copyOfDouble()
      copyOfFloat()
      copyOfBoolean()
      copyOfAnyRef()
      copyOfAnyRefWithChangeOfType()
      copyOfRangeAnyRef()
      copyOfRangeAnyRefArrayIndexOutOfBoundsException()
      asList()

      hashCodeBoolean()
      hashCodeChars()
      hashCodeBytes()
      hashCodeShorts()
      hashCodeInts()
      hashCodeLongs()
      hashCodeFloats()
      hashCodeDoubles()
      hashCodeAnyRef()
      deepHashCode()

      equalsBooleans()
      equalsBytes()
      equalsChars()
      equalsShorts()
      equalsInts()
      equalsLongs()
      equalsFloats()
      equalsDoubles()

      deepEquals()
      // toStringAnyRef()

      // TODO: JSArrayConstr in deepToString
      // deepToString()
    }

    locally {
      val test = new ArrayListTest
      import test._
      ensureCapacity()
      constructor()
      constructorInt()
      constructorCollectionInteger()
      constructorCollectionString()
      constructorNullThrowsNullPointerException()
      equalsForEmptyLists()

      equalsForNonEmptyLists()
      trimToSizeForNonEmptyListsWithDifferentCapacities()
      trimToSizeForEmptyLists()
      trimToSizeForNonEmptyLists()
      size()
      isEmpty()
      indexOfAny()
      lastIndexOfAny()
      testClone()
      cloneWithSizeNotEqualCapacity()
      toArray()
      toArrayDefaultInitialCapacityThenAddElements()
      toArrayArrayWhenArrayIsShorter()
      toArrayArrayWhenArrayIsWithTheSameLengthOrLonger()
      arrayEToArrayTWhenTSubE()
      arrayEToArrayTShouldThrowArrayStoreExceptionWhenNotTSubE()
      toArrayNullThrowsNull()
      getInt()
      setInt()
      add()
      addInt()
      addIntWhenTheCapacityHasToBeExpanded()
      addAll()
      removeInt()
      removeAny()
      removeRangeFromToIndenticalInvalidIndices()
      removeRangeFromToInvalidIndices()
      removeRangeFromToFirstTwoElements()
      removeRangeFromToFirstTwoElementsAtHead()
      removeRangeFromToTwoElementsFromMiddle()
      removeRangeFromToLastTwoElementsAtTail()
      removeRangeFromToEntireListAllElements()
      clearTest()
      shouldThrowAnErrorWithNegativeInitialCapacity()
      containsAny()
      testToString()
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