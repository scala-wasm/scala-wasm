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

/** Marks a class as the implementation of an exported WIT resource.
 *
 *  An instance of the class is a value of the resource. The functions of the
 *  resource use the same annotations as an imported resource (see
 *  [[WitResourceImport]]):
 *
 *  - `@WitResourceMethod` on a method of the class.
 *  - `@WitResourceConstructor` on an `apply` method of the companion object.
 *  - `@WitResourceStaticMethod` on a method of the companion object.
 *
 *  {{{
 *  @WitResourceExport(WitScope.unversioned("ns", "pkg", "iface"), "counter")
 *  final class Counter(private var value: Int) {
 *    @WitResourceMethod("get")
 *    def get(): Int = value
 *
 *    @WitResourceMethod("add")
 *    def add(@WitName("n") n: Int): Unit = value += n
 *  }
 *
 *  object Counter {
 *    @WitResourceConstructor
 *    def apply(@WitName("init") init: Int): Counter = new Counter(init)
 *
 *    @WitResourceStaticMethod("zero")
 *    def zero(): Counter = new Counter(0)
 *  }
 *  }}}
 *
 *  The other members of the class and of its companion object are ordinary
 *  Scala members.
 *
 *  An `own<counter>` that leaves the component (a result of an export, or a
 *  parameter of an import) gets a new handle for the instance. A
 *  `borrow<counter>` or `own<counter>` that comes in gives back the same
 *  instance. The component keeps an instance alive while a handle to it
 *  exists; after the last handle is dropped, the garbage collector owns it.
 */
@field @getter @setter
class WitResourceExport private () extends scala.annotation.StaticAnnotation {
  def this(scope: WitScope, name: String) = this()
}
