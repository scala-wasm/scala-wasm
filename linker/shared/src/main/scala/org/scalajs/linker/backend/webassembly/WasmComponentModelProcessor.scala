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

import org.scalajs.linker.Nullables._
import org.scalajs.linker.interface.unstable.OutputDirectoryImpl
import org.scalajs.logging.Logger

private[backend] abstract class WasmComponentModelProcessor {

  /** Process a core wasm module into a component model binary in-place.
   *
   *  This method performs operations in sequence:
   *  1. Writes and embeds linker-generated WIT definitions using wasm-tools
   *     component embed (with utf16 encoding)
   *     TODO: generate Wasm Component Model binary directly, instead of using wasm-tools?
   *  2. Converts the embedded module into a component using wasm-tools component new
   *
   *  @throws WasmToolsNotFoundException if wasm-tools is not installed
   *  @throws WasmToolsExecutionException if wasm-tools execution fails
   */
  def processComponentModel(
      outputDirectory: OutputDirectoryImpl,
      wasmFileName: String,
      witContent: String,
      logger: Logger
  )(implicit ec: ExecutionContext): Future[Unit]
}

private[backend] object WasmComponentModelProcessor {
  def apply(): WasmComponentModelProcessor =
    WasmComponentModelProcessorPlatform.create()
}

class WasmToolsNotFoundException(message: String) extends Exception(message)

class WasmToolsExecutionException(message: String, cause: Nullable[Throwable] = null)
    extends Exception(message, cause)
