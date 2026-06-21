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

package scala.scalajs.wit

sealed trait Result[+A, +B]

final class Ok[A](val value: A) extends Result[A, Nothing] {
  override def equals(other: Any): Boolean = {
    other match {
      case that: Ok[_] =>
        if (this.value == null) that.value == null
        else this.value == that.value
      case _ => false
    }
  }

  override def hashCode(): Int =
    if (value == null) 0 else value.hashCode()

  override def toString(): String = "Ok(" + value + ")"
}

object Ok {
  def apply[A](value: A): Ok[A] = new Ok(value)
}

final class Err[B](val value: B) extends Result[Nothing, B] {
  override def equals(other: Any): Boolean = {
    other match {
      case that: Err[_] =>
        if (this.value == null) that.value == null
        else this.value == that.value
      case _ => false
    }
  }

  override def hashCode(): Int =
    if (value == null) 0 else value.hashCode()

  override def toString(): String = "Err(" + value + ")"
}

object Err {
  def apply[B](value: B): Err[B] = new Err(value)
}
