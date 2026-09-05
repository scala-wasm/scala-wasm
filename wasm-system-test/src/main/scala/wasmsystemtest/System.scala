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
import wasmsystemtest.wasi.cli.stdout
import wasmsystemtest.wasi.clocks.wall_clock
import java.util.{HashMap, Map}
import scala.scalajs.wit
import scala.scalajs.wit.unsigned.UByte

object System {
  def println(s: String): Unit = {
    val out = stdout.getStdout()
    try {
      val bytes = (s + "\n").getBytes().asInstanceOf[Array[UByte]]
      unwrapResult(out.blockingWriteAndFlush(bytes), "Failed to write to stdout")
    } finally {
      out.close()
    }
  }

  def currentTimeMillis(): scala.Long = {
    val d = wall_clock.now()
    d.seconds.toLong * 1000L + d.nanoseconds.toLong / 1000000L
  }

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

  private def unwrapResult[A, B](r: wit.Result[A, B], message: String): A = {
    if (r.isOk) {
      r.get
    } else {
      throw new RuntimeException(message)
    }
  }
}
