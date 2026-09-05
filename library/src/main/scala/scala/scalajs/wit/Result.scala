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

sealed trait Result[+A, +B] {
  def isOk: Boolean
  def isErr: Boolean = !isOk

  def get: A

  def fold[C](ifOk: A => C, ifErr: B => C): C

  def map[C](f: A => C): Result[C, B] =
    fold(a => Ok(f(a)), _ => this.asInstanceOf[Result[C, B]])

  def mapErr[C](f: B => C): Result[A, C] =
    fold(_ => this.asInstanceOf[Result[A, C]], b => Err(f(b)))

  def flatMap[C, E >: B](f: A => Result[C, E]): Result[C, E] =
    fold(f, _ => this.asInstanceOf[Result[C, E]])

  def getOrElse[C >: A](default: => C): C =
    fold(a => a, _ => default)

  def orElse[C >: A, E >: B](alternative: => Result[C, E]): Result[C, E] =
    fold(_ => this, _ => alternative)

  def foreach[U](f: A => U): Unit =
    fold(f, _ => ())

  def foreachErr[U](f: B => U): Unit =
    fold(_ => (), f)

  def swap: Result[B, A] =
    fold(a => Err(a), b => Ok(b))

  def toEither: Either[B, A] =
    fold(a => Right(a), b => Left(b))
}

final class Ok[A](val value: A) extends Result[A, Nothing] {
  def isOk: Boolean = true
  def get: A = value
  def fold[C](ifOk: A => C, ifErr: Nothing => C): C = ifOk(value)

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
  def unapply[A](ok: Ok[A]): scala.Some[A] = scala.Some(ok.value)
}

final class Err[B](val value: B) extends Result[Nothing, B] {
  def isOk: Boolean = false
  def get: Nothing = throw new NoSuchElementException("Err.get")
  def fold[C](ifOk: Nothing => C, ifErr: B => C): C = ifErr(value)

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
  def unapply[B](err: Err[B]): scala.Some[B] = scala.Some(err.value)
}
