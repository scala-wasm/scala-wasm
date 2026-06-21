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

package scala.scalajs.wit.annotation

import scala.annotation.meta._

/** Represents bitflags in the Component Model
 *
 *  Must be used on a final case class with a single Int parameter named "value".
 *
 *  Example:
 *  {{{
 *  @WitFlags(3)
 *  final case class MyFlags(value: Int) { ... }
 *  object MyFlags {
 *    val Flag0 = MyFlags(1 << 0)
 *    val Flag1 = MyFlags(1 << 1)
 *    val Flag2 = MyFlags(1 << 2)
 *  }
 *  }}}
 *
 *  @param numFlags The number of flags in this flags type
 */
@field @getter @setter
class WitFlags(val numFlags: Int) extends scala.annotation.StaticAnnotation
