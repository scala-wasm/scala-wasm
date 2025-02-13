package scala.scalajs.component

sealed trait Option[+A] extends Variant

final case object None extends Option[Nothing] {
  type T = Unit
  val value = ()
  val _index: Int = 0
}


final case class Some[A](val value: A) extends Option[A] {
  type T = A
  val _index: Int = 1
}