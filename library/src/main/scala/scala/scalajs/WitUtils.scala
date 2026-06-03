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
import scala.scalajs.WitConversions._

@deprecated("Use scala.scalajs.WitConversions instead.", "1.21.1-wasm.5")
object WitUtils {
  def toEither[A, B](res: wm.Result[A, B]): Either[B, A] =
    res.toEither

  def toOption[A, B](opt: java.util.Optional[A]): Option[A] =
    opt.toOption
}
