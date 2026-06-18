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
    _exceptionHandling: Boolean,
    _witDirectory: Option[String],
    _witWorld: Option[String],
    _autoIncludeWasiImports: Boolean,
    _useJSPI: Boolean
) {
  import WasmFeatures._

  private def this() = {
    this(
      _exceptionHandling = true,
      _witDirectory = None,
      _witWorld = None,
      _autoIncludeWasiImports = true,
      _useJSPI = false
    )
  }

  /** Enables WebAssembly exception handling.
   *
   *  Default: `true`
   */
  val exceptionHandling = _exceptionHandling

  /** WIT directory to use when generating a WebAssembly component.
   *
   *  Default: `None`
   */
  val witDirectory = _witDirectory

  /** WIT world to use when generating a WebAssembly component.
   *
   *  Default: `None`
   */
  val witWorld = _witWorld

  /** Automatically include WASI imports when generating a WebAssembly component.
   *
   *  Default: `true`
   */
  val autoIncludeWasiImports = _autoIncludeWasiImports

  def withExceptionHandling(exceptionHandling: Boolean): WasmFeatures =
    copy(exceptionHandling = exceptionHandling)

  def withWitDirectory(witDirectory: Option[String]): WasmFeatures =
    copy(witDirectory = witDirectory)

  def withWitWorld(witWorld: Option[String]): WasmFeatures =
    copy(witWorld = witWorld)

  def withAutoIncludeWasiImports(autoIncludeWasiImports: Boolean): WasmFeatures =
    copy(autoIncludeWasiImports = autoIncludeWasiImports)

  /** Enables JSPI, the JavaScript Promise Integration proposal.
   *
   *  Default: `false`
   *
   *  When `true`, the backend supports usage of `js.async/js.await`.
   *
   *  @see [[https://github.com/WebAssembly/js-promise-integration/blob/main/proposals/js-promise-integration/Overview.md]]
   */
  val useJSPI = _useJSPI

  def withUseJSPI(useJSPI: Boolean): WasmFeatures =
    copy(useJSPI = useJSPI)

  override def equals(that: Any): Boolean = that match {
    case that: WasmFeatures =>
      this.exceptionHandling == that.exceptionHandling &&
      this.witDirectory == that.witDirectory &&
      this.witWorld == that.witWorld &&
      this.autoIncludeWasiImports == that.autoIncludeWasiImports &&
      this.useJSPI == that.useJSPI
    case _ =>
      false
  }

  override def hashCode(): Int = {
    import scala.util.hashing.MurmurHash3._
    var acc = HashSeed
    acc = mix(acc, exceptionHandling.##)
    acc = mix(acc, witDirectory.##)
    acc = mix(acc, witWorld.##)
    acc = mix(acc, autoIncludeWasiImports.##)
    acc = mixLast(acc, useJSPI.##)
    finalizeHash(acc, 5)
  }

  override def toString(): String = {
    s"""WasmFeatures(
       |  exceptionHandling = $exceptionHandling,
       |  witDirectory = $witDirectory,
       |  witWorld = $witWorld,
       |  autoIncludeWasiImports = $autoIncludeWasiImports,
       |  useJSPI = $useJSPI
       |)""".stripMargin
  }

  private def copy(
      exceptionHandling: Boolean = this.exceptionHandling,
      witDirectory: Option[String] = this.witDirectory,
      witWorld: Option[String] = this.witWorld,
      autoIncludeWasiImports: Boolean = this.autoIncludeWasiImports,
      useJSPI: Boolean = this.useJSPI
  ): WasmFeatures = {
    new WasmFeatures(
      _exceptionHandling = exceptionHandling,
      _witDirectory = witDirectory,
      _witWorld = witWorld,
      _autoIncludeWasiImports = autoIncludeWasiImports,
      _useJSPI = useJSPI
    )
  }
}

object WasmFeatures {
  private val HashSeed =
    scala.util.hashing.MurmurHash3.stringHash(classOf[WasmFeatures].getName)

  /** Default configuration of Wasm features.
   *
   *  - `exceptionHandling`: true
   *  - `witDirectory`: `None`
   *  - `witWorld`: `None`
   *  - `autoIncludeWasiImports`: true
   *  - `useJSPI`: false
   */
  val Defaults: WasmFeatures = new WasmFeatures()

  private[interface] implicit object WasmFeaturesFingerprint extends Fingerprint[WasmFeatures] {

    override def fingerprint(wasmFeatures: WasmFeatures): String = {
      new FingerprintBuilder("WasmFeatures")
        .addField("exceptionHandling", wasmFeatures.exceptionHandling)
        .addField("witDirectory", wasmFeatures.witDirectory)
        .addField("witWorld", wasmFeatures.witWorld)
        .addField("autoIncludeWasiImports", wasmFeatures.autoIncludeWasiImports)
        .addField("useJSPI", wasmFeatures.useJSPI)
        .build()
    }
  }
}
