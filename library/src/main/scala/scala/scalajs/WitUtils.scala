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

import scala.scalajs.{wit => wm}

object WitUtils {
  def toEither[A, B](res: wm.Result[A, B]): Either[B, A] = res match {
    case err: wm.Err[B] => Left(err.value)
    case ok: wm.Ok[A]   => Right(ok.value)
  }

  def toOption[A, B](opt: java.util.Optional[A]): Option[A] = {
    if (opt.isEmpty()) None
    else Some(opt.get())
  }
}
