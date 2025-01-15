package scala.scalajs.component

sealed trait Result[+A, +B] extends Variant

final case class Ok[X](val value: X) extends Result[X, Nothing] {
  type T = X
  val _index: Int = 0
}
final case class Err[E](val value: E) extends Result[Nothing, E] {
  type T = E
  val _index: Int = 1
}