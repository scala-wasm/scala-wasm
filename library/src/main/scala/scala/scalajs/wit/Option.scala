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

/** WIT `option<t>`, distinct from `scala.Option`. */
sealed trait Option[+A] {
  def isEmpty: Boolean
  def isDefined: Boolean = !isEmpty

  def get: A

  def getOrElse[B >: A](default: => B): B =
    if (isEmpty) default else get

  def map[B](f: A => B): Option[B] =
    if (isEmpty) None else Some(f(get))

  def flatMap[B](f: A => Option[B]): Option[B] =
    if (isEmpty) None else f(get)

  def fold[B](ifEmpty: => B)(f: A => B): B =
    if (isEmpty) ifEmpty else f(get)

  def filter(p: A => Boolean): Option[A] =
    if (isEmpty || p(get)) this else None

  def foreach[U](f: A => U): Unit =
    if (!isEmpty) f(get)

  def orElse[B >: A](alternative: => Option[B]): Option[B] =
    if (isEmpty) alternative else this

  def contains[B >: A](elem: B): Boolean =
    !isEmpty && get == elem

  def toScala: scala.Option[A] =
    if (isEmpty) scala.None else scala.Some(get)
}

final case class Some[+A](value: A) extends Option[A] {
  def isEmpty: Boolean = false
  def get: A = value
}

case object None extends Option[Nothing] {
  def isEmpty: Boolean = true

  def get: Nothing =
    throw new java.util.NoSuchElementException("None.get")
}

object Option {
  def apply[A](value: A): Option[A] = Some(value)

  def empty[A]: Option[A] = None

  def fromScala[A](opt: scala.Option[A]): Option[A] = opt match {
    case scala.Some(value) => Some(value)
    case scala.None        => None
  }
}
