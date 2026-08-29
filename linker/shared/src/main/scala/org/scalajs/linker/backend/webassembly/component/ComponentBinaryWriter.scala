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

import java.nio.ByteBuffer

import org.scalajs.linker.backend.webassembly.BinaryWriter.Buffer

import Components._

/** Binary writer for Wasm Component. */
private sealed class ComponentBinaryWriter(component: Component) {
  import ComponentBinaryWriter._

  private val buf = new Buffer()

  def write(): ByteBuffer = {
    // magic | version | layer
    buf.byte(0)
    buf.byte('a')
    buf.byte('s')
    buf.byte('m')
    buf.byte(0x0d)
    buf.byte(0)
    buf.byte(1)
    buf.byte(0)

    writeSection(SectionType) {
      buf.u32(1)
      buf.byte(0x41) // componenttype
      writeDeclVec(component.decls, parentTypeIdx = Map.empty)
    }

    writeSection(SectionExport) {
      // Decoder finds the world via this type export.
      buf.u32(1)
      writeNameAttributes(buf, component.worldName)
      buf.byte(SortType)
      buf.u32(0)
      buf.byte(0x00)
    }

    // wasm-tools convention:
    // > //! Currently the component additionally has a custom section named
    // > //! `wit-component-encoding` (see `CUSTOM_SECTION_NAME`). This section is
    // > //! currently defined as 2 bytes:
    // > //! * The first byte is `CURRENT_VERSION` to help protect against future and
    // > //!   past changes.
    // > //! * The second byte indicates the string encoding used for imports/exports as
    // > //!   part of the bindings process. The mapping is defined by
    // > //!   `encode_string_encoding`.
    // [[https://github.com/bytecodealliance/wasm-tools/blob/a2b006b52915754823c01495c34f4ec247efc9b4/crates/wit-component/src/metadata.rs#L1-L42]]
    writeCustomSection("wit-component-encoding") {
      buf.byte(EncodingVersion)
      buf.byte(StringEncodingUTF16)
    }

    buf.result()
  }

  private def writeSection(sectionID: Byte)(sectionContent: => Unit): Unit = {
    buf.byte(sectionID)
    buf.byteLengthSubSection(sectionContent)
  }

  private def writeCustomSection(customSectionName: String)(
      sectionContent: => Unit): Unit = {
    writeSection(SectionCustom) {
      buf.name(customSectionName)
      sectionContent
    }
  }

  private def writeDeclVec(decls: List[Decl], parentTypeIdx: Map[TypeID, Int]): Unit = {
    val typeIdxValues: Map[TypeID, Int] =
      decls.collect { case t: TypeDecl => t.id }.zipWithIndex.toMap
    val instanceIdxValues: Map[InstanceID, Int] =
      decls.collect { case Decl.Import(_, _, Some(id)) => id }.zipWithIndex.toMap

    buf.u32(decls.size)
    for (decl <- decls)
      writeDecl(decl, typeIdxValues, instanceIdxValues, parentTypeIdx)
  }

  /** One `componentdecl` or `instancedecl`
   *  ([[https://github.com/WebAssembly/component-model/blob/main/design/mvp/Binary.md]]).
   *
   *  {{{
   *  componentdecl ::= 0x03 <importdecl>
   *                  | <instancedecl>
   *  instancedecl  ::= 0x00 t:<core:type> => t
   *                  | 0x01 <type>
   *                  | 0x02 <alias>
   *                  | 0x04 <exportdecl>
   *  }}}
   */
  private def writeDecl(decl: Decl, typeIdx: Map[TypeID, Int],
      instanceIdx: Map[InstanceID, Int], parentTypeIdx: Map[TypeID, Int]): Unit = {
    decl match {
      case Decl.ComponentType(_, nested) =>
        buf.byte(0x01)
        buf.byte(0x41)
        writeDeclVec(nested, parentTypeIdx = typeIdx)

      case Decl.InstanceType(_, nested) =>
        buf.byte(0x01)
        buf.byte(0x42)
        writeDeclVec(nested, parentTypeIdx = typeIdx)

      case Decl.FuncType(_, params, result) =>
        buf.byte(0x01)
        buf.byte(0x40)
        buf.vec(params) { case (name, tpe) =>
          buf.name(name)
          writeValRef(tpe, typeIdx)
        }
        // resultlist ::= 0x00 t:<valtype> => (result t)
        //              | 0x01 0x00
        result match {
          case Some(tpe) =>
            buf.byte(0x00)
            writeValRef(tpe, typeIdx)
          case None =>
            buf.byte(0x01)
            buf.byte(0x00)
        }

      // defvaltype    ::= ...
      //    | 0x72 lt*:vec(<labelvaltype>) => (record (field lt)*)    (if |lt*| > 0)
      case Decl.Record(_, fields) =>
        buf.byte(0x01)
        buf.byte(0x72)
        buf.vec(fields) { case (name, tpe) =>
          buf.name(name)
          writeValRef(tpe, typeIdx)
        }

      case Decl.Variant(_, cases) =>
        buf.byte(0x01)
        buf.byte(0x71)
        buf.vec(cases) { case (name, tpe) =>
          buf.name(name)
          tpe match {
            case Some(r) =>
              buf.byte(0x01) // <T>? ::= 0x01 t:<T>
              writeValRef(r, typeIdx)
            case None =>
              buf.byte(0x00) // <T>? ::= 0x00
          }
          buf.byte(0x00) // case ::= ... 0x00
        }

      case Decl.Enum(_, cases) =>
        buf.byte(0x01)
        buf.byte(0x6d)
        buf.vec(cases)(buf.name)

      case Decl.Flags(_, names) =>
        buf.byte(0x01)
        buf.byte(0x6e)
        buf.vec(names)(buf.name)

      case Decl.ListTy(_, elem) =>
        buf.byte(0x01)
        buf.byte(0x70)
        writeValRef(elem, typeIdx)

      case Decl.FixedList(_, elem, len) =>
        buf.byte(0x01)
        buf.byte(0x67)
        writeValRef(elem, typeIdx)
        buf.u32(len)

      case Decl.Tuple(_, elems) =>
        buf.byte(0x01)
        buf.byte(0x6f)
        buf.vec(elems)(writeValRef(_, typeIdx))

      case Decl.OptionTy(_, inner) =>
        buf.byte(0x01)
        buf.byte(0x6b)
        writeValRef(inner, typeIdx)

      case Decl.ResultTy(_, ok, err) =>
        buf.byte(0x01)
        buf.byte(0x6a)
        ok match {
          case Some(r) => buf.byte(0x01); writeValRef(r, typeIdx)
          case None    => buf.byte(0x00)
        }
        err match {
          case Some(r) => buf.byte(0x01); writeValRef(r, typeIdx)
          case None    => buf.byte(0x00)
        }

      case Decl.Own(_, resource) =>
        buf.byte(0x01)
        buf.byte(0x69)
        buf.u32(typeIdx(resource))

      case Decl.Borrow(_, resource) =>
        buf.byte(0x01)
        buf.byte(0x68)
        buf.u32(typeIdx(resource))

      // defvaltype ::= pvt:<primvaltype> => pvt  (and other valtypes already covered)
      case Decl.ValTypeDef(_, v) =>
        buf.byte(0x01)
        writeValRef(v, typeIdx)

      case Decl.AliasExport(_, instance, name) =>
        buf.byte(0x02)
        buf.byte(SortType) // sort ::= 0x03 type
        buf.byte(0x00)
        buf.u32(instanceIdx(instance))
        buf.name(name)

      case Decl.AliasOuter(_, outer) =>
        buf.byte(0x02)
        buf.byte(SortType)
        buf.byte(0x02)
        buf.u32(1) // enclosing componenttype
        buf.u32(parentTypeIdx(outer))

      case Decl.ExportResource(_, name) =>
        buf.byte(0x04)
        writeNameAttributes(buf, name)
        buf.byte(0x03)
        buf.byte(0x01)

      case Decl.ExportTypeEq(_, name, ty) =>
        buf.byte(0x04)
        writeNameAttributes(buf, name)
        buf.byte(0x03)
        buf.byte(0x00)
        buf.u32(typeIdx(ty))

      case Decl.ImportTypeEq(_, name, ty) =>
        buf.byte(0x03)
        writeNameAttributes(buf, name)
        buf.byte(0x03)
        buf.byte(0x00) // typebound ::= eq
        buf.u32(typeIdx(ty))

      case Decl.ImportTypeResource(_, name) =>
        buf.byte(0x03)
        writeNameAttributes(buf, name)
        buf.byte(0x03)
        buf.byte(0x01) // typebound ::= sub resource

      case Decl.Import(name, externtype, _) =>
        buf.byte(0x03)
        writeNameAttributes(buf, name)
        writeExternType(externtype, typeIdx)

      case Decl.Export(name, externtype) =>
        buf.byte(0x04)
        writeNameAttributes(buf, name)
        writeExternType(externtype, typeIdx)
    }
  }

  // externtype ::= 0x01|0x04|0x05 <typeidx>  (func | component | instance)
  private def writeExternType(externtype: ExternType, typeIdx: Map[TypeID, Int]): Unit = {
    externtype match {
      case ExternType.Func(ty) =>
        buf.byte(0x01)
        buf.u32(typeIdx(ty))
      case ExternType.Instance(ty) =>
        buf.byte(0x05)
        buf.u32(typeIdx(ty))
      case ExternType.Component(ty) =>
        buf.byte(0x04)
        buf.u32(typeIdx(ty))
    }
  }

  // valtype ::= <primvaltype> | <typeidx>
  private def writeValRef(ref: ValRef, typeIdx: Map[TypeID, Int]): Unit = ref match {
    case ValRef.Bool     => buf.byte(0x7f)
    case ValRef.S8       => buf.byte(0x7e)
    case ValRef.U8       => buf.byte(0x7d)
    case ValRef.S16      => buf.byte(0x7c)
    case ValRef.U16      => buf.byte(0x7b)
    case ValRef.S32      => buf.byte(0x7a)
    case ValRef.U32      => buf.byte(0x79)
    case ValRef.S64      => buf.byte(0x78)
    case ValRef.U64      => buf.byte(0x77)
    case ValRef.F32      => buf.byte(0x76)
    case ValRef.F64      => buf.byte(0x75)
    case ValRef.Char     => buf.byte(0x74)
    case ValRef.String   => buf.byte(0x73)
    case ValRef.Type(id) => buf.s33OfUInt(typeIdx(id))
  }
}

object ComponentBinaryWriter {
  private final val EncodingVersion: Byte = 0x04
  private final val StringEncodingUTF16: Byte = 0x01

  private final val SectionCustom = 0x00.toByte
  private final val SectionType = 0x07.toByte
  private final val SectionExport = 0x0b.toByte

  private final val SortType: Byte = 0x03

  def write(component: Component): ByteBuffer =
    new ComponentBinaryWriter(component).write()

  private def writeNameAttributes(buf: Buffer, name: String): Unit = {
    buf.byte(0x00)
    buf.name(name)
  }
}
