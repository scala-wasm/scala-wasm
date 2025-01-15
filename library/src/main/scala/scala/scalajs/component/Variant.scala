package scala.scalajs.component

trait Variant {
  type T
  val value: T
  protected val _index: Int
}