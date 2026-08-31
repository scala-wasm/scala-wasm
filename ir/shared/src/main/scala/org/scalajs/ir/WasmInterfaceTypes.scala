package org.scalajs.ir

import Names._
import WellKnownNames._
import org.scalajs.ir.{Types => jstpe}

object WasmInterfaceTypes {

  sealed trait WasmInterfaceType

  sealed trait ValType extends WasmInterfaceType {
    def toIRType(): jstpe.Type
  }

  sealed trait ExternType extends WasmInterfaceType

  sealed trait FundamentalType extends ValType

  /** A specialized value types in Wasm Component Model.
   *
   *  Specialized value types are defined by expansion into the `fundamental value types`.
   *
   *  For example:
   *  - A `result` type is more than a variant - it represents success/failure and enables
   *    idiomatic error handling in source languages
   *  - `string` uses Unicode encodings while `list<char>` uses 4-byte char code points
   *  - `flags` uses bit-vectors while equivalent boolean field records use byte sequences
   *
   *  Note that, while Component Model defines `string` and `flags` as specialized value types,
   *  we do not mark them as specialized types, because they have distinct
   *  core Wasm representations distinct from their respective expansions in CanonicalABI.
   *
   *  @see
   *    [[https://github.com/WebAssembly/component-model/blob/main/design/mvp/Explainer.md#specialized-value-types]]
   */
  sealed trait SpecializedType extends ValType

  sealed abstract class PrimValType extends FundamentalType

  case object BoolType extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.BooleanType
  }

  case object U8Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.ByteType
  }

  case object U16Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.ShortType
  }

  case object U32Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.IntType
  }

  case object U64Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.LongType
  }

  case object S8Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.ByteType
  }

  case object S16Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.ShortType
  }

  case object S32Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.IntType
  }

  case object S64Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.LongType
  }

  case object F32Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.FloatType
  }

  case object F64Type extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.DoubleType
  }

  case object CharType extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.CharType
  }

  case object StringType extends PrimValType {
    def toIRType(): jstpe.Type = jstpe.ClassType(BoxedStringClass, true, false)
  }

  final case class ListType(elemType: ValType, length: Option[Int]) extends FundamentalType {
    def toIRType(): jstpe.Type = {
      val ref = toTypeRef(elemType)
      Types.ArrayType(Types.ArrayTypeRef.of(ref), true, false)
    }
  }

  /** label won't be used in load/store with memory or stack, used for Analyzer */
  final case class FieldType(label: FieldName, name: String, tpe: ValType)

  object FieldType {
    def apply(label: FieldName, tpe: ValType): FieldType =
      FieldType(label, "", tpe)
  }

  /** Reference to a named WIT record. Definition is on `ClassDef.witTypeDef`. */
  final case class RecordTypeRef(className: ClassName) extends FundamentalType {
    def toIRType(): jstpe.Type = jstpe.ClassType(className, true, false)
  }

  final case class TupleType(ts: List[ValType]) extends SpecializedType {
    def toIRType(): jstpe.Type =
      jstpe.ClassType(ClassName("scala.scalajs.wit.Tuple" + ts.size), true, false)
  }

  final case class CaseType(className: ClassName, name: String, tpe: Option[ValType]) {
    def toIRType(): jstpe.Type = jstpe.ClassType(className, true, false)
  }

  /** Reference to a named WIT variant. Definition is on `ClassDef.witTypeDef`. */
  final case class VariantTypeRef(className: ClassName) extends FundamentalType {
    def toIRType(): jstpe.Type = jstpe.ClassType(className, true, false)
  }

  final case class ResultType(ok: Option[ValType], err: Option[ValType]) extends SpecializedType {
    def toIRType(): jstpe.Type = jstpe.ClassType(ComponentResultClass, true, false)
  }

  /** Reference to a named WIT enum. Definition is on `ClassDef.witTypeDef`. */
  final case class EnumTypeRef(className: ClassName) extends SpecializedType {
    override def toIRType(): jstpe.Type = jstpe.ClassType(className, true, false)
  }

  final case class OptionType(tpe: ValType) extends SpecializedType {
    def toIRType(): jstpe.Type = jstpe.ClassType(ComponentOptionClass, true, false)
  }

  /** Reference to a named WIT flags. Definition is on `ClassDef.witTypeDef`. */
  final case class FlagsTypeRef(className: ClassName) extends FundamentalType {
    def toIRType(): jstpe.Type = jstpe.ClassType(className, nullable = true, exact = false)
  }

  /** Named WIT type alias reference (`type name = ...`). */
  final case class AliasTypeRef(scope: WitScope, name: String, owner: ClassName) extends ValType {
    def toIRType(): jstpe.Type = {
      throw new AssertionError(
          s"AliasTypeRef($scope, $name, $owner) must be dealiased first")
    }
  }

  /** A WIT resource declaration, without handle ownership.
   *
   *  For example, `streams/input-stream` in:
   *
   *  ```
   *  package wasi:io@0.2.0;
   *  interface streams {
   *    resource input-stream;
   *  }
   *  ```
   *
   *  Used on `WitTypeDef.Resource`. Value types use `ResourceType`.
   */
  final case class ResourceRef(className: ClassName, scope: WitScope, name: String)

  /** A WIT resource handle value type (`own<r>` or `borrow<r>`).
   *
   *  Scope and WIT name live on `WitTypeDef.Resource` for `className`.
   */
  final case class ResourceType(className: ClassName, ownership: ResourceOwnership)
      extends FundamentalType {
    def toIRType(): jstpe.Type = jstpe.ClassType(className, nullable = true, exact = false)
  }

  // ExternTypes
  final case class ParamType(name: String, tpe: ValType)

  final case class FuncType(params: List[ParamType], resultType: Option[ValType]) extends ExternType {
    def paramTypes: List[ValType] = params.map(_.tpe)
  }

  // utilities

  def toTypeRef(tpe: ValType): jstpe.TypeRef = tpe match {
    case BoolType                   => jstpe.BooleanRef
    case U8Type | S8Type            => jstpe.ByteRef
    case U16Type | S16Type          => jstpe.ShortRef
    case U32Type | S32Type          => jstpe.IntRef
    case U64Type | S64Type          => jstpe.LongRef
    case F32Type                    => jstpe.FloatRef
    case F64Type                    => jstpe.DoubleRef
    case CharType                   => jstpe.CharRef
    case StringType                 => jstpe.ClassRef(BoxedStringClass)
    case ListType(elemType, length) =>
      jstpe.ArrayTypeRef.of(toTypeRef(elemType))
    case RecordTypeRef(className)  => jstpe.ClassRef(className)
    case TupleType(ts)             => jstpe.ClassRef(ClassName("scala.scalajs.wit.Tuple" + ts.size))
    case VariantTypeRef(className) => jstpe.ClassRef(className)
    case ResultType(ok, err)       => jstpe.ClassRef(ComponentResultClass)
    case EnumTypeRef(className)    => jstpe.ClassRef(className)
    case OptionType(tpe)           => jstpe.ClassRef(ComponentOptionClass)
    case FlagsTypeRef(className)   => jstpe.ClassRef(className)
    case ResourceType(className, _)       => jstpe.ClassRef(className)
    case AliasTypeRef(scope, name, owner) =>
      throw new AssertionError(
          s"AliasTypeRef($scope, $name, $owner) must be dealiased first")
  }

  def dealias(tpe: ValType, resolve: (WitScope, String) => ValType): ValType = {
    def loop(t: ValType): ValType = t match {
      case AliasTypeRef(scope, name, _) =>
        loop(resolve(scope, name))
      case ListType(elem, len) =>
        ListType(loop(elem), len)
      case TupleType(ts) =>
        TupleType(ts.map(loop))
      case OptionType(inner) =>
        OptionType(loop(inner))
      case ResultType(ok, err) =>
        ResultType(ok.map(loop), err.map(loop))
      case other =>
        other
    }
    loop(tpe)
  }

  def makeCtorName(tpe: Option[ValType]): MethodName = {
    tpe match {
      case None    => MethodName.constructor(Nil)
      case Some(t) => MethodName.constructor(List(toTypeRef(t)))
    }
  }

  // def fromIRType(tpe: jstpe.Type): WasmInterfaceType = {
  //   tpe match {
  //     case jstpe.BooleanType => BoolType
  //     case jstpe.ByteType => S8Type
  //     case jstpe.ShortType => S16Type
  //     case jstpe.IntType => S32Type
  //     case jstpe.LongType => S64Type
  //     case jstpe.FloatType => F32Type
  //     case jstpe.DoubleType => F64Type
  //     case jstpe.CharType => CharType
  //     case jstpe.StringType => StringType
  //     case jstpe.ArrayType(_, _) => ListType(???, ???)
  //     // record
  //     // tuple
  //     // variant
  //     case _ if tpe.typeSymbol.isSubClass() =>
  //     // enum
  //     // option
  //     // result
  //     // flag
  //     // resource
  //     case _ => throw new AssertionError(s"Invalid type $tpe")

  //   }
  // }

}
