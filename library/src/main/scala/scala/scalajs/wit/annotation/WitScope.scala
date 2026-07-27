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

package scala.scalajs.wit.annotation

final class WitScope private ()

object WitScope {
  def apply(namespace: String, packageName: String, name: String,
      version: String): WitScope = {
    new WitScope
  }

  def unversioned(namespace: String, packageName: String,
      name: String): WitScope = {
    new WitScope
  }

  val root: WitScope = new WitScope

  def inline(name: String): WitScope = new WitScope
}
