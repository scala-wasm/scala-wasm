package scala.scalajs.component.annotation

import scala.annotation.meta._

/**
  * Represents bitflags in the Component Model
  */
@field @getter @setter
class ComponentFlags private () extends scala.annotation.StaticAnnotation {
  def this(numFlags: Int) = this()
}