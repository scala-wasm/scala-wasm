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
import java.io.{InputStream, OutputStream}
import java.util.Comparator

private[webassembly] object WasiWitExtractor {
  private val ResourceBase = "/org/scalajs/linker/backend/webassembly/wasi-wit/"

  /** Extracts the WASI WIT bundle to a temporary directory. */
  def extractWasiWitToTempDir(): Path = {
    val tempDir = Files.createTempDirectory("scala-wasm-wasi-wit-")

    try {
      extractResource(ResourceBase + "world.wit", tempDir.resolve("world.wit"))

      val depsDir = tempDir.resolve("deps")
      Files.createDirectories(depsDir)

      // List of WASI packages to extract
      val wasiPackages = Seq(
          "wasi-cli-0.2.0",
          "wasi-clocks-0.2.0",
          "wasi-filesystem-0.2.0",
          "wasi-http-0.2.0",
          "wasi-io-0.2.0",
          "wasi-random-0.2.0",
          "wasi-sockets-0.2.0"
      )

      for (pkg <- wasiPackages) {
        val pkgDir = depsDir.resolve(pkg)
        Files.createDirectories(pkgDir)
        extractResource(
          ResourceBase + s"deps/$pkg/package.wit",
          pkgDir.resolve("package.wit")
        )
      }
      tempDir
    } catch {
      case e: Exception =>
        deleteDirectory(tempDir)
        throw new Exception(s"Failed to extract WASI WIT bundle: ${e.getMessage}", e)
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
