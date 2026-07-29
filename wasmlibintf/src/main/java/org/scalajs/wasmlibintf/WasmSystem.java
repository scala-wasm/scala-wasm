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

package org.scalajs.wasmlibintf;

public final class WasmSystem {
  private WasmSystem() {}

  public static void print(String s, boolean isErr) {
    throw new AssertionError("stub");
  }

  public static long nanoTime() {
    throw new AssertionError("stub");
  }

  public static long currentTimeMillis() {
    throw new AssertionError("stub");
  }

  public static double random() {
    throw new AssertionError("stub");
  }
}
