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

/** Marks a function as a Wasm Component Model export.
 *
 *  This annotation is only valid on methods in static objects.
 *
 *  For a function of an exported resource (see [[WitResourceExport]]), the
 *  name is the Component Model name of the function: `[constructor]r`,
 *  `[method]r.f` or `[static]r.f`. A method takes the instance as its first
 *  parameter, `self: scala.scalajs.wit.Borrow[R]`.
 */
@field @getter @setter
class WitExport private () extends scala.annotation.StaticAnnotation {
  def this(scope: WitScope, name: String) = this()
}
