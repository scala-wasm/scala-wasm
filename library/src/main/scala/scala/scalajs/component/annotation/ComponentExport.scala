package scala.scalajs.component.annotation

import scala.annotation.meta._

@field @getter @setter
class ComponentExport private () extends scala.annotation.StaticAnnotation {
  def this(name: String, moduleName: String) = this()
}