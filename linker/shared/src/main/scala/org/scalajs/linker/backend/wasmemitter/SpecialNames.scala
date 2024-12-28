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

package org.scalajs.linker.backend.wasmemitter

import org.scalajs.ir.Names._
import org.scalajs.ir.Types._

object SpecialNames {
  // Class names

  /* Our back-end-specific box classes for the generic representation of
   * `char` and `long`. These classes are not part of the classpath. They are
   * generated automatically by `DerivedClasses`.
   */
  val CharBoxClass = BoxedCharacterClass.withSuffix("Box")
  val LongBoxClass = BoxedLongClass.withSuffix("Box")
  val BooleanBoxClass = BoxedBooleanClass.withSuffix("Box")
  val IntegerBoxClass = BoxedIntegerClass.withSuffix("Box")
  val FloatBoxClass = BoxedFloatClass.withSuffix("Box")
  val DoubleBoxClass = BoxedDoubleClass.withSuffix("Box")
  val UnitBoxClass = BoxedUnitClass.withSuffix("Box")

  val CharBoxCtor = MethodName.constructor(List(CharRef))
  val LongBoxCtor = MethodName.constructor(List(LongRef))
  val BooleanBoxCtor = MethodName.constructor(List(BooleanRef))

  val JLNumberClass = ClassName("java.lang.Number")

  // js.JavaScriptException, for WrapAsThrowable and UnwrapFromThrowable
  val JSExceptionClass = ClassName("scala.scalajs.js.JavaScriptException")

  val UndefinedBehaviorErrorClass =
    ClassName("org.scalajs.linker.runtime.UndefinedBehaviorError")

  // Wasm Component Model
  val WasmComponentResultClass =
    ClassName("scala.scalajs.component.Result")
  val WasmComponentOkClass =
    ClassName("scala.scalajs.component.Ok")
  val WasmComponentErrClass =
    ClassName("scala.scalajs.component.Err")


  // Field names

  val valueFieldSimpleName = SimpleFieldName("value")

  val exceptionFieldName = FieldName(JSExceptionClass, SimpleFieldName("exception"))

  val wasmComponentOkValueFieldName = FieldName(WasmComponentOkClass, SimpleFieldName("value"))
  val wasmComponentErrValueFieldName = FieldName(WasmComponentErrClass, SimpleFieldName("value"))

  // Method names

  val AnyArgConstructorName = MethodName.constructor(List(ClassRef(ObjectClass)))
  val StringArgConstructorName = MethodName.constructor(List(ClassRef(BoxedStringClass)))
  val IntArgConstructorName = MethodName.constructor(List(IntRef))
  val ThrowableArgConsructorName = MethodName.constructor(List(ClassRef(ThrowableClass)))

  val hashCodeMethodName = MethodName("hashCode", Nil, IntRef)

  /** A unique simple method name to map all method *signatures* into `MethodName`s. */
  val normalizedSimpleMethodName = SimpleMethodName("m")
}
