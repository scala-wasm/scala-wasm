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

/** WebAssembly features to use when linking.
 *
 *  The options in `WasmFeatures` specify what features of modern versions of
 *  WebAssembly are used by the Scala.js linker.
 */
final class WasmFeatures private (
    /* We define `val`s separately below so that we can attach Scaladoc to them
     * (putting Scaladoc comments on constructor param `val`s has no effect).
     */
    _useJSPI: Boolean,
    _exceptionHandling: Boolean,
    _witDirectory: Option[String],
    _witWorld: Option[String],
    _autoIncludeWasiImports: Boolean
) {
  import WasmFeatures._

  private def this() = {
    this(
      _useJSPI = false,
      _exceptionHandling = true,
      _witDirectory = None,
      _witWorld = None,
      _autoIncludeWasiImports = true
    )
  }

  /** Enables JSPI, the JavaScript Promise Integration proposal.
   *
   *  Default: `false`
   *
   *  When `true`, the backend supports usage of `js.async/js.await`.
   *
   *  @see [[https://github.com/WebAssembly/js-promise-integration/blob/main/proposals/js-promise-integration/Overview.md]]
   */
  val useJSPI = _useJSPI

  val exceptionHandling = _exceptionHandling
  val witDirectory = _witDirectory
  val witWorld = _witWorld
  val autoIncludeWasiImports = _autoIncludeWasiImports

  def withUseJSPI(useJSPI: Boolean): WasmFeatures =
    copy(useJSPI = useJSPI)

  def withExceptionHandling(exceptionHandling: Boolean): WasmFeatures =
    copy(exceptionHandling = exceptionHandling)

  def withWitDirectory(witDirectory: Option[String]): WasmFeatures =
    copy(witDirectory = witDirectory)

  def withWitWorld(witWorld: Option[String]): WasmFeatures =
    copy(witWorld = witWorld)

  def withAutoIncludeWasiImports(autoIncludeWasiImports: Boolean): WasmFeatures =
    copy(autoIncludeWasiImports = autoIncludeWasiImports)

  override def equals(that: Any): Boolean = that match {
    case that: WasmFeatures =>
      this.useJSPI == that.useJSPI &&
      this.exceptionHandling == that.exceptionHandling &&
      this.witDirectory == that.witDirectory &&
      this.witWorld == that.witWorld &&
      this.autoIncludeWasiImports == that.autoIncludeWasiImports
    case _ =>
      false
  }

  override def hashCode(): Int = {
    import scala.util.hashing.MurmurHash3._
    var acc = HashSeed
    acc = mix(acc, useJSPI.##)
    acc = mix(acc, exceptionHandling.##)
    acc = mix(acc, witDirectory.##)
    acc = mix(acc, witWorld.##)
    acc = mixLast(acc, autoIncludeWasiImports.##)
    finalizeHash(acc, 5)
  }

  override def toString(): String = {
    s"""WasmFeatures(
       |  useJSPI = $useJSPI,
       |  exceptionHandling = $exceptionHandling,
       |  witDirectory = $witDirectory,
       |  witWorld = $witWorld,
       |  autoIncludeWasiImports = $autoIncludeWasiImports
       |)""".stripMargin
  }

  private def copy(
      useJSPI: Boolean = this.useJSPI,
      exceptionHandling: Boolean = this.exceptionHandling,
      witDirectory: Option[String] = this.witDirectory,
      witWorld: Option[String] = this.witWorld,
      autoIncludeWasiImports: Boolean = this.autoIncludeWasiImports
  ): WasmFeatures = {
    new WasmFeatures(
      _useJSPI = useJSPI,
      _exceptionHandling = exceptionHandling,
      _witDirectory = witDirectory,
      _witWorld = witWorld,
      _autoIncludeWasiImports = autoIncludeWasiImports
    )
  }
}

object WasmFeatures {
  private val HashSeed =
    scala.util.hashing.MurmurHash3.stringHash(classOf[WasmFeatures].getName)

  /** Default configuration of Wasm features.
   *
   *  - `useJSPI`: `false`
   *  - `exceptionHandling`: `true`
   *  - `witDirectory`: `None`
   *  - `witWorld`: `None`
   *  - `autoIncludeWasiImports`: `true`
   */
  val Defaults: WasmFeatures = new WasmFeatures()

  private[interface] implicit object WasmFeaturesFingerprint extends Fingerprint[WasmFeatures] {

    override def fingerprint(wasmFeatures: WasmFeatures): String = {
      new FingerprintBuilder("WasmFeatures")
        .addField("useJSPI", wasmFeatures.useJSPI)
        .addField("exceptionHandling", wasmFeatures.exceptionHandling)
        .addField("witDirectory", wasmFeatures.witDirectory)
        .addField("witWorld", wasmFeatures.witWorld)
        .addField("autoIncludeWasiImports", wasmFeatures.autoIncludeWasiImports)
        .build()
    }
  }
}
