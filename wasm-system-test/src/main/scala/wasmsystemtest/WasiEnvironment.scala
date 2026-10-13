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

package wasmsystemtest

import wasmsystemtest.wasi.cli.environment
import java.util.{HashMap, Map}
import scala.scalajs.wit

/** `java.lang.System.getenv` and more, on top of `wasi:cli/environment`.
 *
 *  `java.lang.System.out`, `currentTimeMillis()`, `nanoTime()`, as well as
 *  `Math.random()`, are plugged into the javalib by
 *  `org.scalajs.wasmlibintf.WasmSystem` instead.
 */
object WasiEnvironment {
  def getenv(): Map[String, String] = {
    val vars: Array[wit.Tuple2[String, String]] = environment.getEnvironment()
    val map = new HashMap[String, String](vars.length)
    var i = 0
    while (i < vars.length) {
      val entry = vars(i)
      map.put(entry._1, entry._2)
      i += 1
    }
    map
  }

  def getenv(name: String): String = {
    if (name == null)
      throw new NullPointerException
    getenv().get(name)
  }

  /** WASI-only (`wasi:cli/environment#initial-cwd`). Not a `java.lang.System` API. */
  def initialCwd(): String = {
    val cwd = environment.initialCwd()
    if (cwd.isDefined)
      cwd.get
    else
      null
  }
}
