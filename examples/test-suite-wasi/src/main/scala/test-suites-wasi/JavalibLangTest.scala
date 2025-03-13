package testSuiteWASI

import javalib.lang._
import testSuiteWASI.compiler.ClassDiffersOnlyInCaseTest.testClassesThatDifferOnlyInCase_Issue4855

object JavalibLangTest {
  def run(): Unit = {
    locally {
      val test = new BooleanTest
      test.booleanValue()
      test.compareTo()
      test.compareToAnyAny()
      // JSMethodApply
      // test.parseStringMethods()
    }

    locally {
      val test = new ByteTest
      test.compareToJavaByte()
      test.compareTo()
      test.toUnsignedInt()
      test.toUnsignedLong()
      // JSMethodApply
      // test.parseString()

      // throw
      // test.parseStringInvalidThrows()
      // test.parseStringBase16()

      // Integer asInt
      // test.decodeStringBase8()
      // test.decodeInvalidThrows()
    }

    locally {
      val test = new CharacterTest
      import test._
      hashCodeChar()
      toStringChar()
      isValidCodePoint()
      isBmpCodePoint()
      isSupplementaryCodePoint()
      isHighSurrogate()
      isLowSurrogate()
      // TODO: JSArrayConstructor (List[Char] ?)
      // isSurrogatePair()
      charCount()
      toCodePoint()
      // TODO: Assertion fails
      // highSurrogate()
      // lowSurrogate()

      // TODO: Closure
      // isISOControl()

      // TODO: Closure
      // digit()

      forDigit()
      codePointAtCharSequence()
      codePointAtCharSequenceIndexOutOfBounds()

      codePointAtArray()
      codePointAtArrayIndexOutOfBounds()
      codePointAtArrayWithLimit()
      codePointAtArrayWithLimitIndexOutOfBounds()

      codePointBeforeCharSequence()
      codePointBeforeCharSequenceIndexOutOfBounds()
      codePointBeforeArray()
      codePointBeforeArrayIndexOutOfBounds()
      codePointBeforeArrayWithStart()
      codePointBeforeArrayWithStartIndexOutOfBounds()
      toChars()

      toCharsInPlace()
      offsetByCodePointsCharSequence()
      offsetByCodePointsCharSequenceBackwards()
      offsetByCodePointsArray()
      offsetByCodePointsArrayBackwards()
      isDigit()

      // TODO: Closure
      // toLowerCaseCompareCharAndCodepoint()

      toLowerCaseInt()
      toLowerCaseCodePointSpecialCases()
      toLowerCaseCodePointStringLowerCaseDiffCharacterLowerCase()

      // TODO: Closure
      // toUpperCaseCompareCharAndCodepoint()

      toUpperCaseInt()
      toUpperCaseCodePointSpecialCases()
      toUpperCaseCodePointStringUpperCaseDiffCharacterUpperCase()

      // TODO: Closure
      // toTitleCaseCompareCharAndCodepoint()

      toTitleCaseCodePointSpecialCases()
      toTitleCaseCodePointStringUpperCaseDiffCharacterTitleCase()

      codePointCountString()
      codePointCountCharSequence()
      codePointCountArray()
      compare()
      compareTo()
      compareToAnyAny()

      // TODO: Closure
      // isIdentifierIgnorable()

      isUnicodeIdentifierStart()
      isUnicodeIdentifierPart()

      // TODO: Closure
      // isUpperCase()

      isAlphabetic()
      isIdeographic()
      isSpaceChar()
      isWhitespace()
      isTitleCase()
      isMirrored()
      isDefined()
      getType()
      isJavaLetter()
      isJavaLetterOrDigit()
      isJavaIdentifierStart()
      reverseBytes()
    }

    locally {
      val test = new CharacterUnicodeBlockTest
      import test._
      // TODO: _String.replace
      // forNameNormalizations()
      // forNameHistorical()

      ofIntOutOfRangeThrowsIllegalArgumentException()
      forNameNotFoundThrowsIllegalArgumentException()

      // TODO: JSMethodApply: _String.replace
      // ofChar()
      // ofCodePoint()
      // forNameString()
    }

    locally {
      // val test = new ClassTest
      // import test._
      // TODO: Closure
      // hierarchy()
      // getPrimitiveTypeName()
      // getClassGetName()
      // getClassGetNameNull()
      // wellKnownClasses()

      // JSSelect: hasOwnProperty: Utils.scala
      // getSimpleName()

      // Closure
      // isAssignableFrom()
      // getComponentType()
    }

    // TODO
    // JSMethodApply(VarRef(LocalName<map>),StringLiteral(get),List(VarRef(LocalName<type>)))
    // java/lang/Utils.scala 144
    // locally {
    //   (new ClassValueTest).testClassValue()
    // }

    locally {
      val test = new DoubleTest
      import test._
      properEquals()
      hashCodeTest()
      // TODO Double toString
      // toStringWithIntegerValuesWhenAnInteger()
      // toHexStringTest()

      // TODO: JSArrayConstr (?)
      // parseStringMethods()
      // parseDoubleInvalidThrows()
      compareToJavaDouble()

      compareToConvertedFromInt_Issue3085()
      compareTo()
      isInfinite_Issue515()
      isNaNTest()
      // longBitsToDouble()
      doubleToLongBits()
      isFinite()
      testStaticHashCode()
      // sum()
      // max()
      // min()
    }

    locally {
      val test = new FloatTest
      import test._
      properEquals()
      hashCodeTest()

      // JSMethodApply
      // toStringWithIntegerValuesWhenAnInteger()
      // toHexStringTest()
      // parseStringMethods()
      // parseFloatInvalidThrows()

      compareTo()
      compareToAnyAny()
      isInfinite_Issue515()
      isNaNTest()
      // intBitsToFloat()
      floatToIntBits()
      isFinite()
      testStaticHashCode()

      // sum()
      // max()
      // min()
    }

    locally {
      val test = new IntegerTest
      import test._
      `reverseBytes`()
      `reverse`()
      rotateLeft()
      rotateRight()
      bitCount()
      numberOfLeadingZeros()
      numberOfTrailingZeros()

      toBinaryString()
      toHexString()
      toOctalString()
      compareToInteger()
      compareTo()

      // TODO: JSBinaryOp 8: asInt (number2dynamic)
      // parseString()
      // parseStringInvalidThrows()
      // parseUnsignedInt()
      // parseUnsignedIntInvalidThrows()
      // parseStringBase16()
      // decodeStringBase8()
      // decodeStringInvalidThrows()

      highestOneBit()
      lowestOneBit()

      // TODO: JSBinaryOp 8: asInt parseString
      // new Integer("-12345").toString
      testToString()

      // TODO: JSMethodApply toString (enableJSNumberOps)
      // toStringRadix()

      // TODO: JSMethodApply (asInt)
      // parseUnsignedIntRadix()
      // parseUnsignedIntRadixInvalidThrows()
      // parseUnsignedIntBase16()

      // JSBinaryOp 13 Utils.scala toUint
      // compareUnsigned()

      toUnsignedLong()
      divideUnsigned()
      remainderUnsigned()

      // TODO: JSMethodApply toString
      // toUnsignedString()
      // toUnsignedStringRadix()

      testStaticHashCode()
      sum()
      max()
      min()
    }

    locally {
      val test = new IterableDefaultTest
      import test._
      empty()
      // TODO: Closure (WrappedString 63)
      // simpleSum()
      // iteratorThrowsNoSuchElementException()
    }

    locally {
      val test = new LongTest
      import test._
      reverse()
      rotateLeft()
      rotateRight()
      bitCount()
      compareToJavaLong()
      compareTo()

      // TODO: JSArrayConstr (StringRadixInfos)
      // parseString()
      // parseStringInvalidThrows()
      // parseStringBase16()
      // parseStringBase2To36()
      // parseStringsBaseLessThanTwoOrBaseLargerThan36Throws()
      // testDecodeBase8()
      // decodeStringInvalidThrows()
      // toStringRadix()
      testToString()

      highestOneBit()
      lowestOneBit()

      toBinaryString()
      toHexString()
      toOctalString()
      numberOfLeadingZeros()
      numberOfTrailingZeros()
      signum()

      // TODO: JSArrayConstr (StringRadixInfos)
      // parseUnsignedLong()
      // parseUnsignedLongFailureCases()
      // toUnsignedString()

      hashCodeTest()
      compareUnsigned()
      divideUnsigned()
      remainderUnsigned()
      sum()
      max()
      min()
    }

    // TODO: MathTest

    locally {
      val test = new ObjectTest
      import test._
      // TODO: anyGetTypeData - jsValueType
      // testGetClass()

      // TODO: JSArrayConstr List(XY) ?
      // test.equals()

      isInstanceOfObjectExceptNull() // TODO List(1)
      isInstanceOfObjectNull()
      asInstanceOfObjectAll()
      cloneCtorSideEffects_Issue3192()
    }

    locally {
      val test = new ShortTest
      import test._
      compareToJavaShort()
      compareTo()
      toUnsignedInt()

      // TODO: JSBinaryOp asInt
      // parseString()
      // parseStringInvalidThrows()
      // parseStringBase16()
      // decodeStringBase8()
      decodeStringInvalidThrows()
    }

    locally {
      // TODO assertion fail
      // (new StackTraceElementTest).toStringUnmodifiedIfColumnNumberIsNotSpecified()
    }

    locally {
      val test = new StringBufferTest
      import test._
      init()
      initInt()
      initString()
      initCharSequence()

      // TODO: toString null / None?
      // appendAnyRef()

      appendString()
      appendStringBuffer()

      // TODO: concat with CharSequence?
      // appendCharSequence()
      // appendCharSequenceStartEnd()

      appendCharArray()
      appendCharArrayOffsetLen()

      // TODO: toString primitive
      // appendPrimitive()

      appendCodePoint()

      // TODO: Closure in WrappedString
      // getChars()

      delete()
      deleteCharAt()
      replace()
      insertCharArrayOffsetLen()
      // insertAnyRef()
      insertString()
      insertCharArray()
      // insertCharSequence()
      // insertCharSequenceStartEnd()
      // insertPrimitive()

      indexOfString()
      indexOfStringInt()
      lastIndexOfString()
      lastIndexOfStringInt()

      reverse()
      length()
      capacity()
      ensureCapacity()
      trimToSize()
      setLength()
      charAt()

      // TODO
      // codePointAt()

      codePointBefore()
      codePointCount()
      offsetByCodePoints()
      offsetByCodePointsBackwards()
      setCharAt()
      substringStart()
      subSequence()
      substringStartEnd()
    }

    locally {
      val test = new StringBuilderTest
      import test._

      init()
      initInt()
      initString()
      initCharSequence()

      // appendAnyRef()
      appendString()
      appendStringBuffer()

      // appendCharSequence()
      // appendCharSequenceStartEnd()

      appendCharArray()
      appendCharArrayOffsetLen()

      // TODO: toString
      // appendPrimitive()

      appendCodePoint()

      delete()
      deleteCharAt()
      replace()
      insertCharArrayOffsetLen()
      // insertAnyRef()
      insertString()
      insertCharArray()
      // insertCharSequence()
      // insertCharSequenceStartEnd()
      // insertPrimitive()
      indexOfString()
      indexOfStringInt()
      lastIndexOfString()
      lastIndexOfStringInt()
      reverse()
      length()
      capacity()
      ensureCapacity()
      trimToSize()
      setLength()
      charAt()
      // codePointAt()
      codePointBefore()
      codePointCount()
      offsetByCodePoints()
      offsetByCodePointsBackwards()
      // TODO: Closure: WrappedString
      // getChars()
      setCharAt()
      substringStart()
      subSequence()
      substringStartEnd()
      stringInterpolationToSurviveNullAndUndefined()
    }

    locally {
      val test = new StringTest
      import test._
      lengthTest()
      intern()
      equalsTest()
      compareTo()
      equalsIgnoreCase()
      compareToIgnoreCase()

      isEmpty()
      contains()

      startWith()
      endsWith()

      indexOfString()
      lastIndexOfString()

      indexOfInt()
      lastIndexOfInt()

      toUpperCase()
      toLowerCase()

      charAt()
      codePointBefore()
      codePointBeforeIndexOutOfBounds()
      codePointCount()
      offsetByCodePoints()
      offsetByCodePointsBackwards()
      substringBegin()
      substringBeginIndexOutOfBounds()
      substringBeginEnd()
      substringBeginEndIndexOutOfBounds()
      subSequence()
      subSequenceIndexOutOfBounds()

      // TODO _String replace
      // replace()

      // TODO: Regex
      // matches()
      // split()
      // splitWithCharAsArguments
      startsWithPrefixToffset_Issue1603()
      toCharArray()
      hashCodeTest()

      // TODO: Closure in WrappedString
      // getChars()
      concat()
      constructors()
      // format()
      // getBytes()
      regionMatches()

      // TODO Closure
      // trim()
      // createFromLargeCharArray_Issue2553()
      // createFromLargeCodePointArray_Issue2553()

      stringCaseInsensitiveOrdering()
    }

    locally {
      val test = new SystemArraycopyTest
      import test._
      // TODO: Closure in WrappedString
      // simpleTests()
      arraycopyWithRangeOverlapsForTheSameArrayInt()
      arraycopyWithRangeOverlapsForTheSameArrayBoolean()
      arraycopyWithRangeOverlapsForTheSameArrayObject()
      // TODO: type mismatch: expected subtype of externref, found (ref (id 1))
      // arraycopyWithRangeOverlapsForTheSameArrayString()

      // TODO: JSArrayConstr in SystemArraycopyTest
      // new Array[Int](10) translates to JSArrayConstr?
      // arraycopyNulls()
      // arraycopyNullsShortcircuited()

      // TODO: exception handling
      // arraycopyIndexOutOfBoundsInt()
      // arraycopyIndexOutOfBoundsBoolean()
      // arraycopyIndexOutOfBoundsObject()
      // arraycopyIndexOutOfBoundsString()

      // TODO: JSArrayConstr
      // earlyArrayStoreException()
      // lateArrayStoreException()

    }

    // TODO: System.scala (JSObjectConstr)
    locally {
      val test = new SystemPropertiesTest
      import test._
      // testScenariosWithoutJavaUtilProperties()
      // testScenariosWithGetProperties()
      // testScenariosWithSetProperties()
    }

    locally {
      val test = new SystemTest
      import test._
      setIn()
      setOut()
      setErr()

      // TODO: identityHashCode
      // identityHashCode()
      // identityHashCodeNotEqualHashCodeForList()
      // identityHashCodeOfNull()

      lineSeparator()
      getenvReturnsUnmodifiableMap()
      getenvLinksAndDoesNotThrow()
    }

    locally {
      val test = new ThreadTest
      import test._
      getNameAndSetName()

      // TODO: StackTrace
      // currentThreadGetStackTrace()

      getId()
      interruptExistsAndTheStatusIsProperlyReflected()
    }

    locally {
      val test = new ThrowablesTest
      import test._

      allJavaLangErrorsAndExceptions()
      // TODO: Closure
      // throwableMessage_Issue2559()

      // TODO: test fail (why?)
      // noWritableStackTrace()

      // TODO: Closure
      // suppression()
      noSuppression()
      throwableStillHasMethodsOfObject()
      throwableJSToStringCanBeOverridden()

      // TODO: float to string / AssertionError("boom").getMessage fails
      assertionErrorsPeculiarConstructors()

    }


  }
}