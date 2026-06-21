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

package java.nio

import scala.scalajs.js
import scala.scalajs.js.typedarray._

import scala.scalajs.LinkingInfo
import scala.scalajs.LinkingInfo.moduleKind
import scala.scalajs.LinkingInfo.ModuleKind.{MinimalWasmModule, WasmComponent}

final class ByteOrder private (name: String) {
  override def toString(): String = name
}

object ByteOrder {
  val BIG_ENDIAN: ByteOrder = new ByteOrder("BIG_ENDIAN")
  val LITTLE_ENDIAN: ByteOrder = new ByteOrder("LITTLE_ENDIAN")

  private[nio] val areTypedArraysBigEndian = {
    LinkingInfo.linkTimeIf(moduleKind == MinimalWasmModule || moduleKind == WasmComponent) {
      true
    } {
      if (js.typeOf(js.Dynamic.global.Int32Array) != "undefined") {
        val arrayBuffer = new ArrayBuffer(4)
        (new Int32Array(arrayBuffer))(0) = 0x01020304
        (new Int8Array(arrayBuffer))(0) == 0x01
      } else {
        true // as good a value as any
      }
    }
  }

  def nativeOrder(): ByteOrder = {
    if (areTypedArraysBigEndian) BIG_ENDIAN
    else LITTLE_ENDIAN
  }
}
