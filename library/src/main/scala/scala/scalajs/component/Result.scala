package scala.scalajs.component

sealed abstract class Result[+T, +E]
final case class Ok[T](value: T) extends Result[T, Nothing]
final case class Err[E](value: E) extends Result[Nothing, E]