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

/**
 * System features that the javalib needs from the host when there is no JS
 * host, i.e., with {@code ModuleKind.MinimalWasmModule} and
 * {@code ModuleKind.WasmComponent}.
 *
 * <p>The javalib only compiles against this stub; it never ships its IR.
 * A library on the classpath provides the implementation, as a Scala
 * {@code object org.scalajs.wasmlibintf.WasmSystem} with these methods, on
 * top of whatever system interface the application targets (e.g., WASI 0.2
 * {@code wasi:cli/stdout}, {@code wasi:clocks}, {@code wasi:random}).
 * Without such a library, code that reaches these features fails to link,
 * as it did before.
 *
 * <p>Users of the javalib:
 * <ul>
 *   <li>{@code print}: {@code System.out} and {@code System.err}</li>
 *   <li>{@code currentTimeMillis}: {@code System.currentTimeMillis()}</li>
 *   <li>{@code nanoTime}: {@code System.nanoTime()}</li>
 *   <li>{@code random}: {@code Math.random()}, and thus the default seed of
 *     {@code java.util.Random}</li>
 * </ul>
 */
public final class WasmSystem {
  private WasmSystem() {}

  /** Writes {@code s} (a whole line, including its line terminator) to
   *  the standard output, or to the standard error if {@code isErr}.
   */
  public static void print(String s, boolean isErr) {
    throw new AssertionError("stub");
  }

  /** Same contract as {@code System.currentTimeMillis()}. */
  public static long currentTimeMillis() {
    throw new AssertionError("stub");
  }

  /** Same contract as {@code System.nanoTime()}. */
  public static long nanoTime() {
    throw new AssertionError("stub");
  }

  /** Same contract as {@code Math.random()}. */
  public static double random() {
    throw new AssertionError("stub");
  }
}
