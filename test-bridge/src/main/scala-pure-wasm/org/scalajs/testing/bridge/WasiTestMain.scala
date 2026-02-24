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

import scala.util.control.NonFatal

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

@WitExportInterface
private[bridge] trait WasiCliRunExports {
  @WitExport("wasi:cli/run@0.2.0", "run")
  def run(): wit.Result[Unit, Unit]
}

@WitImplementation
private[bridge] object WasiCliRunExportsImpl extends WasiCliRunExports {
  override def run(): wit.Result[Unit, Unit] = {
    try {
      Bridge.start()
      wit.Ok(())
    } catch {
      case NonFatal(_) =>
        wit.Err(())
    }
  }
}
