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

object Test {
  def main(args: Array[String]): Unit = {
    System.println("wasm system test")

    val javaHome = System.getenv("JAVA_HOME")
    if (javaHome != null)
      System.println("JAVA_HOME=" + javaHome)
    else
      System.println("JAVA_HOME is not set")

    val cwd = System.initialCwd()
    if (cwd != null)
      System.println("initial-cwd=" + cwd)
    else
      System.println("initial-cwd is not set")

    System.println("currentTimeMillis=" + System.currentTimeMillis())
  }
}
