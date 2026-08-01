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

package org.scalajs.ir

sealed abstract class WitScope

object WitScope {

  /** A package-level WIT interface
   *
   *  `interface name` inside `package namespace:package[@version]`.
   */
  final case class Interface(namespace: String, packageName: String,
      name: String, version: Option[String])
      extends WitScope {

    /** The full package identifier, e.g., `wasi:cli@0.2.0`. */
    def witPackage: String =
      namespace + ":" + packageName + version.fold("")("@" + _)

    /** The interface identifier, e.g., `wasi:cli/stdout@0.2.0`. */
    def witId: String =
      namespace + ":" + packageName + "/" + name + version.fold("")("@" + _)
  }

  /** An inline WIT interface nested in a world.
   *
   *  `import name: interface { ... }` or `export name: interface { ... }`.
   *
   *  ```
   *  world app {
   *    import filesystem: interface {
   *      read-file: func(path: string) -> string;
   *    }
   *  }
   *  ```
   */
  final case class Inline(name: String) extends WitScope

  /** The WIT world body itself, for top-level world imports and exports.
   *
   *  ```
   *  world app {
   *    import log: func(message: string);
   *    export run: func();
   *  }
   *  ```
   */
  case object Root extends WitScope

  def importModuleName(scope: WitScope): String = scope match {
    case iface: WitScope.Interface => iface.witId
    case WitScope.Inline(name)     => name
    case WitScope.Root             => "$root"
  }

  def exportName(scope: WitScope, name: String): String = scope match {
    case iface: WitScope.Interface => s"${iface.witId}#$name"
    case WitScope.Inline(inline)   => s"$inline#$name"
    case WitScope.Root             => name
  }
}
