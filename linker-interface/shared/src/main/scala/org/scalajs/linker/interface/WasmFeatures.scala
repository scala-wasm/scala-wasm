package org.scalajs.linker.interface

import Fingerprint.FingerprintBuilder

final class WasmFeatures private (
  _exceptionHandling: Boolean
) {
  import WasmFeatures._

  private def this() = {
    this(
      _exceptionHandling = true
    )
  }

  val exceptionHandling = _exceptionHandling

  def withExceptionHandling(exceptionHandling: Boolean): WasmFeatures =
    copy(exceptionHandling = exceptionHandling)

  override def equals(that: Any): Boolean = that match {
    case that: WasmFeatures =>
      this.exceptionHandling == that.exceptionHandling
    case _ =>
      false
  }

  override def hashCode(): Int = {
    import scala.util.hashing.MurmurHash3._
    var acc = HashSeed
    acc = mixLast(acc, exceptionHandling.##)
    finalizeHash(acc, 4)
  }

  override def toString(): String = {
    s"""WasmFeatures(
       |  exceptionHandling = $exceptionHandling,
       |)""".stripMargin
  }

  private def copy(
      exceptionHandling: Boolean = this.exceptionHandling,
  ): WasmFeatures = {
    new WasmFeatures(
        _exceptionHandling = exceptionHandling,
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
