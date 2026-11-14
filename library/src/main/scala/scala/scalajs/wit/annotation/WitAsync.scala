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

/** Marks a WIT function as `async func` in the component type.
 *
 *  Only the function type changes. The function is still lowered and lifted
 *  with the synchronous Canonical ABI, so the Scala signature is the same.
 *
 *  This annotation is valid together with `@WitImport`, `@WitExport`,
 *  `@WitResourceMethod` and `@WitResourceStaticMethod`. WIT does not allow
 *  async constructors.
 *
 *  Example:
 *  {{{
 *  // get: async func(key: string) -> option<string>;
 *  @WitAsync
 *  @WitImport(WitScope("example", "kv", "store", "0.1.0"), "get")
 *  def get(@WitName("key") key: String): Option[String] = native
 *  }}}
 */
@field @getter @setter
class WitAsync extends scala.annotation.StaticAnnotation
