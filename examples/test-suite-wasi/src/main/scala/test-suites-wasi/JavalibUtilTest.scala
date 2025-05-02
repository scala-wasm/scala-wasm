package testSuiteWASI

import org.scalajs.testsuite.javalib.util._
import org.scalajs.testsuite.javalib.lang.IterableTest

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
      runListTests(test)
      runCollectionTest(test)
      runIterableTest(test)

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

  private def runListTests(test: ListTest) = {
    import test._
    addStringGetIndex()
    addIntGetIndex()
    addDoubleGetIndex()
    addCustomObjectsGetIndex()
    removeStringRemoveIndex()
    removeDoubleOnCornerCases()
    clearList()
    containsStringList()
    // containedDoubleOnCornerCases()
    setString()
    test.iterator()
    toArrayObjectForList()
    toArraySpecificForList()
    listIterator()
    listIteratorPreviousThrowsNoSuchElementException()
    addIndex()
    indexOf()
    lastIndexOf()
    // indexOfLastIndexOfDoubleCornerCases()
    subListBackedByList()
    iteratorSetRemoveIfAllowed()
    replaceAll()
    sortWithNaturalOrdering()
    sortWithComparator()
  }

  private def runCollectionTest(test: CollectionTest) = {
    import test._
    testWithString()
    testWithInt()
    // testWithDouble()
    testWithCustomClass()
    removeString()
    removeDoubleCornerCases()
    clear()
    containsString()
    // containsDoubleCornerCases()
    iteratorString()
    toArrayObject()
    toArraySpecific()
    removeIf()
    // TODO: float to string
    // toStringCollectionDoubleEmpty()
    // toStringCollectionDoubleOneElement()
    // toStringCollectionDoubleHasCommaSpace()
    toStringCollectionAnyWithNull()
    toStringCollectionCustomClass()
  }

  private def runIterableTest(test: IterableTest) = {
    import test._
    empty()
    simpleSum()
    iteratorThrowsNoSuchElementException()
  }
}