package scala.scalajs.component

sealed trait Result[+A, +B] extends Variant

final class Ok[A](val value: A) extends Result[A, Nothing] {
  type T = A
  val _index: Int = 0
}
final class Err[B](val value: B) extends Result[Nothing, B] {
  type T = B
  val _index: Int = 1
}