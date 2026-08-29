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

package org.scalajs.linker.backend.webassembly.component

import org.junit.Test
import org.junit.Assert._

import org.scalajs.ir.Names.{ClassName, FieldName, SimpleFieldName}
import org.scalajs.ir.WitScope
import org.scalajs.ir.WitTypeDef
import org.scalajs.ir.WasmInterfaceTypes._

import Components.Decl

class ComponentBuilderTest {

  private val scope = WitScope.Interface("test", "pkg", "iface", version = None)

  @Test def encodeInstanceType(): Unit = {
    val f1Class = ClassName("test.F1")
    val r1Class = ClassName("test.R1")

    val f1 = WitTypeDef.Flags(f1Class, scope, "f1", List("b0"))
    val r1 = WitTypeDef.Record(r1Class, scope, "r1",
        List(
          FieldType(FieldName(r1Class, SimpleFieldName("a")), "a", U8Type),
          FieldType(FieldName(r1Class, SimpleFieldName("b")), "b", FlagsTypeRef(f1Class))
        ))

    val typeDefByClass = Map(f1Class -> f1, r1Class -> r1)
    // r1 depends on f1, but r1 comes first
    val decls = ComponentBuilder.encodeInstanceType(
        scope, Nil, List(r1, f1), Nil, Map.empty, typeDefByClass)

    val exportedNames = decls.collect {
      case Decl.ExportTypeEq(_, name, _) => name
    }
    assertEquals(List("f1", "r1"), exportedNames)
  }
}
