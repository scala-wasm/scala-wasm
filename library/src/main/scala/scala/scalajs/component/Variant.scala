package scala.scalajs.component

trait Variant[T] extends scala.AnyRef {
  protected val _index: Int
  val value: T
}