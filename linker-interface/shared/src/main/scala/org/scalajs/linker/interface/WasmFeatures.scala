package org.scalajs.linker.interface

import Fingerprint.FingerprintBuilder

final class WasmFeatures private (
  _exceptionHandling: Boolean,
  _targetPureWasm: Boolean,
  _componentModel: Boolean
) {
  import WasmFeatures._

  private def this() = {
    this(
      _exceptionHandling = true,
      _targetPureWasm = false,
      _componentModel = false
    )
  }

  val exceptionHandling = _exceptionHandling
  val targetPureWasm = _targetPureWasm
  val componentModel = _componentModel

  def withExceptionHandling(exceptionHandling: Boolean): WasmFeatures =
    copy(exceptionHandling = exceptionHandling)

  def withTargetPureWasm(targetPureWasm: Boolean): WasmFeatures =
    copy(targetPureWasm = targetPureWasm)

  def withComponentModel(componentModel: Boolean): WasmFeatures =
    copy(componentModel = componentModel)

  override def equals(that: Any): Boolean = that match {
    case that: WasmFeatures =>
      this.exceptionHandling == that.exceptionHandling &&
      this.targetPureWasm == that.targetPureWasm &&
      this.componentModel == that.componentModel
    case _ =>
      false
  }

  override def hashCode(): Int = {
    import scala.util.hashing.MurmurHash3._
    var acc = HashSeed
    acc = mix(acc, exceptionHandling.##)
    acc = mix(acc, targetPureWasm.##)
    acc = mixLast(acc, componentModel.##)
    finalizeHash(acc, 3)
  }

  override def toString(): String = {
    s"""WasmFeatures(
       |  exceptionHandling = $exceptionHandling,
       |  targetPureWasm = $targetPureWasm
       |  componentModel = $componentModel
       |)""".stripMargin
  }

  private def copy(
      exceptionHandling: Boolean = this.exceptionHandling,
      targetPureWasm: Boolean = this.targetPureWasm,
      componentModel: Boolean = this.componentModel
  ): WasmFeatures = {
    new WasmFeatures(
        _exceptionHandling = exceptionHandling,
        _targetPureWasm = targetPureWasm,
        _componentModel = componentModel
    )
  }
}

object WasmFeatures {
  private val HashSeed =
    scala.util.hashing.MurmurHash3.stringHash(classOf[WasmFeatures].getName)

  val Defaults: WasmFeatures = new WasmFeatures()

  private[interface] implicit object WasmFeaturesFingerprint
      extends Fingerprint[WasmFeatures] {

    override def fingerprint(wasmFeatures: WasmFeatures): String = {
      new FingerprintBuilder("WasmFeatures")
        .addField("exceptionHandling", wasmFeatures.exceptionHandling)
        .addField("targetPureWasm", wasmFeatures.targetPureWasm)
        .addField("componentModel", wasmFeatures.componentModel)
        .build()
    }
  }
}
