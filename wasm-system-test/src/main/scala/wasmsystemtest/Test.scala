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

package wasmsystemtest

import java.util.{Random, UUID}

object Test {
  def main(args: Array[String]): Unit = {
    // Plugged in by org.scalajs.wasmlibintf.WasmSystem
    System.out.println("wasm system test")
    System.err.println("to stderr")
    System.out.print("partial ")
    System.out.println("line")

    val millis = System.currentTimeMillis()
    check(millis >= 1360059308000L, "currentTimeMillis=" + millis) // first commit of Scala.js
    System.out.println("currentTimeMillis=" + millis)

    val nanos1 = System.nanoTime()
    val nanos2 = System.nanoTime()
    check(nanos2 >= nanos1, "nanoTime " + nanos1 + " then " + nanos2)

    val r = Math.random()
    check(r >= 0.0 && r < 1.0, "Math.random()=" + r)
    val seeded1 = new Random().nextLong()
    val seeded2 = new Random().nextLong()
    check(seeded1 != seeded2, "two new Random() gave " + seeded1)

    // Through the java.security.SecureRandom of this project
    val uuid = UUID.randomUUID()
    check(uuid.version() == 4, "UUID.randomUUID()=" + uuid)
    check(uuid != UUID.randomUUID(), "two equal random UUIDs " + uuid)

    // Through wasi:cli/environment directly
    val javaHome = WasiEnvironment.getenv("JAVA_HOME")
    if (javaHome != null)
      System.out.println("JAVA_HOME=" + javaHome)
    else
      System.out.println("JAVA_HOME is not set")

    val cwd = WasiEnvironment.initialCwd()
    if (cwd != null)
      System.out.println("initial-cwd=" + cwd)
    else
      System.out.println("initial-cwd is not set")
  }

  private def check(cond: Boolean, message: String): Unit = {
    if (!cond)
      throw new AssertionError(message)
  }
}
