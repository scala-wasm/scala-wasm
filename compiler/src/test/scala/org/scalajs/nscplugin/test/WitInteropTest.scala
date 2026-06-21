/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package org.scalajs.nscplugin.test

import org.scalajs.nscplugin.test.util._

import org.junit.Test

class WitInteropTest extends DirectTest with TestHelpers {

  override def preamble: String =
    """
    import scala.scalajs.{wit => wm}
    import scala.scalajs.wit.annotation._
    import scala.scalajs.wit.unsigned._
    """

  @Test def resourceImportMustBeOnTrait: Unit = {
    """
    @WitResourceImport("test:module", "resource")
    class MyResource
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitResourceImport is allowed for traits
      |    class MyResource
      |          ^
    """

    """
    @WitResourceImport("test:module", "resource")
    object MyResource
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitResourceImport is allowed for traits
      |    object MyResource
      |           ^
    """
  }

  @Test def resourceImportCannotBeSealed: Unit = {
    """
    @WitResourceImport("test:module", "resource")
    sealed trait MyResource
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitResourceImport traits cannot be sealed
      |    sealed trait MyResource
      |                 ^
    """
  }

  @Test def resourceMethodsMustHaveAnnotation: Unit = {
    """
    @WitResourceImport("test:module", "resource")
    trait MyResource {
      def doSomething(): Unit
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: Method 'doSomething' in @WitResourceImport trait must be annotated with @WitResourceMethod or @WitResourceDrop
      |      def doSomething(): Unit
      |          ^
    """

    """
    @WitResourceImport("test:module", "resource")
    trait MyResource {
      def method1(): Unit
      def method2(x: Int): String
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: Method 'method1' in @WitResourceImport trait must be annotated with @WitResourceMethod or @WitResourceDrop
      |      def method1(): Unit
      |          ^
      |newSource1.scala:9: error: Method 'method2' in @WitResourceImport trait must be annotated with @WitResourceMethod or @WitResourceDrop
      |      def method2(x: Int): String
      |          ^
    """

    """
    @WitResourceImport("test:module", "resource")
    trait MyResource {
      @WitResourceMethod("annotated")
      def annotated(): Unit = wm.native

      def unannotated(): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:11: error: Method 'unannotated' in @WitResourceImport trait must be annotated with @WitResourceMethod or @WitResourceDrop
      |      def unannotated(): Unit = wm.native
      |          ^
    """
  }

  @Test def resourceMethodParametersMustBeCompatible: Unit = {
    """
    class NotCompatible

    @WitResourceImport("test:module", "resource")
    trait MyResource {
      @WitResourceMethod("invalid")
      def invalidMethod(x: NotCompatible): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:11: error: Parameter type 'NotCompatible' in method 'invalidMethod' is not compatible with Component Model
      |      def invalidMethod(x: NotCompatible): Unit = wm.native
      |          ^
    """
  }

  @Test def resourceMethodReturnTypeMustBeCompatible: Unit = {
    """
    class NotCompatible

    @WitResourceImport("test:module", "resource")
    trait MyResource {
      @WitResourceMethod("invalid")
      def invalidMethod(): NotCompatible = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:11: error: Return type 'NotCompatible' in method 'invalidMethod' is not compatible with Component Model
      |      def invalidMethod(): NotCompatible = wm.native
      |          ^
    """
  }

  @Test def resourceCompanionObjectMethodsMustHaveAnnotation: Unit = {
    """
    @WitResourceImport("test:module", "resource")
    trait MyResource {
      @WitResourceMethod("do-something")
      def doSomething(): Unit = wm.native
    }
    object MyResource {
      def invalidMethod(): Unit = ???
    }
    """ hasErrors
    """
      |newSource1.scala:12: error: Public method 'invalidMethod' in companion object of @WitResourceImport trait must be annotated with @WitResourceConstructor or @WitResourceStaticMethod
      |      def invalidMethod(): Unit = ???
      |          ^
    """
  }

  @Test def resourceDropMustHaveNoParametersAndReturnUnit: Unit = {
    """
    @WitResourceImport("test:module", "resource")
    trait MyResource {
      @WitResourceDrop
      def close(x: Int): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:9: error: @WitResourceDrop method must take no parameters
      |      def close(x: Int): Unit = wm.native
      |          ^
    """

    """
    @WitResourceImport("test:module", "resource")
    trait MyResource {
      @WitResourceDrop
      def close(): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:9: error: @WitResourceDrop method must return Unit
      |      def close(): Int = wm.native
      |          ^
    """
  }

  @Test def resourceCanHaveAtMostOneDropMethod: Unit = {
    """
    @WitResourceImport("test:module", "resource")
    trait MyResource {
      @WitResourceDrop
      def close(): Unit = wm.native

      @WitResourceDrop
      def dispose(): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitResourceImport trait can have at most one @WitResourceDrop method, found 2
      |    trait MyResource {
      |          ^
    """
  }

  @Test def resourceConstructorOnlyInCompanionObject: Unit = {
    """
    @WitResourceImport("test:module", "resource")
    trait MyResource {
      @WitResourceConstructor
      def create(): MyResource = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:9: error: @WitResourceConstructor can only be used on apply method in companion object
      |      def create(): MyResource = wm.native
      |          ^
    """
  }

  @Test def resourceStaticMethodOnlyInCompanionObject: Unit = {
    """
    @WitResourceImport("test:module", "resource")
    trait MyResource {
      @WitResourceStaticMethod("factory")
      def factory(): MyResource = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:9: error: @WitResourceStaticMethod can only be used in companion object
      |      def factory(): MyResource = wm.native
      |          ^
    """
  }

  @Test def resourceConstructorMustBeOnApply: Unit = {
    """
    @WitResourceImport("test:module", "resource")
    trait MyResource
    object MyResource {
      @WitResourceConstructor
      def create(): MyResource = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:10: error: @WitResourceConstructor can only be used on apply method
      |      def create(): MyResource = wm.native
      |          ^
    """
  }

  @Test def resourceAnnotationsOnlyInResourceImportTraits: Unit = {
    """
    trait NotAResource {
      @WitResourceMethod("invalid")
      def method(): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitResourceMethod("invalid") is allowed in trait annotated with @WitResourceImport
      |      def method(): Unit = wm.native
      |          ^
    """

    """
    trait NotAResource {
      @WitResourceDrop
      def drop(): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitResourceDrop is allowed in trait annotated with @WitResourceImport
      |      def drop(): Unit = wm.native
      |          ^
    """
  }

  @Test def resourceConstructorAnnotationsOnlyInResourceCompanions: Unit = {
    """
    object NotAResourceCompanion {
      @WitResourceConstructor
      def apply(): String = ???
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitResourceConstructor is allowed in companion object of trait annotated with @WitResourceImport
      |      def apply(): String = ???
      |          ^
    """

    """
    object NotAResourceCompanion {
      @WitResourceStaticMethod("foo")
      def foo(): String = ???
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitResourceStaticMethod("foo") is allowed in companion object of trait annotated with @WitResourceImport
      |      def foo(): String = ???
      |          ^
    """
  }

  @Test def resourceValidExample: Unit = {
    """
    @WitResourceImport("test:io/streams", "input-stream")
    trait InputStream {
      @WitResourceMethod("read")
      def read(len: ULong): wm.Result[Array[UByte], String] = wm.native

      @WitResourceMethod("blocking-read")
      def blockingRead(len: ULong): wm.Result[Array[UByte], String] = wm.native

      @WitResourceDrop
      def close(): Unit = wm.native
    }

    @WitResourceImport("test:io/streams", "output-stream")
    trait OutputStream {
      @WitResourceMethod("write")
      def write(data: Array[UByte]): wm.Result[Unit, String] = wm.native

      @WitResourceMethod("flush")
      def flush(): wm.Result[Unit, String] = wm.native

      @WitResourceDrop
      def close(): Unit = wm.native
    }
    object OutputStream {
      @WitResourceConstructor
      def apply(): OutputStream = wm.native

      @WitResourceConstructor
      def apply(x: Int): OutputStream = wm.native
    }
    """.hasNoWarns()
  }

  // --- Component Variant Tests ---

  @Test def variantMustBeSealed: Unit = {
    """
    @WitVariant
    trait NotSealed
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitVariant can only be used on sealed traits or sealed abstract classes
      |    trait NotSealed
      |          ^
    """
  }

  @Test def variantMustHaveAtLeastOneCase: Unit = {
    """
    @WitVariant
    sealed trait Empty
    """ hasErrors
    """
      |newSource1.scala:7: error: Component variant 'Empty' must have at least one case
      |    sealed trait Empty
      |                 ^
    """
  }

  @Test def variantCasesMustBeCaseClassOrObject: Unit = {
    """
    @WitVariant
    sealed trait MyVariant
    object MyVariant {
      class NotACase extends MyVariant
    }
    """ hasErrors
    """
      |newSource1.scala:9: error: Component variant case 'NotACase' must be a case class or case object
      |      class NotACase extends MyVariant
      |            ^
    """
  }

  @Test def variantCaseClassMustHaveAtMostOneField: Unit = {
    """
    @WitVariant
    sealed trait MyVariant
    object MyVariant {
      final case class TooManyFields(x: Int, y: String) extends MyVariant
    }
    """ hasErrors
    """
      |newSource1.scala:9: error: Component variant case 'TooManyFields' must have exactly one field, found 2.
      |      final case class TooManyFields(x: Int, y: String) extends MyVariant
      |                       ^
    """
  }

  @Test def variantCaseFieldMustBeNamedValue: Unit = {
    """
    @WitVariant
    sealed trait MyVariant
    object MyVariant {
      final case class WrongName(data: Int) extends MyVariant
    }
    """ hasErrors
    """
      |newSource1.scala:9: error: Component variant case 'WrongName' field must be named 'value', found 'data'.
      |      final case class WrongName(data: Int) extends MyVariant
      |                                 ^
    """
  }

  @Test def variantCaseFieldMustBeCompatibleType: Unit = {
    """
    class NotCompatible

    @WitVariant
    sealed trait MyVariant
    object MyVariant {
      final case class InvalidType(value: NotCompatible) extends MyVariant
    }
    """ hasErrors
    """
      |newSource1.scala:11: error: Field 'value' has type 'NotCompatible' which is not compatible with Component Model.
      |      final case class InvalidType(value: NotCompatible) extends MyVariant
      |                                   ^
    """
  }

  @Test def variantValidCaseClassWithValue: Unit = {
    """
    @WitVariant
    sealed trait MyVariant
    object MyVariant {
      final case class IntValue(value: Int) extends MyVariant
      final case class StringValue(value: String) extends MyVariant
      final case object None extends MyVariant
    }
    """.hasNoWarns()
  }

  // --- Component Record Tests ---

  @Test def recordMustBeCaseClass: Unit = {
    """
    @WitRecord
    class NotCaseClass(x: Int, y: Int)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitRecord can only be used on case classes
      |    class NotCaseClass(x: Int, y: Int)
      |          ^
    """

    """
    @WitRecord
    trait NotCaseClass
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitRecord can only be used on case classes
      |    trait NotCaseClass
      |          ^
    """

    """
    @WitRecord
    object NotCaseClass
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitRecord can only be used on case classes
      |    object NotCaseClass
      |           ^
    """
  }

  @Test def recordMustBeFinal: Unit = {
    """
    @WitRecord
    case class NotFinal(x: Int, y: Int)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitRecord case class must be final
      |    case class NotFinal(x: Int, y: Int)
      |               ^
    """
  }

  @Test def recordFieldsMustBeCompatible: Unit = {
    """
    class NotCompatible

    @WitRecord
    final case class InvalidRecord(x: Int, y: NotCompatible)
    """ hasErrors
    """
      |newSource1.scala:9: error: Field 'y' has type 'NotCompatible' which is not compatible with Component Model
      |    final case class InvalidRecord(x: Int, y: NotCompatible)
      |                                           ^
    """

    """
    class NotCompatible

    @WitRecord
    final case class MultipleInvalid(a: NotCompatible, b: String, c: NotCompatible)
    """ hasErrors
    """
      |newSource1.scala:9: error: Field 'a' has type 'NotCompatible' which is not compatible with Component Model
      |    final case class MultipleInvalid(a: NotCompatible, b: String, c: NotCompatible)
      |                                     ^
      |newSource1.scala:9: error: Field 'c' has type 'NotCompatible' which is not compatible with Component Model
      |    final case class MultipleInvalid(a: NotCompatible, b: String, c: NotCompatible)
      |                                                                  ^
    """
  }

  @Test def recordValidExamples: Unit = {
    """
    @WitRecord
    final case class Point(x: Int, y: Int)

    @WitRecord
    final case class Person(name: String, age: Int)

    @WitRecord
    final case class Empty()
    """.hasNoWarns()
  }

  // --- Component Flags Tests ---

  @Test def flagsMustBeCaseClass: Unit = {
    """
    @WitFlags(1)
    class NotCaseClass(value: Int)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags can only be used on case classes
      |    class NotCaseClass(value: Int)
      |          ^
    """

    """
    @WitFlags(1)
    trait NotCaseClass
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags can only be used on case classes
      |    trait NotCaseClass
      |          ^
    """
  }

  @Test def flagsMustBeFinal: Unit = {
    """
    @WitFlags(1)
    case class NotFinal(value: Int)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags case class must be final
      |    case class NotFinal(value: Int)
      |               ^
    """
  }

  @Test def flagsMustNotExtendAnyVal: Unit = {
    """
    @WitFlags(1)
    final case class ValueClass(value: Int) extends AnyVal
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags case class must NOT extend AnyVal. Use a regular case class instead.
      |    final case class ValueClass(value: Int) extends AnyVal
      |                     ^
    """
  }

  @Test def flagsMustHaveOneParameter: Unit = {
    """
    @WitFlags(1)
    final case class NoParameters()
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags case class must have exactly one parameter, found 0
      |    final case class NoParameters()
      |                     ^
    """

    """
    @WitFlags(2)
    final case class TwoParameters(value: Int, other: Int)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags case class must have exactly one parameter, found 2
      |    final case class TwoParameters(value: Int, other: Int)
      |                     ^
    """
  }

  @Test def flagsParameterMustBeIntNamedValue: Unit = {
    """
    @WitFlags(1)
    final case class WrongType(value: String)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags case class parameter must be of type Int, found 'String'
      |    final case class WrongType(value: String)
      |                               ^
    """

    """
    @WitFlags(1)
    final case class WrongName(data: Int)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags case class parameter must be named 'value', found 'data'
      |    final case class WrongName(data: Int)
      |                               ^
    """
  }

  @Test def flagsValidExamples: Unit = {
    """
    @WitFlags(3)
    final case class MyFlags(value: Int) {
      def |(other: MyFlags): MyFlags = MyFlags(value | other.value)
      def &(other: MyFlags): MyFlags = MyFlags(value & other.value)
    }
    object MyFlags {
      val Flag0 = MyFlags(1 << 0)
      val Flag1 = MyFlags(1 << 1)
      val Flag2 = MyFlags(1 << 2)
    }

    @WitFlags(2)
    final case class SimpleFlags(value: Int)
    object SimpleFlags {
      val A = SimpleFlags(1 << 0)
      val B = SimpleFlags(1 << 1)
    }
    """.hasNoWarns()
  }

  // --- Component Import Function Tests ---

  @Test def witImportMustBeInPublicObject: Unit = {
    """
    class MyClass {
      @WitImport("test:module", "in-class")
      def inClass(x: Int): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport("test:module", "in-class") methods must be defined in a public object
      |      def inClass(x: Int): Int = wm.native
      |          ^
    """

    """
    trait MyTrait {
      @WitImport("test:module", "in-trait")
      def inTrait(x: Int): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport("test:module", "in-trait") methods must be defined in a public object
      |      def inTrait(x: Int): Int = wm.native
      |          ^
    """

    """
    private object PrivateObject {
      @WitImport("test:module", "in-private")
      def inPrivate(x: Int): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport("test:module", "in-private") methods must be defined in a public object
      |      def inPrivate(x: Int): Int = wm.native
      |          ^
    """
  }

  @Test def witImportMustBePublic: Unit = {
    """
    object MyFunctions {
      @WitImport("test:module", "private-func")
      private def privateFunc(x: Int): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport("test:module", "private-func") methods must be public
      |      private def privateFunc(x: Int): Int = wm.native
      |                  ^
    """

    """
    object MyFunctions {
      @WitImport("test:module", "protected-func")
      protected def protectedFunc(x: Int): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport("test:module", "protected-func") methods must be public
      |      protected def protectedFunc(x: Int): Int = wm.native
      |                    ^
    """
  }

  @Test def witImportCannotHaveTypeParameters: Unit = {
    """
    object MyFunctions {
      @WitImport("test:module", "generic-func")
      def genericFunc[T](x: T): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport("test:module", "generic-func") methods cannot have type parameters
      |      def genericFunc[T](x: T): Int = wm.native
      |          ^
    """
  }

  @Test def witImportCannotHaveRepeatedParameters: Unit = {
    """
    object MyFunctions {
      @WitImport("test:module", "varargs-func")
      def varargsFunc(xs: Int*): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport("test:module", "varargs-func") methods may not have repeated parameters
      |      def varargsFunc(xs: Int*): Unit = wm.native
      |          ^
    """
  }

  @Test def witImportCannotHaveDefaultParameters: Unit = {
    """
    object MyFunctions {
      @WitImport("test:module", "default-param")
      def defaultParam(x: Int = 42): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport("test:module", "default-param") methods may not have default parameters
      |      def defaultParam(x: Int = 42): Int = wm.native
      |          ^
    """
  }

  @Test def witImportParametersMustBeCompatible: Unit = {
    """
    class NotCompatible

    object MyFunctions {
      @WitImport("test:module", "invalid-param")
      def invalidParam(x: NotCompatible): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:10: error: Parameter 'x' has type 'NotCompatible' which is not compatible with Component Model
      |      def invalidParam(x: NotCompatible): Unit = wm.native
      |          ^
    """
  }

  @Test def witImportReturnTypeMustBeCompatible: Unit = {
    """
    class NotCompatible

    object MyFunctions {
      @WitImport("test:module", "invalid-return")
      def invalidReturn(x: Int): NotCompatible = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:10: error: Return type 'NotCompatible' is not compatible with Component Model
      |      def invalidReturn(x: Int): NotCompatible = wm.native
      |          ^
    """
  }

  @Test def witImportCannotOverride: Unit = {
    """
    trait Base {
      def baseMethod(): Int
    }

    object MyFunctions extends Base {
      @WitImport("test:module", "override-func")
      def baseMethod(): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:12: error: An scala.scalajs.wit.annotation.WitImport("test:module", "override-func") member cannot implement the inherited member Base.baseMethod
      |      def baseMethod(): Int = wm.native
      |          ^
    """
  }

  @Test def witImportCannotBeOnLocalDefinition: Unit = {
    """
    object MyFunctions {
      def outer(): Unit = {
        @WitImport("test:module", "local-func")
        def localFunc(x: Int): Int = wm.native
      }
    }
    """ hasErrors
    """
      |newSource1.scala:9: error: scala.scalajs.wit.annotation.WitImport("test:module", "local-func") is not allowed on local definitions
      |        def localFunc(x: Int): Int = wm.native
      |            ^
    """
  }

  @Test def witImportCannotBeOnConstructor: Unit = {
    """
    class MyClass @WitImport("test:module", "ctor")() {}
    """ hasErrors
    """
      |newSource1.scala:7: error: scala.scalajs.wit.annotation.WitImport("test:module", "ctor") is not allowed on constructor
      |
      |   ^
    """
  }

  @Test def witImportValidExamples: Unit = {
    """
    object MyImports {
      @WitImport("test:module", "add")
      def add(a: Int, b: Int): Int = wm.native

      @WitImport("test:module", "greet")
      def greet(name: String): String = wm.native

      @WitImport("test:module", "process")
      def process(data: Array[UByte]): wm.Result[Unit, String] = wm.native

      @WitImport("test:module", "no-params")
      def noParams(): Unit = wm.native

      @WitImport("test:module", "returns-optional")
      def returnsOptional(x: Int): java.util.Optional[String] = wm.native
    }

    @WitRecord
    final case class Point(x: Int, y: Int)

    object MoreImports {
      @WitImport("test:geom", "distance")
      def distance(p1: Point, p2: Point): Double = wm.native
    }
    """.hasNoWarns()
  }

  // --- Component Export Interface Tests ---

  @Test def exportInterfaceMustBeOnTrait: Unit = {
    """
    @WitExportInterface
    class NotATrait {
      @WitExport("test:module", "func")
      def func(): Unit
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: @WitExport can only be used in @WitExportInterface traits.
      |      @WitExport("test:module", "func")
      |       ^
    """

    """
    @WitExportInterface
    object NotATrait {
      @WitExport("test:module", "func")
      def func(): Unit = ???
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: @WitExport can only be used in @WitExportInterface traits.
      |      @WitExport("test:module", "func")
      |       ^
    """
  }

  @Test def exportInterfaceCannotHaveConcreteMethod: Unit = {
    """
    @WitExportInterface
    trait MyExport {
      @WitExport("test:module", "concrete")
      def concreteMethod(): Unit = {
        println("concrete")
      }
    }
    """ hasErrors
    """
      |newSource1.scala:9: error: @WitExportInterface trait cannot contain concrete method implementations. Method 'concreteMethod' must be abstract.
      |      def concreteMethod(): Unit = {
      |          ^
    """
  }

  @Test def exportInterfaceMethodsMustHaveAnnotation: Unit = {
    """
    @WitExportInterface
    trait MyExport {
      @WitExport("test:module", "annotated")
      def annotated(): Unit

      def unannotated(): Unit
    }
    """ hasErrors
    """
      |newSource1.scala:11: error: All methods in @WitExportInterface trait must be annotated with @WitExport. Method 'unannotated' is missing the annotation.
      |      def unannotated(): Unit
      |          ^
    """
  }

  @Test def exportInterfaceCannotHaveNonMethodMembers: Unit = {
    """
    @WitExportInterface
    trait MyExport {
      @WitExport("test:module", "method")
      def method(): Unit

      val value: Int = 42
    }
    """ hasErrors
    """
      |newSource1.scala:11: error: @WitExportInterface trait cannot contain concrete method implementations. Method 'value' must be abstract.
      |      val value: Int = 42
      |          ^
    """

    """
    @WitExportInterface
    trait MyExport {
      var mutableValue: String = "foo"
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: @WitExportInterface trait cannot contain concrete method implementations. Method 'mutableValue' must be abstract.
      |      var mutableValue: String = "foo"
      |          ^
    """
  }

  @Test def exportInterfaceExtendedByClassOrTrait: Unit = {

    """
    @WitExportInterface
    trait MyExport {
      @WitExport("test:module", "method")
      def method(): Unit
    }

    @WitImplementation
    class MyClass extends MyExport {
      override def method(): Unit = ???
    }
    """ hasErrors
    """
      |newSource1.scala:13: error: @WitExportInterface trait MyExport cannot be extended by a class. Use an object annotated with @WitImplementation instead.
      |    class MyClass extends MyExport {
      |          ^
    """

    """
    @WitExportInterface
    trait MyExport {
      @WitExport("test:module", "method")
      def method(): Unit
    }

    @WitImplementation
    trait AnotherTrait extends MyExport {
      override def method(): Unit = ???
    }
    """ hasErrors
    """
      |newSource1.scala:13: error: @WitExportInterface trait MyExport cannot be extended by another trait. Use an object annotated with @WitImplementation instead.
      |    trait AnotherTrait extends MyExport {
      |          ^
    """

    """
    @WitExportInterface
    trait MyExport {
      @WitExport("test:module", "method")
      def method(): Unit
    }

    object MyObjectWithoutAnnotation extends MyExport {
      override def method(): Unit = ???
    }
    """ hasErrors
    """
      |newSource1.scala:12: error: Object MyObjectWithoutAnnotation extends @WitExportInterface trait MyExport but is not annotated with @WitImplementation. Add @WitImplementation annotation to the object.
      |    object MyObjectWithoutAnnotation extends MyExport {
      |           ^
    """
  }

  // --- Component Implementation Tests ---

  @Test def witImplementationMustExtendExportInterface: Unit = {
    """
    trait PlainTrait {
      def method(): Unit
    }

    @WitImplementation
    object MyImpl extends PlainTrait {
      override def method(): Unit = ???
    }
    """ hasErrors
    """
      |newSource1.scala:11: error: @WitImplementation object must extend a trait annotated with @WitExportInterface
      |    object MyImpl extends PlainTrait {
      |           ^
      |newSource1.scala:12: error: @WitImplementation object MyImpl must extend a trait annotated with @WitExportInterface
      |      override def method(): Unit = ???
      |                   ^
    """

    """
    @WitImplementation
    object StandaloneImpl {
      def method(): Unit = ???
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitImplementation object must extend a trait annotated with @WitExportInterface
      |    object StandaloneImpl {
      |           ^
      |newSource1.scala:8: error: @WitImplementation object StandaloneImpl must extend a trait annotated with @WitExportInterface
      |      def method(): Unit = ???
      |          ^
    """
  }

  @Test def witImplementationMustImplementAllMethods: Unit = {
    """
    @WitExportInterface
    trait MyExport {
      @WitExport("test:module", "method1")
      def method1(): Unit

      @WitExport("test:module", "method2")
      def method2(x: Int): String
    }

    @WitImplementation
    object IncompleteImpl extends MyExport {
      override def method1(): Unit = ???
      // missing method2
    }
    """ hasErrors
    """
      |newSource1.scala:16: error: Must implement method method2 from trait MyExport
      |    object IncompleteImpl extends MyExport {
      |           ^
    """
  }

  @Test def witImplementationMethodSignatureMustMatch: Unit = {
    """
    @WitExportInterface
    trait MyExport {
      @WitExport("test:module", "method")
      def method(x: Int): String
    }

    @WitImplementation
    object WrongSignature extends MyExport {
      override def method(x: Int): Int = 42  // Wrong return type
    }
    """ hasErrors
    """
      |newSource1.scala:14: error: overriding method method in trait MyExport of type (x: Int)String;
      | method method has incompatible type
      |      override def method(x: Int): Int = 42  // Wrong return type
      |                   ^
    """
  }

  @Test def validWitExportExamples: Unit = {
    """
    @WitExportInterface
    trait Run {
      @WitExport("wasi:cli/run@0.2.0", "run")
      def run(): wm.Result[Unit, Unit]
    }

    @WitImplementation
    object RunImpl extends Run {
      override def run(): wm.Result[Unit, Unit] = {
        println("Hello!")
        new wm.Ok(())
      }
    }

    @WitExportInterface
    trait MyAPI {
      @WitExport("test:api", "get-data")
      def getData(id: Int): wm.Result[String, String]

      @WitExport("test:api", "process")
      def process(data: Array[UByte]): Unit
    }

    @WitImplementation
    object MyAPIImpl extends MyAPI {
      override def getData(id: Int): wm.Result[String, String] =
        new wm.Ok(s"data-$id")

      override def process(data: Array[UByte]): Unit = {
        // Process data
      }
    }
    """.hasNoWarns()
  }
}
