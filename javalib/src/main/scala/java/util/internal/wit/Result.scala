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

package java.util.internal.wit

import java.util.NoSuchElementException

sealed trait Result[+A, +B] {
  def isOk: Boolean
  def isErr: Boolean = !isOk

  def get: A

  def orElse[C >: A](other: C): C =
    if (isOk) this.get else other
}

final class Ok[A](val value: A) extends Result[A, Nothing] {
  def isOk: Boolean = true
  def get: A = value

  override def toString(): String = "Ok(" + value + ")"
}

object Ok {
  def apply[A](value: A): Ok[A] = new Ok(value)
}

final class Err[B](val value: B) extends Result[Nothing, B] {
  def isOk: Boolean = false
  def get: Nothing = throw new NoSuchElementException("Err.get")

  override def toString(): String = "Err(" + value + ")"
}

object Err {
  def apply[B](value: B): Err[B] = new Err(value)
}
