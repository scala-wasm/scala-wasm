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

/** scala.scalajs.LinkingInfo shim for testSuiteJVM
 *
 *  Currently, LinkingInfo.linkTimeIf removes some test functions
 *  that do not link for pure Wasm, in some tests under test-suite/shared.
 *  Once all tests link to pure Wasm and linkTimeIf removed, this shim will no longer be needed.
 */
object LinkingInfo {
  val moduleKind = -1

  def linkTimeIf[T](cond: Boolean)(thenp: T)(elsep: T): T =
    if (cond) thenp else elsep

  object ModuleKind {
    final val MinimalWasmModule = 4
    final val WasmComponent = 5
  }
}
