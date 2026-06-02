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

import java.nio.file.{Files, Path}
import java.nio.charset.StandardCharsets
import java.io.{InputStream, OutputStream}
import java.util.Comparator

private[webassembly] object WasiWitExtractor {
  private val ResourceBase = "/org/scalajs/linker/backend/webassembly/wasi-wit/"
  private val WasiPackages = Seq(
      "wasi-cli-0.2.0",
      "wasi-clocks-0.2.0",
      "wasi-filesystem-0.2.0",
      "wasi-http-0.2.0",
      "wasi-io-0.2.0",
      "wasi-random-0.2.0",
      "wasi-sockets-0.2.0"
  )

  /** Extracts the WASI WIT bundle to a temporary directory. */
  def extractWasiWitToTempDir(): Path = {
    val tempDir = Files.createTempDirectory("scala-wasm-wasi-wit-")

    try {
      extractResource(ResourceBase + "world.wit", tempDir.resolve("world.wit"))
      extractWasiPackages(tempDir)
      tempDir
    } catch {
      case e: Exception =>
        deleteDirectory(tempDir)
        throw new Exception(s"Failed to extract WASI WIT bundle: ${e.getMessage}", e)
    }
  }

  /** Extracts the WIT needed to export the synthetic `wasi:cli/run`. */
  def extractWasiCliRunWitToTempDir(): Path = {
    val tempDir = Files.createTempDirectory("scala-wasm-wasi-cli-run-wit-")

    try {
      Files.write(
          tempDir.resolve("world.wit"),
          """package scala-wasm:cli-run;
            |
            |world cli-run {
            |  export wasi:cli/run@0.2.0;
            |}
            |""".stripMargin.getBytes(StandardCharsets.UTF_8))

      extractWasiPackages(tempDir)
      tempDir
    } catch {
      case e: Exception =>
        deleteDirectory(tempDir)
        throw new Exception(s"Failed to extract WASI CLI run WIT: ${e.getMessage}", e)
    }
  }

  private def extractWasiPackages(targetDir: Path): Unit = {
    val depsDir = targetDir.resolve("deps")
    Files.createDirectories(depsDir)

    for (pkg <- WasiPackages) {
      val pkgDir = depsDir.resolve(pkg)
      Files.createDirectories(pkgDir)
      extractResource(
          ResourceBase + s"deps/$pkg/package.wit",
          pkgDir.resolve("package.wit"))
    }
  }

  /** Extracts a single resource file to the target path. */
  private def extractResource(resourcePath: String, targetPath: Path): Unit = {
    val inputStream = getClass.getResourceAsStream(resourcePath)
    if (inputStream == null) {
      throw new Exception(s"Resource not found: $resourcePath")
    }

    try {
      val outputStream = Files.newOutputStream(targetPath)
      try {
        transferTo(inputStream, outputStream)
      } finally {
        outputStream.close()
      }
    } finally {
      inputStream.close()
    }
  }

  private def transferTo(in: InputStream, out: OutputStream): Unit = {
    val buffer = new Array[Byte](8192)
    var bytesRead = in.read(buffer)
    while (bytesRead != -1) {
      out.write(buffer, 0, bytesRead)
      bytesRead = in.read(buffer)
    }
  }

  def deleteDirectory(dir: Path): Unit = {
    if (Files.exists(dir)) {
      val stream = Files.walk(dir)
      try {
        stream
          .sorted(Comparator.reverseOrder())
          .forEach(Files.delete(_))
      } finally {
        stream.close()
      }
    }
  }
}
