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

private[backend] object WasmComponentModelProcessorPlatform {
  def create(): WasmComponentModelProcessor = new WasmComponentModelProcessorStub
}

private final class WasmComponentModelProcessorStub extends WasmComponentModelProcessor {
  override def processComponentModel(
      outputDirectory: OutputDirectoryImpl,
      wasmFileName: String,
      witDirectory: Path,
      worldName: Option[String],
      autoIncludeWasiImports: Boolean,
      logger: Logger
  )(implicit ec: ExecutionContext): Future[Unit] = {
    Future.failed(new UnsupportedOperationException(
      "Component model processing is not supported on the JavaScript platform. " +
      "Please use the JVM version of the linker."
    ))
  }
}
