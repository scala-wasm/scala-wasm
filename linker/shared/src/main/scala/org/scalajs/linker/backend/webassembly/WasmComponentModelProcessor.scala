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

package org.scalajs.linker.backend.webassembly

import scala.concurrent.{ExecutionContext, Future}

import java.nio.file.Path

import org.scalajs.linker.interface.unstable.OutputDirectoryImpl
import org.scalajs.logging.Logger

private[backend] abstract class WasmComponentModelProcessor {

  /** Process a core wasm module into a component model binary in-place.
   *
   *  This method performs operations in sequence:
   *  1. Embeds WIT definitions from the specified directory using wasm-tools component embed (with utf16 encoding)
   *  2. If autoIncludeWasiImports is true, embeds additional WASI WIT definitions
   *  3. Converts the embedded module into a component using wasm-tools component new
   *
   *  @throws WasmToolsNotFoundException if wasm-tools is not installed
   *  @throws WasmToolsExecutionException if wasm-tools execution fails
   */
  def processComponentModel(
      outputDirectory: OutputDirectoryImpl,
      wasmFileName: String,
      witDirectory: Path,
      worldName: Option[String],
      autoIncludeWasiImports: Boolean,
      logger: Logger
  )(implicit ec: ExecutionContext): Future[Unit]
}

private[backend] object WasmComponentModelProcessor {
  def apply(): WasmComponentModelProcessor =
    WasmComponentModelProcessorPlatform.create()
}

class WasmToolsNotFoundException(message: String) extends Exception(message)

class WasmToolsExecutionException(message: String, cause: Throwable = null)
    extends Exception(message, cause)
