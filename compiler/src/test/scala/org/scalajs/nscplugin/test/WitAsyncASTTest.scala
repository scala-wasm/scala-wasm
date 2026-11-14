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

package org.scalajs.nscplugin.test

import util._

import org.junit.Test
import org.junit.Assert._

import org.scalajs.ir.{Trees => js}

class WitAsyncASTTest extends JSASTTest {

  private val preamble: String =
    """
    import scala.scalajs.{wit => wm}
    import scala.scalajs.wit.annotation._
    import scala.scalajs.wit.unsigned._
    """

  @Test def asyncFlagInSignatures(): Unit = {
    val ast = stringAST(preamble + """
    object MyImports {
      @WitAsync
      @WitImport(WitScope.unversioned("test", "test", "module"), "get")
      def get(@WitName("key") key: String): String = wm.native

      @WitImport(WitScope.unversioned("test", "test", "module"), "put")
      def put(@WitName("key") key: String): Unit = wm.native
    }

    object MyExports {
      @WitAsync
      @WitExport(WitScope.unversioned("test", "test", "api"), "on-event")
      def onEvent(@WitName("id") id: Int): Int = id

      @WitExport(WitScope.unversioned("test", "test", "api"), "ping")
      def ping(): Unit = ()
    }

    @WitResourceImport(WitScope.unversioned("test", "test", "module"), "conn")
    final class Conn private () extends Object {
      @WitAsync
      @WitResourceMethod("send")
      def send(@WitName("data") data: Array[UByte]): Unit = wm.native

      @WitResourceMethod("id")
      def id(): Int = wm.native

      @WitResourceDrop
      def close(): Unit = wm.native
    }
    object Conn {
      @WitAsync
      @WitResourceStaticMethod("open")
      def open(@WitName("url") url: String): Conn = wm.native

      @WitResourceConstructor
      def apply(): Conn = wm.native
    }
    """)

    val nativeMembers = ast.clDefs.flatMap(_.witNativeMembers)
    val asyncByName = nativeMembers.map { m =>
      js.WitFunctionName.wasmName(m.name) -> m.signature.isAsync
    }.toMap

    assertEquals(
        Map(
          "get" -> true,
          "put" -> false,
          "[method]conn.send" -> true,
          "[method]conn.id" -> false,
          "[resource-drop]conn" -> false,
          "[static]conn.open" -> true,
          "[constructor]conn" -> false
        ),
        asyncByName)

    val exportAsyncByName = ast.clDefs.flatMap(_.topLevelExportDefs).collect {
      case js.WitExportDef(_, name, _, signature) =>
        js.WitFunctionName.wasmName(name) -> signature.isAsync
    }.toMap

    assertEquals(Map("on-event" -> true, "ping" -> false), exportAsyncByName)
  }
}
