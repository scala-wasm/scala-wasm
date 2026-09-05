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

import java.util.NoSuchElementException

sealed trait Option[+A] {
  def isDefined: Boolean
  def isEmpty: Boolean = !isDefined

  def get: A

  def fold[B](ifEmpty: => B, ifDefined: A => B): B

  def map[B](f: A => B): Option[B] =
    fold(None, a => Some(f(a)))

  def flatMap[B](f: A => Option[B]): Option[B] =
    fold(None, f)

  def getOrElse[B >: A](default: => B): B =
    fold(default, a => a)

  def orElse[B >: A](alternative: => Option[B]): Option[B] =
    fold(alternative, _ => this)

  def foreach[U](f: A => U): Unit =
    fold((), f)

  def toScalaOption: scala.Option[A] =
    fold(scala.None, a => scala.Some(a))
}

final class Some[A](val value: A) extends Option[A] {
  def isDefined: Boolean = true
  def get: A = value
  def fold[B](ifEmpty: => B, ifDefined: A => B): B = ifDefined(value)

  override def equals(other: Any): Boolean = {
    other match {
      case that: Some[_] =>
        if (this.value == null) that.value == null
        else this.value == that.value
      case _ => false
    }
  }

  override def hashCode(): Int =
    if (value == null) 0 else value.hashCode()

  override def toString(): String = "Some(" + value + ")"
}

object Some {
  def apply[A](value: A): Some[A] = new Some(value)
  def unapply[A](some: Some[A]): scala.Some[A] = scala.Some(some.value)
}

object None extends Option[Nothing] {
  def isDefined: Boolean = false
  def get: Nothing = throw new NoSuchElementException("None.get")
  def fold[B](ifEmpty: => B, ifDefined: Nothing => B): B = ifEmpty

  override def equals(other: Any): Boolean = other match {
    case _: None.type => true
    case _            => false
  }

  override def hashCode(): Int = 0

  override def toString(): String = "None"
}
