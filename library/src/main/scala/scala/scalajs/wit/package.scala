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

/** Types, methods and values for interoperability with Wasm Component Model libraries. */
package object wit {

  /** Marks a WIT resource handle as borrowed in a Component Model signature.
   *
   *  `Borrow[R]` is to tell Scala.js compiler to emit `borrow<r>` where
   *  `R` represents own resource type.
   *  For example, `Borrow[InputStream]` corresponds to:
   *
   *  {{{
   *  read: func(s: borrow<input-stream>);
   *  }}}
   */
  type Borrow[T] = T

  /** Denotes a method body as imported from Wasm Component. For use in facade types:
   *
   *  {{{
   *  class MyJSClass extends js.Object {
   *    def myMethod(x: String): Int = wit.native
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
