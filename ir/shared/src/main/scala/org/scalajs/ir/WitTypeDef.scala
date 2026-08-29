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

import Names.ClassName
import WasmInterfaceTypes.{CaseType, FieldType, ResourceRef}

sealed trait ResourceOwnership

object ResourceOwnership {
  case object Own extends ResourceOwnership
  case object Borrow extends ResourceOwnership
}

sealed trait WitTypeDef {
  def className: ClassName
  def scope: WitScope
  def name: String
}

object WitTypeDef {
  final case class Record(className: ClassName, scope: WitScope, name: String,
      fields: List[FieldType])
      extends WitTypeDef

  final case class Variant(className: ClassName, scope: WitScope, name: String, cases: List[CaseType])
      extends WitTypeDef

  final case class Enum(className: ClassName, scope: WitScope, name: String, cases: List[CaseType])
      extends WitTypeDef

  final case class Flags(className: ClassName, scope: WitScope, name: String, names: List[String])
      extends WitTypeDef {
    def numFields: Int = names.size
  }

  final case class Resource(resource: ResourceRef) extends WitTypeDef {
    def className: ClassName = resource.className
    def scope: WitScope = resource.scope
    def name: String = resource.name
  }
}

/** Named WIT `type name = target`, stored on the enclosing module ClassDef. */
final case class WitAliasDef(scope: WitScope, name: String,
    target: WasmInterfaceTypes.ValType)
