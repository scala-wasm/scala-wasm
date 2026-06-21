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

import java.util.function.Supplier

import scala.scalajs.js
import scala.scalajs.LinkingInfo
import scala.scalajs.LinkingInfo.ESVersion

private[regex] object JSEngine extends Engine {
  /* Dictionary[V] is left abstract; it is js.Map[String, V] when linking for
   * ES 2015+ and js.Dictionary[V] otherwise.
   */

  type RegExp = js.RegExp
  type ExecResult = js.RegExp.ExecResult
  type IndicesArray = js.Array[js.UndefOr[js.Tuple2[Int, Int]]]

  @inline
  def dictEmpty[V](): Dictionary[V] = {
    if (LinkingInfo.esVersion >= ESVersion.ES2015)
      new js.Map[String, V]().asInstanceOf[Dictionary[V]]
    else
      Utils.dictEmpty[V]().asInstanceOf[Dictionary[V]]
  }

  @inline
  def dictSet[V](dict: Dictionary[V], key: String, value: V): Unit = {
    if (LinkingInfo.esVersion >= ESVersion.ES2015)
      Utils.mapSet(dict.asInstanceOf[js.Map[String, V]], key, value)
    else
      Utils.dictSet(dict.asInstanceOf[js.Dictionary[V]], key, value)
  }

  @inline
  def dictContains[V](dict: Dictionary[V], key: String): Boolean = {
    if (LinkingInfo.esVersion >= ESVersion.ES2015)
      Utils.mapHas(dict.asInstanceOf[js.Map[String, V]], key)
    else
      Utils.dictContains(dict.asInstanceOf[js.Dictionary[V]], key)
  }

  @inline
  def dictRawApply[V](dict: Dictionary[V], key: String): V = {
    if (LinkingInfo.esVersion >= ESVersion.ES2015)
      Utils.mapGet(dict.asInstanceOf[js.Map[String, V]], key)
    else
      Utils.dictRawApply(dict.asInstanceOf[js.Dictionary[V]], key)
  }

  @inline
  def dictGetOrElse[V](dict: Dictionary[V], key: String)(default: Supplier[V]): V = {
    if (LinkingInfo.esVersion >= ESVersion.ES2015)
      Utils.mapGetOrElse(dict.asInstanceOf[js.Map[String, V]], key)(default)
    else
      Utils.dictGetOrElse(dict.asInstanceOf[js.Dictionary[V]], key)(default)
  }

  @inline
  def dictGetOrElseUpdate[V](dict: Dictionary[V], key: String)(default: Supplier[V]): V = {
    if (LinkingInfo.esVersion >= ESVersion.ES2015)
      Utils.mapGetOrElseUpdate(dict.asInstanceOf[js.Map[String, V]], key)(default)
    else
      Utils.dictGetOrElseUpdate(dict.asInstanceOf[js.Dictionary[V]], key)(default)
  }

  @inline
  def featureTest(flags: String): Boolean = {
    try {
      new js.RegExp("", flags)
      true
    } catch {
      case _: Throwable =>
        false
    }
  }

  @inline
  def compile(pattern: String, flags: String): RegExp =
    new js.RegExp(pattern, flags)

  @inline
  def validateScriptName(scriptName: String): Boolean = {
    try {
      compile(s"\\p{sc=$scriptName}", "u")
      true
    } catch {
      case _: Throwable => false
    }
  }

  @inline
  def getLastIndex(regexp: RegExp): Int =
    regexp.lastIndex

  @inline
  def setLastIndex(regexp: RegExp, newLastIndex: Int): Unit =
    regexp.lastIndex = newLastIndex

  @inline
  def exec(regexp: RegExp, input: String): ExecResult =
    regexp.exec(input)

  @inline
  def getIndex(result: ExecResult): Int =
    result.index

  @inline
  def getInput(result: ExecResult): String =
    result.input

  @inline
  def getIndices(result: ExecResult): IndicesArray = {
    Utils.undefOrGetOrNull(
        result.asInstanceOf[js.Dynamic].indices.asInstanceOf[js.UndefOr[IndicesArray]])
  }

  @inline
  def setIndices(result: ExecResult, indices: IndicesArray): Unit =
    result.asInstanceOf[js.Dynamic].indices = indices

  @inline
  def getGroup(result: ExecResult, group: Int): String =
    Utils.undefOrGetOrNull(result(group))

  @inline
  def getStart(indices: IndicesArray, group: Int): Int =
    Utils.undefOrFold(indices(group))(() => -1)(_._1)

  @inline
  def getEnd(indices: IndicesArray, group: Int): Int =
    Utils.undefOrFold(indices(group))(() => -1)(_._2)
}
