package org.scalajs.linker.interface

import Fingerprint.FingerprintBuilder

final class WasmFeatures private (
  _exceptionHandling: Boolean,
  _targetPureWasm: Boolean
) {
  import WasmFeatures._

  private def this() = {
    this(
      _exceptionHandling = true,
      _targetPureWasm = false
    )
  }

  val exceptionHandling = _exceptionHandling
  val targetPureWasm = _targetPureWasm

  def withExceptionHandling(exceptionHandling: Boolean): WasmFeatures =
    copy(exceptionHandling = exceptionHandling)

  def withTargetPureWasm(targetPureWasm: Boolean): WasmFeatures =
    copy(targetPureWasm = targetPureWasm)

  override def equals(that: Any): Boolean = that match {
    case that: WasmFeatures =>
      this.exceptionHandling == that.exceptionHandling
    case _ =>
      false
  }

  override def hashCode(): Int = {
    import scala.util.hashing.MurmurHash3._
    var acc = HashSeed
    acc = mix(acc, exceptionHandling.##)
    acc = mixLast(acc, targetPureWasm.##)
    finalizeHash(acc, 2)
  }

  override def toString(): String = {
    s"""WasmFeatures(
       |  exceptionHandling = $exceptionHandling,
       |  targetPureWasm = $targetPureWasm
       |)""".stripMargin
  }

  private def copy(
      exceptionHandling: Boolean = this.exceptionHandling,
      targetPureWasm: Boolean = this.targetPureWasm
  ): WasmFeatures = {
    new WasmFeatures(
        _exceptionHandling = exceptionHandling,
        _targetPureWasm = targetPureWasm
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
        .build()
    }
  }
}
