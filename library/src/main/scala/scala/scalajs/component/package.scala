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

package scala.scalajs

import scala.annotation.meta._

/** Types, methods and values for interoperability with Wasm Component Model libraries.
 */
package object component {
  /** Marks the annotated class, trait or object as a Wasm Component instance.
   *
   *  Wasm Component instances are not implemented in Scala.js. They are facade types
   *  for Wasm Component libraries.
   *
   *  Only types extending [[Any js.Any]] can be annotated with `@js.native`.
   *  The body of all concrete members in a native JS class, trait or object
   *  must be `= js.native`.
   */
  @field @getter @setter
  class native extends scala.annotation.StaticAnnotation

  class variant extends scala.annotation.StaticAnnotation

  /** Denotes a method body as imported from Wasm Component. For use in facade types:
   *
   *  {{{
   *  class MyJSClass extends js.Object {
   *    def myMethod(x: String): Int = component.native
   *  }
   *  }}}
   */
  def native: Nothing = {
    throw new java.lang.Error(
        "A method defined in a native JavaScript type of a Scala.js library " +
        "has been called. This is most likely because you tried to run " +
        "Scala.js binaries on the JVM. Make sure you are using the JVM " +
        "version of the libraries.")
  }


}
