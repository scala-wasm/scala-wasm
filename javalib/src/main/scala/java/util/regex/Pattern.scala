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

import scala.annotation.tailrec

import java.util.ScalaOps._

import scala.scalajs.LinkingInfo

import PatternCompiler.Support._

final class Pattern private[regex] (
    _pattern: String,
    _flags: Int,
    jsPattern: String,
    jsFlags: String,
    sticky: Boolean,
    private[regex] val groupCount: Int,
    groupNumberMap: Array[Int],
    namedGroups: Engine.engine.Dictionary[Int]
) extends Serializable {

  import Pattern._
  import Engine.engine

  @inline private def jsFlagsForFind: String =
    jsFlags + (if (sticky && supportsSticky) "gy" else "g")

  /** Whether we already added the 'd' flag to the native RegExp's. */
  private var enabledNativeIndices: Boolean = false

  /** The RegExp that is used for `Matcher.find()`.
   *
   *  It receives the 'g' flag so that `lastIndex` is taken into acount.
   *
   *  It also receives the 'y' flag if this pattern is sticky and it is
   *  supported. If it is not supported, its behavior is polyfilled in
   *  `execFind()`.
   *
   *  Since that RegExp is only used locally within `execFind()`, we can
   *  always reuse the same instance.
   */
  private[this] var jsRegExpForFind: engine.RegExp =
    engine.compile(jsPattern, jsFlagsForFind)

  /** Another version of the RegExp that is used by `Matcher.matches()`.
   *
   *  It forces `^` and `$` at the beginning and end of the pattern so that
   *  only entire inputs are matched. In addition, it does not have the 'g'
   *  flag, so that it can be repeatedly used without managing `lastIndex`.
   *
   *  Since that RegExp is only used locally within `execMatches()`, we can
   *  always reuse the same instance.
   */
  private[this] var jsRegExpForMatches: engine.RegExp =
    engine.compile(wrapJSPatternForMatches(jsPattern), jsFlags)

  private lazy val indicesBuilder: IndicesBuilder =
    IndicesBuilder(jsPattern, jsFlags)

  private[regex] def execMatches(input: String): engine.ExecResult =
    engine.exec(jsRegExpForMatches, input)

  @inline // to stack-allocate the tuple
  private[regex] def execFind(input: String, start: Int): (engine.ExecResult, Int) = {
    val mtch = execFindInternal(input, start)
    val end = engine.getLastIndex(jsRegExpForFind)
    (mtch, end)
  }

  private def execFindInternal(input: String, start: Int): engine.ExecResult = {
    val regexp = jsRegExpForFind

    if (!supportsSticky && sticky) {
      engine.setLastIndex(regexp, start)
      val mtch = engine.exec(regexp, input)
      if (mtch == null || engine.getIndex(mtch) > start)
        null
      else
        mtch
    } else if (supportsUnicode) {
      engine.setLastIndex(regexp, start)
      engine.exec(regexp, input)
    } else {
      /* When the native RegExp does not support the 'u' flag (introduced in
       * ECMAScript 2015), it can find a match starting in the middle of a
       * surrogate pair. This can happen if the pattern can match a substring
       * starting with a lone low surrogate. However, that is not valid,
       * because surrogate pairs must always stick together.
       *
       * In all the other situations, the `PatternCompiler` makes sure that
       * surrogate pairs are always matched together or not at all, but it
       * cannot avoid this specific situation because there is no look-behind
       * support in that case either. So we take care of it now by skipping
       * matches that start in the middle of a surrogate pair.
       */
      @tailrec
      def loop(start: Int): engine.ExecResult = {
        engine.setLastIndex(regexp, start)
        val mtch = engine.exec(regexp, input)
        if (mtch == null) {
          null
        } else {
          val index = engine.getIndex(mtch)
          if (index > start && index < input.length() &&
              Character.isLowSurrogate(input.charAt(index)) &&
              Character.isHighSurrogate(input.charAt(index - 1))) {
            loop(index + 1)
          } else {
            mtch
          }
        }
      }
      loop(start)
    }
  }

  private[regex] def numberedGroup(group: Int): Int = {
    if (group < 0 || group > groupCount)
      throw new IndexOutOfBoundsException(group.toString())
    groupNumberMap(group)
  }

  private[regex] def namedGroup(name: String): Int = {
    groupNumberMap(engine.dictGetOrElse(namedGroups, name) { () =>
      throw new IllegalArgumentException(s"No group with name <$name>")
    })
  }

  private[regex] def getIndices(lastMatch: engine.ExecResult,
      forMatches: Boolean): engine.IndicesArray = {
    LinkingInfo.linkTimeIf(LinkingInfo.targetPureWasm) {
      val indices = engine.getIndices(lastMatch)
      if (indices == null)
        throw new AssertionError("Unreachable; WasmEngine always supports and produces indices")
      indices
    } {
      // This 'else' branch only links on a JS host, and assumes that `engine eq JSEngine`
      if (engine.getIndices(lastMatch) == null) {
        if (supportsIndices) {
          if (!enabledNativeIndices) {
            jsRegExpForFind = engine.compile(jsPattern, jsFlagsForFind + "d")
            jsRegExpForMatches = engine.compile(wrapJSPatternForMatches(jsPattern), jsFlags + "d")
            enabledNativeIndices = true
          }
          val regexp = if (forMatches) jsRegExpForMatches else jsRegExpForFind
          engine.setLastIndex(regexp, engine.getIndex(lastMatch))
          engine.setIndices(
              lastMatch, engine.getIndices(engine.exec(regexp, engine.getInput(lastMatch))))
        } else {
          val indices: JSEngine.IndicesArray =
            indicesBuilder(forMatches, engine.getInput(lastMatch), engine.getIndex(lastMatch))
          engine.setIndices(lastMatch, indices.asInstanceOf[engine.IndicesArray])
        }
      }
      engine.getIndices(lastMatch)
    }
  }

  // Public API ---------------------------------------------------------------

  def pattern(): String = _pattern
  def flags(): Int = _flags

  override def toString(): String = pattern()

  @inline // `input` is almost certainly a String at call site
  def matcher(input: CharSequence): Matcher =
    new Matcher(this, input.toString())

  @inline // `input` is almost certainly a String at call site
  def split(input: CharSequence): Array[String] =
    split(input, 0)

  @inline // `input` is almost certainly a String at call site
  def split(input: CharSequence, limit: Int): Array[String] =
    split(input.toString(), limit)

  private def split(inputStr: String, limit: Int): Array[String] = {
    // If the input string is empty, always return Array("") - #987, #2592
    if (inputStr == "") {
      Array("")
    } else {
      // Actually split original string
      val lim = if (limit > 0) limit else Int.MaxValue
      val matcher = this.matcher(inputStr)
      val result = new SplitBuilder(if (limit > 0) limit else 16)
      var prevEnd = 0
      while ((result.length < lim - 1) && matcher.find()) {
        if (matcher.end() == 0) {
          /* If there is a zero-width match at the beginning of the string,
           * ignore it, i.e., omit the resulting empty string at the beginning
           * of the array.
           */
        } else {
          result.push(inputStr.substring(prevEnd, matcher.start()))
        }
        prevEnd = matcher.end()
      }
      result.push(inputStr.substring(prevEnd))

      // With `limit == 0`, remove trailing empty strings.
      var actualLength = result.length
      if (limit == 0) {
        while (actualLength != 0 && result(actualLength - 1) == "")
          actualLength -= 1
      }

      // Build result array
      result.build(actualLength)
    }
  }
}

object Pattern {
  final val UNIX_LINES = 0x01
  final val CASE_INSENSITIVE = 0x02
  final val COMMENTS = 0x04
  final val MULTILINE = 0x08
  final val LITERAL = 0x10
  final val DOTALL = 0x20
  final val UNICODE_CASE = 0x40
  final val CANON_EQ = 0x80
  final val UNICODE_CHARACTER_CLASS = 0x100

  def compile(regex: String, flags: Int): Pattern =
    PatternCompiler.compile(regex, flags)

  def compile(regex: String): Pattern =
    compile(regex, 0)

  @inline // `input` is almost certainly a String at call site
  def matches(regex: String, input: CharSequence): Boolean =
    matches(regex, input.toString())

  private def matches(regex: String, input: String): Boolean =
    compile(regex).matcher(input).matches()

  def quote(s: String): String = {
    var result = "\\Q"
    var start = 0
    var end = s.indexOf("\\E", start)
    while (end >= 0) {
      result += s.substring(start, end) + "\\E\\\\E\\Q"
      start = end + 2
      end = s.indexOf("\\E", start)
    }
    result + s.substring(start) + "\\E"
  }

  @inline
  private[regex] def wrapJSPatternForMatches(jsPattern: String): String =
    "^(?:" + jsPattern + ")$" // the group is needed if there is a top-level | in jsPattern

  @inline
  private class SplitBuilder(initialCapacity: Int) {
    private var array: Array[String] = new Array[String](initialCapacity)
    private var _length = 0

    def length: Int = _length

    def apply(index: Int): String = array(index)

    def push(x: String): Unit = {
      if (_length == array.length)
        array = java.util.Arrays.copyOf(array, _length * 2)
      array(_length) = x
      _length += 1
    }

    def build(actualLength: Int): Array[String] =
      if (actualLength == array.length) array
      else java.util.Arrays.copyOf(array, actualLength)
  }
}
