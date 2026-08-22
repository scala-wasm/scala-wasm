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

package org.scalajs.testing.bridge

import scalajs.wit
import scalajs.wit.annotation.{WitImport, WitName, WitScope}

import java.util.Optional

object TestRpcTransport {
  @WitImport(WitScope.unversioned("scalajs", "test-rpc", "rpc"), "init")
  def init(): Unit = wit.native

  @WitImport(WitScope.unversioned("scalajs", "test-rpc", "rpc"), "send")
  def send(@WitName("msg") msg: String): Unit = wit.native

  @WitImport(WitScope.unversioned("scalajs", "test-rpc", "rpc"), "poll")
  def poll(): Optional[String] = wit.native
}
