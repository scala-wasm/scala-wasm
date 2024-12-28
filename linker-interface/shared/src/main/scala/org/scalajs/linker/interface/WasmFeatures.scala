package org.scalajs.linker.interface

import Fingerprint.FingerprintBuilder

final class WasmFeatures private (
  _exceptionHandling: Boolean,
  _useJavaScript: Boolean
) {
  import WasmFeatures._

  private def this() = {
    this(
      _exceptionHandling = true,
      _useJavaScript = true
    )
  }

  val exceptionHandling = _exceptionHandling
  val useJavaScript = _useJavaScript

  def withExceptionHandling(exceptionHandling: Boolean): WasmFeatures =
    copy(exceptionHandling = exceptionHandling)

  def withUseJavaScript(useJavaScript: Boolean): WasmFeatures =
    copy(useJavaScript = useJavaScript)

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
    acc = mixLast(acc, useJavaScript.##)
    finalizeHash(acc, 2)
  }

  override def toString(): String = {
    s"""WasmFeatures(
       |  exceptionHandling = $exceptionHandling,
       |  useJavaScript = $useJavaScript
       |)""".stripMargin
  }

  private def copy(
      exceptionHandling: Boolean = this.exceptionHandling,
      useJavaScript: Boolean = this.useJavaScript
  ): WasmFeatures = {
    new WasmFeatures(
        _exceptionHandling = exceptionHandling,
        _useJavaScript = useJavaScript
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
        .build()
    }
  }
}
