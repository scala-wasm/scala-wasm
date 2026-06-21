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

package java.util.regex

import scala.language.higherKinds

import java.util.function.Supplier

import scala.scalajs.LinkingInfo
import scala.scalajs.LinkingInfo.moduleKind
import scala.scalajs.LinkingInfo.ModuleKind.{MinimalWasmModule, WasmComponent}

/** Underlying engine used by `Pattern`.
 *
 *  There are two possible engines. On a JS host, we use `js.RegExp`. On Wasm
 *  without JS host, we use `WasmEngine`.
 *
 *  The semantics are that of `js.RegExp`. However, an `Engine` does not need
 *  to support all the possible patterns. It only needs to support the patterns
 *  produced by `PatternCompiler`. `IndicesBuilder` contains a sort of spec for
 *  the allowed patterns.
 */
private[regex] abstract class Engine {

  /** A choice of implementation of dictionaries (mutable maps from string to `V`).
   *
   *  This not really tight to the regex engine per se, but we put it here
   *  anyway so that we do not create more abstractions.
   */
  type Dictionary[V]

  type RegExp
  type ExecResult >: Null
  type IndicesArray >: Null

  // Methods manipulating a `Dictionary[V]`
  def dictEmpty[V](): Dictionary[V]
  def dictSet[V](dict: Dictionary[V], key: String, value: V): Unit
  def dictContains[V](dict: Dictionary[V], key: String): Boolean
  def dictRawApply[V](dict: Dictionary[V], key: String): V
  def dictGetOrElse[V](dict: Dictionary[V], key: String)(default: Supplier[V]): V
  def dictGetOrElseUpdate[V](dict: Dictionary[V], key: String)(default: Supplier[V]): V

  /** Tests whether the engine supports the given flag string */
  def featureTest(flags: String): Boolean

  /** Compiles a pattern and flags into a `RegExp`, following `js.RegExp` semantics. */
  def compile(pattern: String, flags: String): RegExp

  /** Tests whether `scriptName` is a valid value for the `sc=` Unicode property name. */
  def validateScriptName(scriptName: String): Boolean

  /** Gets `regexp.lastIndex`, as if by the `js.RegExp.lastIndex` specification. */
  def getLastIndex(regexp: RegExp): Int

  /** Sets `regexp.lastIndex`, as if by the `js.RegExp.lastIndex` specification. */
  def setLastIndex(regexp: RegExp, newLastIndex: Int): Unit

  /** Executes a compiled `RegExp`, as if by the `js.RegExp.exec()` specification. */
  def exec(regexp: RegExp, input: String): ExecResult

  /** Gets `result.index`, as if by the `js.RegExp.ExecResult.index` specification. */
  def getIndex(result: ExecResult): Int

  /** Gets `result.input`, as if by the `js.RegExp.ExecResult.input` specification. */
  def getInput(result: ExecResult): String

  /** Gets `result.indices`, as if by the `js.RegExp.ExecResult.indices` specification.
   *
   *  The implementation should return `null` for an `undefined` result. The
   *  implementation *may* return a (valid) non-null result even if the 'd'
   *  flag was not used when compiling the regexp.
   */
  def getIndices(result: ExecResult): IndicesArray

  /** Sets `result.indices`, as if by the `js.RegExp.ExecResult.indices` specification.
   *
   *  The implementation may ignore that request if `getIndices(result)`
   *  returns a non-null value.
   */
  def setIndices(result: ExecResult, indices: IndicesArray): Unit

  /** Gets the substring of the input matched by the given group.
   *
   *  Index 0 corresponds to the whole match.
   */
  def getGroup(result: ExecResult, group: Int): String

  /** Gets the start index of the range matched by the given group.
   *
   *  Index 0 corresponds to the whole match.
   */
  def getStart(indices: IndicesArray, group: Int): Int

  /** Gets the end index of the range matched by the given group.
   *
   *  Index 0 corresponds to the whole match.
   */
  def getEnd(indices: IndicesArray, group: Int): Int
}

private[regex] object Engine {
  val engine: Engine = {
    LinkingInfo.linkTimeIf[Engine](moduleKind == MinimalWasmModule || moduleKind == WasmComponent) {
      WasmEngine
    } {
      JSEngine
    }
  }
}
