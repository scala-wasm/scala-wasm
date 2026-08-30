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

package org.scalajs.linker.backend.wasmemitter

import scala.collection.mutable

import org.scalajs.ir.WitScope
import org.scalajs.ir.WasmInterfaceTypes._
import org.scalajs.linker.standard.LinkedClass

/** Resolve `AliasTypeRef`s using `WitAliasDef`s from linked classes. */
private[wasmemitter] object WitAliasResolution {

  def collectAliases(
      classes: List[LinkedClass]): Map[(WitScope, String), ValType] = {
    val m = mutable.Map.empty[(WitScope, String), ValType]
    for {
      clazz <- classes
      alias <- clazz.witAliases
    } {
      m.getOrElseUpdate((alias.scope, alias.name), alias.target)
    }
    m.toMap
  }

  def dealiasVal(tpe: ValType,
      aliases: Map[(WitScope, String), ValType]): ValType = {
    dealias(tpe,
        (scope, name) =>
          aliases.getOrElse((scope, name),
              throw new Error(s"WIT alias not found: $scope/$name")))
  }
}
