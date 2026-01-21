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

import scala.scalajs.LinkingInfo._

/** A wrapper class to select regex implementation across different platforms.
 *
 *  This class provides a common interface for regular expression operations,
 *  allowing the underlying implementation to be switched at link time.
 *  For WebAssembly, it uses a Java-based implementation, while for JavaScript
 *  environments, it delegates to the native `js.RegExp`.
 */
private[java] sealed abstract class RegExpImpl {
  type PatRepr
  type Repr

  def compile(patternStr: String): PatRepr
  def exec(pattern: PatRepr, string: String): Repr
  def matches(r: Repr): Boolean
  def exists(r: Repr, index: Int): Boolean
  def get(r: Repr, index: Int): String
  def getOrElse(r: Repr, index: Int, default: String): String
}

private[java] object RegExpImpl {
  val impl = linkTimeIf[RegExpImpl](targetPureWasm) {
    JavaRegExpImpl
  } {
    JSRegExpImpl
  }

  object JSRegExpImpl extends RegExpImpl {
    import java.lang.Utils._
    import scala.scalajs.js

    type PatRepr = js.RegExp
    type Repr = js.RegExp.ExecResult

    def compile(patternStr: String): PatRepr = new js.RegExp(patternStr)
    def exec(pattern: PatRepr, string: String): Repr = pattern.exec(string)
    def matches(r: Repr): Boolean = r != null
    def exists(r: Repr, index: Int): Boolean = undefOrIsDefined(r(index))
    def get(r: Repr, index: Int): String = undefOrForceGet(r(index))

    def getOrElse(r: Repr, index: Int, default: String): String =
      undefOrGetOrElse(r(index))(() => default)
  }

  private object JavaRegExpImpl extends RegExpImpl {
    type PatRepr = Pattern
    type Repr = Matcher

    def compile(patternStr: String): PatRepr = Pattern.compile(patternStr)
    def exec(pattern: PatRepr, string: String): Repr = pattern.matcher(string)
    def matches(r: Repr): Boolean = r.matches()
    def exists(r: Repr, index: Int): Boolean = r.group(index) != null
    def get(r: Repr, index: Int): String = r.group(index)

    def getOrElse(r: Repr, index: Int, default: String): String = {
      val result = r.group(index)
      if (result != null) result else default
    }
  }
}
