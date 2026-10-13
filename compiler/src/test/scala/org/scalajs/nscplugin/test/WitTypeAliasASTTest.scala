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

import org.scalajs.nscplugin.test.util._

import org.scalajs.ir.{WitScope, WitTypeDef}
import org.scalajs.ir.Trees._
import org.scalajs.ir.WasmInterfaceTypes._

import org.junit.Assert._
import org.junit.Test

class WitTypeAliasASTTest extends JSASTTest {

  private val typesScope = WitScope.Interface("test", "test", "types", None)

  private val source = """
    import scala.scalajs.{wit => wm}
    import scala.scalajs.wit.annotation._
    import scala.scalajs.wit.unsigned._

    object types {
      @WitAlias(WitScope.unversioned("test", "test", "types"), "metadata")
      type Metadata = Array[wm.Tuple2[String, String]]
    }

    object callback {
      // WIT `use types.{metadata}`
      type Metadata = types.Metadata

      // Not a WIT alias, just a Scala one
      type Count = UInt

      @WitRecord(WitScope.unversioned("test", "test", "callback"), "event")
      final class Event(@WitName("metadata") val metadata: Metadata)
    }

    object Exports {
      @WitExport(WitScope.unversioned("test", "test", "callback"), "on-event")
      def onEvent(@WitName("metadata") metadata: callback.Metadata,
          @WitName("count") count: callback.Count): callback.Metadata = metadata
    }
  """

  @Test
  def keepWitAliasBehindPlainTypeAliasInSignature(): Unit = {
    val exportDef = source.extractOne("WitExportDef on-event") {
      case d: WitExportDef => d
    }

    exportDef.signature.params.map(_.tpe) match {
      case List(AliasTypeRef(`typesScope`, "metadata", _), U32Type) =>
      case other => fail(s"unexpected param types: $other")
    }
    exportDef.signature.resultType match {
      case Some(AliasTypeRef(`typesScope`, "metadata", _)) =>
      case other => fail(s"unexpected result type: $other")
    }
  }

  @Test
  def keepWitAliasBehindPlainTypeAliasInRecordField(): Unit = {
    val fields = source.extractOne("record event") {
      case cd: ClassDef if cd.witTypeDef.exists(_.isInstanceOf[WitTypeDef.Record]) =>
        cd.witTypeDef.get.asInstanceOf[WitTypeDef.Record].fields
    }

    fields.map(_.tpe) match {
      case List(AliasTypeRef(`typesScope`, "metadata", _)) =>
      case other => fail(s"unexpected field types: $other")
    }
  }
}
