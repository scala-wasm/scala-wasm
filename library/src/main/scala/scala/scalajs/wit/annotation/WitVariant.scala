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

/** Marks a sealed trait as a WebAssembly Component Model variant type.
 *
 *  The sealed trait must have only case classes or case objects as direct
 *  children. Each case class must have at most one field `value`, and that
 *  field must be a type compatible with the Component Model.
 *  Case objects represent variant cases with no payload (unit variants).
 *
 *  The order of case declaration determines the discriminant indices assigned
 *  to each variant case.
 *
 *  Example:
 *  {{{
 *  @WitVariant
 *  sealed trait Result
 *  object Result {
 *    final case class Ok(value: Int) extends Result
 *    final case class Err(message: String) extends Result
 *  }
 *  }}}
 *
 *  Example with case object (enum-like):
 *  {{{
 *  @WitVariant
 *  sealed trait Status
 *  object Status {
 *    case object Pending extends Status
 *    case object Running extends Status
 *    case object Done extends Status
 *  }
 *  }}}
 */
@field @getter @setter
class WitVariant extends scala.annotation.StaticAnnotation
