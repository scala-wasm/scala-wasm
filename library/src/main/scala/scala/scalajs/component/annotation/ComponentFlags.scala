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

package scala.scalajs.component.annotation

import scala.annotation.meta._

/**
  * Represents bitflags in the Component Model
  */
@field @getter @setter
class ComponentFlags private () extends scala.annotation.StaticAnnotation {
  def this(numFlags: Int) = this()
}
