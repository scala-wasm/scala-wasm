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

  def getOrElse[C >: A](default: => C): C =
    if (isOk) get else default

  def map[C](f: A => C): Result[C, B] = this match {
    case Ok(value)  => Ok(f(value))
    case Err(value) => Err(value)
  }

  def mapErr[C](f: B => C): Result[A, C] = this match {
    case Ok(value)  => Ok(value)
    case Err(value) => Err(f(value))
  }

  def flatMap[C, E >: B](f: A => Result[C, E]): Result[C, E] = this match {
    case Ok(value)  => f(value)
    case Err(value) => Err(value)
  }

  def fold[C](ok: A => C, err: B => C): C = this match {
    case Ok(value)  => ok(value)
    case Err(value) => err(value)
  }

  def toEither: Either[B, A] = this match {
    case Ok(value)  => Right(value)
    case Err(value) => Left(value)
  }
}

final case class Ok[+A](value: A) extends Result[A, Nothing] {
  def isOk: Boolean = true
  def get: A = value
}

final case class Err[+B](value: B) extends Result[Nothing, B] {
  def isOk: Boolean = false

  def get: Nothing =
    throw new java.util.NoSuchElementException("Err.get")
}

object Result {
  def ok[A](value: A): Result[A, Nothing] = Ok(value)
  def err[B](value: B): Result[Nothing, B] = Err(value)
}
