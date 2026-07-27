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

import org.scalajs.ir.WitScope

import Fingerprint.FingerprintBuilder

/** The Wasm Component export name used to invoke the module initializers.
 *
 *  The linker includes this export in its generated WIT world.
 *
 *  For Wasm modules, we generate initializers in `_start` function, that runs
 *  when the module is instantiated. In the Component Model, calling WIT functions
 *  from `_start` isn't safe, it can access component imports before those components
 *  are initialized. Therefore, for `ModuleKind.WasmComponent`, module initializers are
 *  invoked from the configured component export instead of `_start`.
 */
final class WasmComponentModuleInitializerExport private (
    val scope: WitScope,
    val functionName: String,
    val resultType: WasmComponentModuleInitializerExport.ResultType
) {
  require(functionName.nonEmpty, "functionName must not be empty")

  def exportName: String = scope match {
    case WitScope.Root             => functionName
    case iface: WitScope.Interface => s"${iface.witId}#$functionName"
    case WitScope.Inline(name)     => s"$name#$functionName"
  }

  override def equals(that: Any): Boolean = that match {
    case that: WasmComponentModuleInitializerExport =>
      this.scope == that.scope &&
      this.functionName == that.functionName &&
      this.resultType == that.resultType
    case _ =>
      false
  }

  override def hashCode(): Int =
    (scope, functionName, resultType).##

  override def toString(): String =
    s"WasmComponentModuleInitializerExport($scope, $functionName, $resultType)"
}

object WasmComponentModuleInitializerExport {

  def apply(scope: WitScope, functionName: String,
      resultType: ResultType): WasmComponentModuleInitializerExport = {
    new WasmComponentModuleInitializerExport(scope, functionName, resultType)
  }

  /** WIT result types for a module-initializer export. */
  sealed abstract class ResultType private ()

  object ResultType {

    /** WIT `unit` */
    case object Unit extends ResultType

    /** WIT `result<unit, unit>`. */
    case object ResultUnitUnit extends ResultType
  }

  private[interface] implicit object ResultTypeFingerprint extends Fingerprint[ResultType] {
    override def fingerprint(resultType: ResultType): String = resultType match {
      case ResultType.Unit           => "Unit"
      case ResultType.ResultUnitUnit => "ResultUnitUnit"
    }
  }

  private[interface] implicit object WasmComponentModuleInitializerExportFingerprint
      extends Fingerprint[WasmComponentModuleInitializerExport] {
    override def fingerprint(componentExport: WasmComponentModuleInitializerExport): String = {
      new FingerprintBuilder("WasmComponentModuleInitializerExport")
        .addField("scope", componentExport.scope.toString)
        .addField("functionName", componentExport.functionName)
        .addField("resultType", componentExport.resultType)
        .build()
    }
  }

  def fingerprint(componentExport: WasmComponentModuleInitializerExport): String =
    Fingerprint.fingerprint(componentExport)
}
