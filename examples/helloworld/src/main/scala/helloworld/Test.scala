package helloworld

import scala.scalajs.component
import component.annotation._

sealed trait StreamError[T] extends component.Variant[T]

final case class LastOperationFailed(value: Error) extends StreamError[Error] {
  protected val _index = 0
}
final case class Closed(value: Unit) extends StreamError[Unit] {
  protected val _index = 1
}

trait Error extends component.Resource

object Test {
  @ComponentImport("tanishiking:test/test@0.0.1", "add")
  def add(a: Int, b: Int): Int = component.native
}