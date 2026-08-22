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

  /** Convert a core wasm module (with `component-type` custom section) into a
   *  component binary in-place via `wasm-tools component new`.
   *
   *  @throws WasmToolsNotFoundException if wasm-tools is not installed
   *  @throws WasmToolsExecutionException if wasm-tools execution fails
   */
  def processComponentModel(
      outputDirectory: OutputDirectoryImpl,
      wasmFileName: String,
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
