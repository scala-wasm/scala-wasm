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

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.sys.process._

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardCopyOption}

import org.scalajs.linker.interface.unstable.OutputDirectoryImpl
import org.scalajs.logging.Logger

private[backend] object WasmComponentModelProcessorPlatform {
  def create(): WasmComponentModelProcessor = new WasmComponentModelProcessorImpl
}

private final class WasmComponentModelProcessorImpl extends WasmComponentModelProcessor {
  override def processComponentModel(
      outputDirectory: OutputDirectoryImpl,
      wasmFileName: String,
      witContent: String,
      logger: Logger
  )(implicit ec: ExecutionContext): Future[Unit] = {
    checkWasmToolsInstalled()

    outputDirectory.readFull(wasmFileName).flatMap { wasmContentBuffer =>
      Future {
        val tempFile = Files.createTempFile("scala-wasm-", ".wasm")
        val witDirectory = Files.createTempDirectory("scala-wasm-wit-")

        try {
          val wasmContent = new Array[Byte](wasmContentBuffer.remaining())
          wasmContentBuffer.get(wasmContent)
          Files.write(tempFile, wasmContent)

          Files.write(witDirectory.resolve("world.wit"),
              witContent.getBytes(StandardCharsets.UTF_8))

          val wasmFilePath = tempFile.toString

          // Step 1: wasm-tools component embed (in-place)
          val baseEmbedCmd = Seq(
            "wasm-tools",
            "component",
            "embed",
            witDirectory.toString,
            wasmFilePath,
            "-o",
            wasmFilePath
          )
          val embedCmd1 = {
            baseEmbedCmd ++ Seq("-w", WitRenderer.WorldName,
                "--encoding", "utf16")
          }

          logger.info(s"Embedding linker-generated WIT for $wasmFileName")

          runCommand(embedCmd1, "wasm-tools component embed")

          // Step 2: wasm-tools component new
          val newCmd = Seq(
            "wasm-tools",
            "component",
            "new",
            wasmFilePath,
            "-o",
            wasmFilePath
          )

          logger.info(s"Running wasm-tools component new for $wasmFileName")

          runCommand(newCmd, "wasm-tools component new")

          // Read the modified wasm back
          val modifiedWasm = Files.readAllBytes(tempFile)
          ByteBuffer.wrap(modifiedWasm)
        } finally {
          Files.deleteIfExists(tempFile)
        }
      }.flatMap { modifiedWasmBuffer =>
        outputDirectory.writeFull(wasmFileName, modifiedWasmBuffer, skipContentCheck = true)
      }
    }
  }

  private def runCommand(cmd: Seq[String], description: String): Unit = {
    val process = Process(cmd)
    val output = new StringBuilder
    val error = new StringBuilder

    val processLogger = ProcessLogger(
      line => output.append(line).append("\n"),
      line => error.append(line).append("\n")
    )

    val result = process.!(processLogger)
    if (result != 0) {
      val errorMsg =
        s"$description failed with exit code $result:\n${error.toString}"
      throw new WasmToolsExecutionException(errorMsg)
    }
  }

  private def checkWasmToolsInstalled(): Unit = {
    try {
      val checkProcess = Process(Seq("wasm-tools", "--version"))
      val checkResult = checkProcess.!(ProcessLogger(_ => (), _ => ()))
      if (checkResult != 0) {
        throw new WasmToolsNotFoundException(
          "wasm-tools is installed but returned non-zero exit code"
        )
      }
    } catch {
      case _: java.io.IOException =>
        throw new WasmToolsNotFoundException(
          """wasm-tools is not installed or not in PATH.
            |
            |To use component model builds, please install wasm-tools:
            |  https://github.com/bytecodealliance/wasm-tools
            |
            |Installation options:
            |  - cargo install wasm-tools
            |  - brew install wasm-tools (macOS)
            |  - Download from GitHub releases
            |""".stripMargin
        )
    }
  }
}
