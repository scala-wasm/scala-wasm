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

sealed trait Option[+A] {
  def isDefined: Boolean
  def isEmpty: Boolean = !isDefined

  def get: A

  def orElse[C >: A](other: C): C =
    if (isDefined) this.get else other
}

final class Some[A](val value: A) extends Option[A] {
  def isDefined: Boolean = true
  def get: A = value

  override def toString(): String = "Some(" + value + ")"
}

object Some {
  def apply[A](value: A): Some[A] = new Some(value)
}

object None extends Option[Nothing] {
  def isDefined: Boolean = false
  def get: Nothing = throw new NoSuchElementException("None.get")

  override def toString(): String = "None"
}
