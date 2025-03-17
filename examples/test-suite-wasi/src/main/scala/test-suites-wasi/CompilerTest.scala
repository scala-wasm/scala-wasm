package testSuiteWASI

import compiler._

object CompilerTest {
  def run(): Unit = {
    // TODO: ArrayTest
    BooleanTest.bitwiseAndOrXorOperators()
    ByteTest.toByteAndToCharAreInRange()
    CharTest.toIntNegativeToPositive()
    CharTest.toIntOverflow()
    CharTest.multiplyOverflow()
    CharTest.doNotBoxSeveralTimesInBlock()
    CharTest.doNotBoxSeveralTimesInIf()
    ClassDiffersOnlyInCaseTest.testClassesThatDifferOnlyInCase_Issue4855()

    // TODO: type mismatch: expected (ref $type), found (ref extern)
    // DefaultMethodsTest.canOverrideDefaultMethod()
    // DefaultMethodsTest.reflectiveCallDefaultMethod()

    locally {
      val test = new IntTest
      test.unaryMinus()
      test.plus()
      test.minus()
      test.times()
      test.division()
      // TODO: assert throws
      // intTest.divisionByZero()
      // intTest.moduloByZero()
      test.remainderNegative0_Issue1984()
      test.shiftLeft()
      test.shiftRight()
      test.shiftRightSignExtend()
      test.intShiftLeftLongConstantFolded()
      test.intShiftLeftLongAtRuntime()
      test.intShiftLogicalRightLongConstantFolded()
      test.intShiftLogicalRightLongAtRuntime()
      test.intShiftArithmeticRightLongConstantFolded()
      test.intShiftArithmeticRightLongAtRuntime()
    }

    // DoubleTest
    // FloatTest
    // LongTest

    locally {
      val test = new MatchTest
      test.switchWithGuardsStat()
      test.switchWithGuardsExpr()

      // TODO: JSArrayConstr in HashSet
      // test.matchWithNonIdentityMatchEndScalaLib()

      test.matchWithNonIdentityMatchEndIndependent()
    }

    locally {
      val test = new NameEncodingTest
      test.namesThatAreJSReservedWords_Issue153()
      test.namesStartingWithDigit_Issue153()
      test.javaIdentPartNotJSIdentPart()
      test.localEvalOrArguments_Issue743()
    }

    // TODO: NullPointersTest

    locally {
      val test = new OuterClassTest
      test.`Test code variant 1 from #2382`()
      test.`Test code variant 2 from #2382`()
      test.`Test code variant 3 from #2382`()
      test.`Test code variant 4 from #2382`()
      test.`Test code variant 5 from #2382`()
    }

    locally {
      (new PatMatOuterPointerCheckTest).testPatMatOuterPointerCheck()
    }

    // TODO: RegressionTest

    locally {
      val test = new ReflectiveCallTest
      test.subtypingInReturnTypes()
      test.thisTypeInReturnTypes()
      // TOOD: ? string equality?
      // test.genericReturnTypes()
      test.unaryMethodsOnPrimitiveTypes()
      test.binaryOperatorsOnPrimitiveTypes()

      // TODO: primitive values equality
      // test.qualityOperatorsOnPrimitiveTypes()

      // TODO: expected (ref i31), found (ref any)
      // test.compareToForPrimitives()

      // TODO: float to string
      // reflective call with string concat doesn't work as expected...
      // test.concatForPrimitives()

      test.arrays()
      test.arraysOfPrimitiveValues()
      test.strings()
      test.forwardersForInheritedMethods()
      test.bugCompatibleWithScalaJVMForInheritedOverloads()
      test.javaLangObjectNotifyNotifyAll_Issue303()
      test.javaLangObjectClone_Issue303()
      // test.scalaAnyRefEqNeSynchronized_Issue2709()

      // TODO
      // 0: 0x8145 - <unknown>!uD
      // 1: 0x9f3a - <unknown>!f.scala.runtime.BoxesRunTime$.equalsNumObject;Ljava.lang.Number;Ljava.lang.Object;Z
      // 2: 0x11fc9 - <unknown>!f.testSuiteWASI.compiler.ReflectiveCallTest$AnyValWithAnyRefPrimitiveMethods$.eq$extension;I;Ljava.lang.Object;Z
      // 3: 0x11eea - <unknown>!f.testSuiteWASI.compiler.ReflectiveCallTest$AnyValWithAnyRefPrimitiveMethods.eq;Ljava.lang.Object;R
      // 4: 0x11c81 - <unknown>!ps.testSuiteWASI.compiler.ReflectiveCallTest.objEqTest$1;Ljava.lang.Object;Ljava.lang.Object;Z
      // test.anyValEqNeSynchronized_Issue2709()

      test.javaLangFloatDoubleIsNaNIsInfinite()
      test.defaultArguments_Issue390()
      test.unboxAllTypesOfArguments_Issue899()

    }

    locally {
      val test = new RuntimeTypeTestsTest
      import test._
      objectType()

      // TODO: JSArrayConstr (List(...))
      regularClass()
      regularInterface()

      // TODO: hit jsValueType
      // serializableAndCloneable()
      // javaLangNumber()
      // primitiveTypes()

      unit()

      // TODO: JSArrayConstr (List(...))
      string()
      arrayTypes()

      nothingType()
      nullType() // TODO: JSArrayConstr (List(...))
    }

    locally {
      val test = new SAMTest
      test.javaCallable()
      test.specialResultTypes()
      test.javaComparator()
      // TODO: expected (ref $type), found (ref extern)
      // test.samHasDefaultMethod()
      test.specialParamTypes()
      test.nonLFMCapableSAM()
    }

    locally {
      val test = new SAMWithOverridingBridgesTest
      import test._
      testVariantA()
      testVariantB()
    }

    locally {
      (new ShortTest).toShort()
    }
    locally {
      (new StoreModuleTest).scalaModuleClass()
    }

    locally {
      val test = new UnitTest
      test.testHashCode()
      test.testEquals()
      test.testEqualsOtherValues()
    }

  }
}