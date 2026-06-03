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

package scala.scalajs

import scala.language.implicitConversions

import java.util.Optional

import scala.scalajs.{wit => wm}

object WitConversions {
  implicit final class ResultOps[A, B](private val result: wm.Result[A, B]) extends AnyVal {
    def toEither: Either[B, A] = result match {
      case err: wm.Err[B] => Left(err.value)
      case ok: wm.Ok[A]   => Right(ok.value)
    }
  }

  @inline implicit def fromScalaEither[A, B](either: Either[B, A]): wm.Result[A, B] = {
    either match {
      case Left(value)  => wm.Err(value)
      case Right(value) => wm.Ok(value)
    }
  }

  @inline implicit def toScalaEither[A, B](result: wm.Result[A, B]): Either[B, A] =
    result.toEither

  implicit final class OptionalOps[A](private val optional: Optional[A]) extends AnyVal {
    def toOption: Option[A] = {
      if (optional.isPresent()) Some(optional.get())
      else None
    }
  }

  @inline implicit def fromScalaOption[A](option: Option[A]): Optional[A] = {
    option match {
      case Some(value) => Optional.of(value)
      case None        => Optional.empty[A]()
    }
  }

  @inline implicit def toScalaOption[A](optional: Optional[A]): Option[A] =
    optional.toOption
}
