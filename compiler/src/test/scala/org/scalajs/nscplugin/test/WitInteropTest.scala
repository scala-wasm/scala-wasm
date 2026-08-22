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

  @Test def resourceImportMustBeOnFinalClass: Unit = {
    """
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    class MyResource
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitResourceImport is allowed for final classes
      |    class MyResource
      |          ^
    """

    """
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    object MyResource
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitResourceImport is allowed for final classes
      |    object MyResource
      |           ^
    """

    """
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    trait MyResource
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitResourceImport is allowed for final classes
      |    trait MyResource
      |          ^
    """
  }

  @Test def resourceImportCannotBeSealed: Unit = {
    """
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    sealed final class MyResource private () extends Object
    """ hasErrors
    """
      |newSource1.scala:7: error: illegal combination of modifiers: final and sealed for: class MyResource
      |    sealed final class MyResource private () extends Object
      |                       ^
    """
  }

  @Test def resourceMethodsMustHaveAnnotation: Unit = {
    """
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    final class MyResource private () extends Object {
      def doSomething(): Unit = ???
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: Method 'doSomething' in @WitResourceImport class must be annotated with @WitResourceMethod or @WitResourceDrop
      |      def doSomething(): Unit = ???
      |          ^
    """

    """
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    final class MyResource private () extends Object {
      def method1(): Unit = ???
      def method2(x: Int): String = ???
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: Method 'method1' in @WitResourceImport class must be annotated with @WitResourceMethod or @WitResourceDrop
      |      def method1(): Unit = ???
      |          ^
      |newSource1.scala:9: error: Method 'method2' in @WitResourceImport class must be annotated with @WitResourceMethod or @WitResourceDrop
      |      def method2(x: Int): String = ???
      |          ^
    """

    """
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    final class MyResource private () extends Object {
      @WitResourceMethod("annotated")
      def annotated(): Unit = wm.native

      def unannotated(): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:11: error: Method 'unannotated' in @WitResourceImport class must be annotated with @WitResourceMethod or @WitResourceDrop
      |      def unannotated(): Unit = wm.native
      |          ^
    """
  }

  @Test def resourceMethodParametersMustBeCompatible: Unit = {
    """
    class NotCompatible

    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    final class MyResource private () extends Object {
      @WitResourceMethod("invalid")
      def invalidMethod(@WitName("x") x: NotCompatible): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:11: error: Parameter type 'NotCompatible' in method 'invalidMethod' is not compatible with Component Model
      |      def invalidMethod(@WitName("x") x: NotCompatible): Unit = wm.native
      |          ^
    """
  }

  @Test def resourceMethodReturnTypeMustBeCompatible: Unit = {
    """
    class NotCompatible

    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    final class MyResource private () extends Object {
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
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    final class MyResource private () extends Object {
      @WitResourceMethod("do-something")
      def doSomething(): Unit = wm.native
    }
    object MyResource {
      def invalidMethod(): Unit = ???
    }
    """ hasErrors
    """
      |newSource1.scala:12: error: Public method 'invalidMethod' in companion object of @WitResourceImport class must be annotated with @WitResourceConstructor or @WitResourceStaticMethod
      |      def invalidMethod(): Unit = ???
      |          ^
    """
  }

  @Test def resourceDropMustHaveNoParametersAndReturnUnit: Unit = {
    """
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    final class MyResource private () extends Object {
      @WitResourceDrop
      def close(@WitName("x") x: Int): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:9: error: @WitResourceDrop method must take no parameters
      |      def close(@WitName("x") x: Int): Unit = wm.native
      |          ^
    """

    """
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    final class MyResource private () extends Object {
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
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    final class MyResource private () extends Object {
      @WitResourceDrop
      def close(): Unit = wm.native

      @WitResourceDrop
      def dispose(): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitResourceImport class can have at most one @WitResourceDrop method, found 2
      |    final class MyResource private () extends Object {
      |                ^
    """
  }

  @Test def resourceConstructorOnlyInCompanionObject: Unit = {
    """
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    final class MyResource private () extends Object {
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
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    final class MyResource private () extends Object {
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
    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "resource")
    final class MyResource private () extends Object
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

  @Test def resourceAnnotationsOnlyInResourceImportClasses: Unit = {
    """
    final class NotAResource private () extends Object {
      @WitResourceMethod("invalid")
      def method(): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitResourceMethod("invalid") is allowed in final class annotated with @WitResourceImport
      |      def method(): Unit = wm.native
      |          ^
    """

    """
    final class NotAResource private () extends Object {
      @WitResourceDrop
      def drop(): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitResourceDrop is allowed in final class annotated with @WitResourceImport
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
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitResourceConstructor is allowed in companion object of class annotated with @WitResourceImport
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
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitResourceStaticMethod("foo") is allowed in companion object of class annotated with @WitResourceImport
      |      def foo(): String = ???
      |          ^
    """
  }

  @Test def resourceValidExample: Unit = {
    """
    @WitResourceImport(WitScope.unversioned("test", "io", "streams"), "input-stream")
    final class InputStream private () extends Object {
      @WitResourceMethod("read")
      def read(@WitName("len") len: ULong): wm.Result[Array[UByte], String] = wm.native

      @WitResourceMethod("blocking-read")
      def blockingRead(@WitName("len") len: ULong): wm.Result[Array[UByte], String] = wm.native

      @WitResourceDrop
      def close(): Unit = wm.native
    }
    object InputStream {}

    @WitResourceImport(WitScope.unversioned("test", "io", "streams"), "output-stream")
    final class OutputStream private () extends Object {
      @WitResourceMethod("write")
      def write(@WitName("data") data: Array[UByte]): wm.Result[Unit, String] = wm.native

      @WitResourceMethod("flush")
      def flush(): wm.Result[Unit, String] = wm.native

      @WitResourceDrop
      def close(): Unit = wm.native
    }
    object OutputStream {
      @WitResourceConstructor
      def apply(): OutputStream = wm.native

      @WitResourceConstructor
      def apply(@WitName("x") x: Int): OutputStream = wm.native
    }
    """.hasNoWarns()
  }

  // --- Component Variant Tests ---

  @Test def variantMustBeSealed: Unit = {
    """
    @WitVariant(WitScope.root, "my-variant")
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
    @WitVariant(WitScope.root, "my-variant")
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
    @WitVariant(WitScope.root, "my-variant")
    sealed trait MyVariant
    object MyVariant {
      @WitName("not-a-case")
      class NotACase extends MyVariant
    }
    """ hasErrors
    """
      |newSource1.scala:10: error: Component variant case 'NotACase' must be a final class or object
      |      class NotACase extends MyVariant
      |            ^
    """
  }

  @Test def variantCaseClassMustHaveAtMostOneField: Unit = {
    """
    @WitVariant(WitScope.root, "my-variant")
    sealed trait MyVariant
    object MyVariant {
      @WitName("too-many-fields")
      final case class TooManyFields(x: Int, y: String) extends MyVariant
    }
    """ hasErrors
    """
      |newSource1.scala:10: error: Component variant case 'TooManyFields' must have exactly one field, found 2.
      |      final case class TooManyFields(x: Int, y: String) extends MyVariant
      |                       ^
    """
  }

  @Test def variantCaseFieldMustBeNamedValue: Unit = {
    """
    @WitVariant(WitScope.root, "my-variant")
    sealed trait MyVariant
    object MyVariant {
      @WitName("wrong-name")
      final case class WrongName(data: Int) extends MyVariant
    }
    """ hasErrors
    """
      |newSource1.scala:10: error: Component variant case 'WrongName' field must be named 'value', found 'data'.
      |      final case class WrongName(data: Int) extends MyVariant
      |                                 ^
    """
  }

  @Test def variantCaseFieldMustBeCompatibleType: Unit = {
    """
    class NotCompatible

    @WitVariant(WitScope.root, "my-variant")
    sealed trait MyVariant
    object MyVariant {
      @WitName("invalid-type")
      final case class InvalidType(value: NotCompatible) extends MyVariant
    }
    """ hasErrors
    """
      |newSource1.scala:12: error: Field 'value' has type 'NotCompatible' which is not compatible with Component Model.
      |      final case class InvalidType(value: NotCompatible) extends MyVariant
      |                                   ^
    """
  }

  @Test def variantValidCaseClassWithValue: Unit = {
    """
    @WitVariant(WitScope.root, "my-variant")
    sealed trait MyVariant
    object MyVariant {
      @WitName("int-value")
      final case class IntValue(value: Int) extends MyVariant
      @WitName("string-value")
      final case class StringValue(value: String) extends MyVariant
      @WitName("none")
      final case object None extends MyVariant
    }
    """.hasNoWarns()
  }

  @Test def variantCasesMustHaveWitName: Unit = {
    """
    @WitVariant(WitScope.root, "my-variant")
    sealed trait MyVariant
    object MyVariant {
      final case class MissingName(value: Int) extends MyVariant
    }
    """ hasErrors
    """
      |newSource1.scala:9: error: Component variant case 'MissingName' must have a @WitName annotation
      |      final case class MissingName(value: Int) extends MyVariant
      |                       ^
    """
  }

  // --- Component Record Tests ---

  @Test def recordMustBeCaseClass: Unit = {
    """
    @WitRecord(WitScope.root, "record")
    class NotCaseClass(x: Int, y: Int)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitRecord class must be final
      |    class NotCaseClass(x: Int, y: Int)
      |          ^
    """

    """
    @WitRecord(WitScope.root, "record")
    trait NotCaseClass
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitRecord can only be used on classes
      |    trait NotCaseClass
      |          ^
    """

    """
    @WitRecord(WitScope.root, "record")
    object NotCaseClass
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitRecord can only be used on classes
      |    object NotCaseClass
      |           ^
    """
  }

  @Test def recordMustBeFinal: Unit = {
    """
    @WitRecord(WitScope.root, "record")
    case class NotFinal(x: Int, y: Int)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitRecord class must be final
      |    case class NotFinal(x: Int, y: Int)
      |               ^
    """
  }

  @Test def recordFieldsMustBeCompatible: Unit = {
    """
    class NotCompatible

    @WitRecord(WitScope.root, "record")
    final case class InvalidRecord(@WitName("x") x: Int, @WitName("y") y: NotCompatible)
    """ hasErrors
    """
      |newSource1.scala:9: error: Field 'y' has type 'NotCompatible' which is not compatible with Component Model
      |    final case class InvalidRecord(@WitName("x") x: Int, @WitName("y") y: NotCompatible)
      |                                                                       ^
    """

    """
    class NotCompatible

    @WitRecord(WitScope.root, "record")
    final case class MultipleInvalid(@WitName("a") a: NotCompatible, @WitName("b") b: String, @WitName("c") c: NotCompatible)
    """ hasErrors
    """
      |newSource1.scala:9: error: Field 'a' has type 'NotCompatible' which is not compatible with Component Model
      |    final case class MultipleInvalid(@WitName("a") a: NotCompatible, @WitName("b") b: String, @WitName("c") c: NotCompatible)
      |                                                   ^
      |newSource1.scala:9: error: Field 'c' has type 'NotCompatible' which is not compatible with Component Model
      |    final case class MultipleInvalid(@WitName("a") a: NotCompatible, @WitName("b") b: String, @WitName("c") c: NotCompatible)
      |                                                                                                            ^
    """
  }

  @Test def recordValidExamples: Unit = {
    """
    @WitRecord(WitScope.root, "point")
    final case class Point(@WitName("x") x: Int, @WitName("y") y: Int)

    @WitRecord(WitScope.root, "person")
    final case class Person(@WitName("name") name: String, @WitName("age") age: Int)

    @WitRecord(WitScope.root, "empty")
    final case class Empty()
    """.hasNoWarns()
  }

  @Test def recordFieldsMustHaveWitName: Unit = {
    """
    @WitRecord(WitScope.root, "record")
    final case class MissingName(x: Int)
    """ hasErrors
    """
      |newSource1.scala:7: error: Field 'x' must have a @WitName annotation
      |    final case class MissingName(x: Int)
      |                                 ^
    """
  }

  // --- Component Flags Tests ---

  @Test def flagsMustBeCaseClass: Unit = {
    """
    @WitFlags(WitScope.root, "flags", Array("a"))
    class NotCaseClass(value: Int)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags class must be final
      |    class NotCaseClass(value: Int)
      |          ^
    """

    """
    @WitFlags(WitScope.root, "flags", Array("a"))
    trait NotCaseClass
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags can only be used on classes
      |    trait NotCaseClass
      |          ^
    """
  }

  @Test def flagsMustBeFinal: Unit = {
    """
    @WitFlags(WitScope.root, "flags", Array("a"))
    case class NotFinal(value: Int)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags class must be final
      |    case class NotFinal(value: Int)
      |               ^
    """
  }

  @Test def flagsMustNotExtendAnyVal: Unit = {
    """
    @WitFlags(WitScope.root, "flags", Array("a"))
    final case class ValueClass(value: Int) extends AnyVal
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags class must NOT extend AnyVal. Use a regular class instead.
      |    final case class ValueClass(value: Int) extends AnyVal
      |                     ^
    """
  }

  @Test def flagsMustHaveOneParameter: Unit = {
    """
    @WitFlags(WitScope.root, "flags", Array("a"))
    final case class NoParameters()
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags class must have exactly one parameter, found 0
      |    final case class NoParameters()
      |                     ^
    """

    """
    @WitFlags(WitScope.root, "flags", Array("a", "b"))
    final case class TwoParameters(value: Int, other: Int)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags class must have exactly one parameter, found 2
      |    final case class TwoParameters(value: Int, other: Int)
      |                     ^
    """
  }

  @Test def flagsParameterMustBeIntNamedValue: Unit = {
    """
    @WitFlags(WitScope.root, "flags", Array("a"))
    final case class WrongType(value: String)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags class parameter must be of type Int, found 'String'
      |    final case class WrongType(value: String)
      |                               ^
    """

    """
    @WitFlags(WitScope.root, "flags", Array("a"))
    final case class WrongName(data: Int)
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitFlags class parameter must be named 'value', found 'data'
      |    final case class WrongName(data: Int)
      |                               ^
    """
  }

  @Test def flagsValidExamples: Unit = {
    """
    @WitFlags(WitScope.root, "my-flags", Array("flag0", "flag1", "flag2"))
    final case class MyFlags(value: Int) {
      def |(other: MyFlags): MyFlags = MyFlags(value | other.value)
      def &(other: MyFlags): MyFlags = MyFlags(value & other.value)
    }
    object MyFlags {
      val Flag0 = MyFlags(1 << 0)
      val Flag1 = MyFlags(1 << 1)
      val Flag2 = MyFlags(1 << 2)
    }

    @WitFlags(WitScope.root, "simple-flags", Array("a", "b"))
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
      @WitImport(WitScope.unversioned("test", "test", "module"), "in-class")
      def inClass(x: Int): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport(scala.scalajs.wit.annotation.WitScope.unversioned("test", "test", "module"), "in-class") methods must be defined in a public object
      |      def inClass(x: Int): Int = wm.native
      |          ^
    """

    """
    trait MyTrait {
      @WitImport(WitScope.unversioned("test", "test", "module"), "in-trait")
      def inTrait(x: Int): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport(scala.scalajs.wit.annotation.WitScope.unversioned("test", "test", "module"), "in-trait") methods must be defined in a public object
      |      def inTrait(x: Int): Int = wm.native
      |          ^
    """

    """
    private object PrivateObject {
      @WitImport(WitScope.unversioned("test", "test", "module"), "in-private")
      def inPrivate(x: Int): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport(scala.scalajs.wit.annotation.WitScope.unversioned("test", "test", "module"), "in-private") methods must be defined in a public object
      |      def inPrivate(x: Int): Int = wm.native
      |          ^
    """
  }

  @Test def witImportMustBePublic: Unit = {
    """
    object MyFunctions {
      @WitImport(WitScope.unversioned("test", "test", "module"), "private-func")
      private def privateFunc(x: Int): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport(scala.scalajs.wit.annotation.WitScope.unversioned("test", "test", "module"), "private-func") methods must be public
      |      private def privateFunc(x: Int): Int = wm.native
      |                  ^
    """

    """
    object MyFunctions {
      @WitImport(WitScope.unversioned("test", "test", "module"), "protected-func")
      protected def protectedFunc(x: Int): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport(scala.scalajs.wit.annotation.WitScope.unversioned("test", "test", "module"), "protected-func") methods must be public
      |      protected def protectedFunc(x: Int): Int = wm.native
      |                    ^
    """
  }

  @Test def witImportCannotHaveTypeParameters: Unit = {
    """
    object MyFunctions {
      @WitImport(WitScope.unversioned("test", "test", "module"), "generic-func")
      def genericFunc[T](x: T): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport(scala.scalajs.wit.annotation.WitScope.unversioned("test", "test", "module"), "generic-func") methods cannot have type parameters
      |      def genericFunc[T](x: T): Int = wm.native
      |          ^
    """
  }

  @Test def witImportCannotHaveRepeatedParameters: Unit = {
    """
    object MyFunctions {
      @WitImport(WitScope.unversioned("test", "test", "module"), "varargs-func")
      def varargsFunc(@WitName("xs") xs: Int*): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport(scala.scalajs.wit.annotation.WitScope.unversioned("test", "test", "module"), "varargs-func") methods may not have repeated parameters
      |      def varargsFunc(@WitName("xs") xs: Int*): Unit = wm.native
      |          ^
    """
  }

  @Test def witImportCannotHaveDefaultParameters: Unit = {
    """
    object MyFunctions {
      @WitImport(WitScope.unversioned("test", "test", "module"), "default-param")
      def defaultParam(@WitName("x") x: Int = 42): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: scala.scalajs.wit.annotation.WitImport(scala.scalajs.wit.annotation.WitScope.unversioned("test", "test", "module"), "default-param") methods may not have default parameters
      |      def defaultParam(@WitName("x") x: Int = 42): Int = wm.native
      |          ^
    """
  }

  @Test def witImportParametersMustBeCompatible: Unit = {
    """
    class NotCompatible

    object MyFunctions {
      @WitImport(WitScope.unversioned("test", "test", "module"), "invalid-param")
      def invalidParam(@WitName("x") x: NotCompatible): Unit = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:10: error: Parameter 'x' has type 'NotCompatible' which is not compatible with Component Model
      |      def invalidParam(@WitName("x") x: NotCompatible): Unit = wm.native
      |          ^
    """
  }

  @Test def witImportReturnTypeMustBeCompatible: Unit = {
    """
    class NotCompatible

    object MyFunctions {
      @WitImport(WitScope.unversioned("test", "test", "module"), "invalid-return")
      def invalidReturn(@WitName("x") x: Int): NotCompatible = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:10: error: Return type 'NotCompatible' is not compatible with Component Model
      |      def invalidReturn(@WitName("x") x: Int): NotCompatible = wm.native
      |          ^
    """
  }

  @Test def witImportCannotOverride: Unit = {
    """
    trait Base {
      def baseMethod(): Int
    }

    object MyFunctions extends Base {
      @WitImport(WitScope.unversioned("test", "test", "module"), "override-func")
      def baseMethod(): Int = wm.native
    }
    """ hasErrors
    """
      |newSource1.scala:12: error: An scala.scalajs.wit.annotation.WitImport(scala.scalajs.wit.annotation.WitScope.unversioned("test", "test", "module"), "override-func") member cannot implement the inherited member Base.baseMethod
      |      def baseMethod(): Int = wm.native
      |          ^
    """
  }

  @Test def witImportCannotBeOnLocalDefinition: Unit = {
    """
    object MyFunctions {
      def outer(): Unit = {
        @WitImport(WitScope.unversioned("test", "test", "module"), "local-func")
        def localFunc(x: Int): Int = wm.native
      }
    }
    """ hasErrors
    """
      |newSource1.scala:9: error: scala.scalajs.wit.annotation.WitImport(scala.scalajs.wit.annotation.WitScope.unversioned("test", "test", "module"), "local-func") is not allowed on local definitions
      |        def localFunc(x: Int): Int = wm.native
      |            ^
    """
  }

  @Test def witImportValidExamples: Unit = {
    """
    object MyImports {
      @WitImport(WitScope.unversioned("test", "test", "module"), "add")
      def add(@WitName("a") a: Int, @WitName("b") b: Int): Int = wm.native

      @WitImport(WitScope.unversioned("test", "test", "module"), "greet")
      def greet(@WitName("name") name: String): String = wm.native

      @WitImport(WitScope.unversioned("test", "test", "module"), "process")
      def process(@WitName("data") data: Array[UByte]): wm.Result[Unit, String] = wm.native

      @WitImport(WitScope.unversioned("test", "test", "module"), "no-params")
      def noParams(): Unit = wm.native

      @WitImport(WitScope.unversioned("test", "test", "module"), "returns-optional")
      def returnsOptional(@WitName("x") x: Int): java.util.Optional[String] = wm.native
    }

    @WitRecord(WitScope.root, "record")
    final case class Point(@WitName("x") x: Int, @WitName("y") y: Int)

    object MoreImports {
      @WitImport(WitScope.unversioned("test", "test", "geom"), "distance")
      def distance(@WitName("p1") p1: Point, @WitName("p2") p2: Point): Double = wm.native
    }
    """.hasNoWarns()
  }

  // --- Component Export Tests ---

  @Test def witExportMustBeInStaticObject: Unit = {
    """
    class NotStatic {
      @WitExport(WitScope.unversioned("test", "test", "module"), "method")
      def method(): Unit = ???
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: @WitExport can only be used in static objects.
      |      @WitExport(WitScope.unversioned("test", "test", "module"), "method")
      |       ^
    """

    """
    class Owner {
      object Nested {
        @WitExport(WitScope.unversioned("test", "test", "module"), "method")
        def method(): Unit = ()
      }
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: @WitExport can only be used in static objects.
      |        @WitExport(WitScope.unversioned("test", "test", "module"), "method")
      |         ^
    """
  }

  @Test def witExportTraitMethodsAreTemporarilyIgnored: Unit = {
    """
    trait GeneratedExports {
      @WitExport(WitScope.unversioned("test", "test", "module"), "method")
      def method(): Unit
    }
    """.hasNoWarns()
  }

  @Test def validWitExportExamples: Unit = {
    """
    object DirectRunImpl {
      val foo = "bar"

      @WitExport(WitScope("wasi", "cli", "run", "0.2.0"), "run")
      def run(): wm.Result[Unit, Unit] = {
        println(foo)
        new wm.Ok(())
      }

      def helper(): String = foo
    }

    object MyAPIImpl {
      @WitExport(WitScope.unversioned("test", "test", "api"), "get-data")
      def getData(@WitName("id") id: Int): wm.Result[String, String] =
        new wm.Ok(s"data-$id")

      @WitExport(WitScope.unversioned("test", "test", "api"), "process")
      def process(@WitName("data") data: Array[UByte]): Unit = {
        // Process data
      }
    }

    object Outer {
      object Nested {
        @WitExport(WitScope.unversioned("test", "test", "nested"), "method")
        def method(): Unit = ()
      }
    }
    """.hasNoWarns()
  }
}
