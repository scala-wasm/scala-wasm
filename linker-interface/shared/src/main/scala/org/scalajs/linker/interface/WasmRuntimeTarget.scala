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

package org.scalajs.linker.interface

import Fingerprint.FingerprintBuilder

final class WasmRuntimeTarget private (val name: String) {

  override def equals(that: Any): Boolean = that match {
    case target: WasmRuntimeTarget => this.name == target.name
    case _ => false
  }

  val isJS: Boolean = this == WasmRuntimeTarget.JS

  override def hashCode(): Int = name.##

  override def toString(): String = name
}

object WasmRuntimeTarget {
  val JS: WasmRuntimeTarget = new WasmRuntimeTarget("js")
  val WASIP1: WasmRuntimeTarget = new WasmRuntimeTarget("wasi_snapshot_preview1")

  private[interface] implicit object WasmRuntimeTargetFingerprint
      extends Fingerprint[WasmRuntimeTarget] {

    override def fingerprint(target: WasmRuntimeTarget): String = {
      new FingerprintBuilder("WasmRuntimeTarget")
        .addField("name", target.name)
        .build()
    }
  }
}
