package org.scalajs.linker.backend.wasmemitter.canonicalabi

import org.scalajs.ir.Types._
import org.scalajs.ir.{WasmInterfaceTypes => wit}
import org.scalajs.ir.Names._
import org.scalajs.ir.Trees.MemberNamespace
import org.scalajs.ir.OriginalName.NoOriginalName

import org.scalajs.linker.backend.webassembly.FunctionBuilder
import org.scalajs.linker.backend.webassembly.{Instructions => wa}
import org.scalajs.linker.backend.webassembly.{Modules => wamod}
import org.scalajs.linker.backend.webassembly.{Identitities => wanme}
import org.scalajs.linker.backend.webassembly.{Types => watpe}
import org.scalajs.linker.backend.webassembly.Types.{FunctionType => Sig}
import org.scalajs.linker.backend.wasmemitter.VarGen._
import org.scalajs.linker.backend.wasmemitter.SpecialNames
import org.scalajs.linker.backend.wasmemitter.TypeTransformer._

import org.scalajs.linker.backend.webassembly.component.Flatten


object CABIToScalaJS {
  def genAdaptScalaJS(fb: FunctionBuilder, tpe: wit.ValType, vi: Iterator[wanme.LocalID]): Unit = {
    tpe match {
      // Scala.js has a same representation
      case wit.BoolType | wit.S8Type | wit.S16Type | wit.S32Type | wit.S64Type |
          wit.U8Type | wit.U16Type | wit.U32Type | wit.U64Type |
          wit.F32Type | wit.F64Type =>
        fb += wa.LocalGet(vi.next())

      case wit.VoidType =>

      case wit.ResourceType(_) =>
        fb += wa.LocalGet(vi.next())

      case wit.VariantType(_, cases) =>
        val flattened = Flatten.flattenVariants(cases.map(_.tpe))
        fb.switch(
          Sig(Nil, List(watpe.Int32)),
          Sig(Nil, List(watpe.RefType(false, genTypeID.ObjectStruct)))
        ) { () =>
          fb += wa.LocalGet(vi.next()) // case index
        }(
          cases.zipWithIndex.map { case (c, i) =>
            val ctor = c.tpe.toIRType() match {
              case pt: PrimTypeWithRef => MethodName.constructor(List(pt.primRef))
              case ClassType(className, _) => MethodName.constructor(List(ClassRef(className)))
              case StringType => SpecialNames.StringArgConstructorName
              case AnyType => SpecialNames.AnyArgConstructorName
              case AnyNotNullType => SpecialNames.AnyArgConstructorName
              case ArrayType(_, _) => ???
              case RecordType(_) => ???
              case UndefType => ???
              case _: WasmComponentResourceType => ???
            }
            (List(i), () => {
              genNewScalaClass(fb, c.className, ctor) {
                val expect = Flatten.flattenType(c.tpe)
                val newLocals = expect.map(t => fb.addLocal(NoOriginalName, t))
                genCoerceValues(fb, vi, flattened, expect)
                newLocals.reverse.foreach { l =>
                  fb += wa.LocalSet(l)
                }
                genAdaptScalaJS(fb, c.tpe, newLocals.toIterator)
              }
            })
          }: _*
        ) { () =>
          fb += wa.Unreachable
        }


      // case tpe @ WasmComponentResultType(ok, err) =>
      //   val okType = transformWIT(ok)
      //   val errType = transformWIT(err)
      //   val flattened = Flatten.flattenVariants(okType.toList ++ errType.toList)

      //   fb.switch(
      //     Sig(Nil, List(watpe.Int32)),
      //     Sig(Nil, List(watpe.RefType(false, genTypeID.WasmComponentResultStruct)))
      //   ) { () => // scrutinee (already on stack)
      //     fb += wa.LocalGet(vi.next()) // case index
      //   }(
      //     // Ok
      //     List(0) -> { () =>
      //       genNewScalaClass(fb, SpecialNames.WasmComponentOkClass, SpecialNames.AnyArgConstructorName) {
      //         val expect = Flatten.flattenType(okType)
      //         val newLocals = expect.map(t => fb.addLocal(NoOriginalName, t))
      //         genCoerceValues(fb, vi, flattened, expect)
      //         newLocals.reverse.foreach { l =>
      //           fb += wa.LocalSet(l)
      //         }
      //         genAdaptScalaJS(fb, ok, newLocals.toIterator)
      //       }
      //     },
      //     // Err
      //     List(1) -> { () =>
      //       genNewScalaClass(fb, SpecialNames.WasmComponentErrClass, SpecialNames.AnyArgConstructorName) {
      //         val expect = Flatten.flattenType(errType)
      //         val newLocals = expect.map(t => fb.addLocal(NoOriginalName, t))
      //         genCoerceValues(fb, vi, flattened, expect)
      //         newLocals.reverse.foreach { l =>
      //           fb += wa.LocalSet(l)
      //         }
      //         genAdaptScalaJS(fb, err, newLocals.toIterator)
      //       }
      //     },
      //   ) { () =>

      //   }

      case _ => ???
    }
  }

  /**
    *
    * @see [[https://github.com/WebAssembly/component-model/blob/main/design/mvp/CanonicalABI.md#flat-lowering]]
    */
  private def genCoerceValues(
    fb: FunctionBuilder,
    vi: Iterator[wanme.LocalID],
    types: List[watpe.Type],
    expect: List[watpe.Type]
  ): Unit = {
    types.zip(expect).map { case (have, want) =>
      fb += wa.LocalGet(vi.next())
      (have, want) match {
        case (watpe.Int32, watpe.Float32) =>
          fb += wa.F32ReinterpretI32
          // TODO: canonicalize NaN
        case (watpe.Int64, watpe.Int32) =>
          fb += wa.I32WrapI64
        case (watpe.Int64, watpe.Float32) =>
          fb += wa.I32WrapI64
          fb += wa.F32ReinterpretI32
          // TODO: canonicalize NaN
        case (watpe.Int64, watpe.Float64) =>
          fb += wa.F64ReinterpretI64
          // TODO: canonicalize NaN
        case _ => assert(have == want)
      }
    }
    // drop padding
    for (_ <- types.drop(expect.length))
      vi.next() // discard
  }

  private def genNewScalaClass(fb: FunctionBuilder, cls: ClassName, ctor: MethodName)(
      genCtorArgs: => Unit): Unit = {
    val instanceLocal = fb.addLocal(NoOriginalName, watpe.RefType(genTypeID.forClass(cls)))

    fb += wa.Call(genFunctionID.newDefault(cls))
    fb += wa.LocalTee(instanceLocal)
    genCtorArgs
    fb += wa.Call(genFunctionID.forMethod(MemberNamespace.Constructor, cls, ctor))
    fb += wa.LocalGet(instanceLocal)
  }
}