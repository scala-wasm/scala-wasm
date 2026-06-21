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

package scala.scalajs.wit.annotation

import scala.annotation.meta._

/** Marks a trait as a Wasm Component Model export interface.
 *
 *  Traits annotated with this annotation must:
 *  - Only contain abstract methods (no concrete implementations)
 *  - All methods must be annotated with @WitExport
 *  - Be implemented in objects marked with @WitImplementation.
 *
 *  Example:
 *  {{{
 *  @WitExportInterface
 *  trait Run {
 *    @WitExport("wasi:cli/run@0.2.0", "run")
 *    def run(): Result[Unit, Unit]
 *  }
 *
 *  @WitImplementation
 *  object RunImpl extends Run {
 *    def run(): Result[Unit, Unit] = {
 *      println("Hello!")
 *      new Ok(())
 *    }
 *  }
 *  }}}
 */
@getter
class WitExportInterface extends scala.annotation.StaticAnnotation
