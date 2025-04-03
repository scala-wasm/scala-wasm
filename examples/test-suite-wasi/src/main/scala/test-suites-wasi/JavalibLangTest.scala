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
      isSurrogatePair()
      charCount()
      toCodePoint()
      highSurrogate()
      lowSurrogate()

      // TODO: Math.random
      // isISOControl()

      digit()

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
      toLowerCaseCompareCharAndCodepoint()
      toUpperCaseCompareCharAndCodepoint()
      toTitleCaseCompareCharAndCodepoint()
      isIdentifierIgnorable()
      isUpperCase()
      toLowerCaseInt()
      toLowerCaseCodePointSpecialCases()
      toLowerCaseCodePointStringLowerCaseDiffCharacterLowerCase()
      toUpperCaseInt()
      toUpperCaseCodePointSpecialCases()
      toUpperCaseCodePointStringUpperCaseDiffCharacterUpperCase()
      toTitleCaseCodePointSpecialCases()
      toTitleCaseCodePointStringUpperCaseDiffCharacterTitleCase()
      codePointCountString()
      codePointCountCharSequence()
      codePointCountArray()
      compare()
      compareTo()
      compareToAnyAny()

      isUnicodeIdentifierStart()
      isUnicodeIdentifierPart()

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

      // TODO: JSMethodApply String.split
      // ofIntOutOfRangeThrowsIllegalArgumentException()
      // forNameNotFoundThrowsIllegalArgumentException()

      // TODO: JSMethodApply: _String.replace
      // ofChar()
      // ofCodePoint()
      // forNameString()
    }

    locally {
      val test = new ClassTest
      import test._
      hierarchy()
      getPrimitiveTypeName()
      // TODO: jsValueType in anyGetTypeData
      // getClassGetName()

      getClassGetNameNull()
      wellKnownClasses()

      // JSSelect: hasOwnProperty: Utils.scala
      // getSimpleName()

      isAssignableFrom()
      getComponentType()
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

      // TODO: parseString
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
      simpleSum()
      iteratorThrowsNoSuchElementException()
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
      // TODO: anyGetTypeData ?
      // testGetClass()

      test.equals()
      isInstanceOfObjectExceptNull()
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
      // decodeStringInvalidThrows()
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

      // TODO: ltoa, float/double to string
      appendPrimitive()

      appendCodePoint()
      getChars()
      delete()
      deleteCharAt()
      replace()
      insertCharArrayOffsetLen()
      insertString()
      insertCharArray()

      // TODO: asesertion fail
      // insertAnyRef()
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

      // TODO: codePointAt
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
      getChars()
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

      getChars()
      concat()
      constructors()

      // TODO: JSFunctionApply
      // format()
      // TODO: hasOwnProperty
      // getBytes()

      regionMatches()

      trim()

      // TODO: it takes too long to concat strings
      // createFromLargeCharArray_Issue2553()
      // createFromLargeCodePointArray_Issue2553()

      stringCaseInsensitiveOrdering()
    }

    locally {
      val test = new SystemArraycopyTest
      import test._
      // TODO: assertion fail, maybe mkString
      // simpleTests()
      arraycopyWithRangeOverlapsForTheSameArrayInt()
      arraycopyWithRangeOverlapsForTheSameArrayBoolean()
      arraycopyWithRangeOverlapsForTheSameArrayObject()
      arraycopyWithRangeOverlapsForTheSameArrayString()

      arraycopyNulls()
      arraycopyNullsShortcircuited()

      arraycopyIndexOutOfBoundsInt()
      arraycopyIndexOutOfBoundsBoolean()
      arraycopyIndexOutOfBoundsObject()
      arraycopyIndexOutOfBoundsString()

      earlyArrayStoreException()
      lateArrayStoreException()

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

      identityHashCode()
      identityHashCodeNotEqualHashCodeForList()
      identityHashCodeOfNull()

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
      throwableMessage_Issue2559()

      // TODO: test fail (why?)
      // noWritableStackTrace()

      suppression()
      noSuppression()
      throwableStillHasMethodsOfObject()
      throwableJSToStringCanBeOverridden()

      // TODO: float to string / AssertionError("boom").getMessage fails
      assertionErrorsPeculiarConstructors()

    }
  }
}