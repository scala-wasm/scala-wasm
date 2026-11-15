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

package org.scalajs.linker.backend.wasmemitter.canonicalabi

import org.scalajs.ir.{ClassKind, OriginalName, Position, ResourceOwnership, WitScope, WitTypeDef}
import org.scalajs.ir.Names.ClassName
import org.scalajs.ir.Trees.WitFunctionName
import org.scalajs.ir.{WasmInterfaceTypes => wit}

import org.scalajs.linker.backend.wasmemitter.VarGen._
import org.scalajs.linker.backend.wasmemitter.WasmContext

import org.scalajs.linker.backend.webassembly.{
  FunctionBuilder,
  Instructions => wa,
  Modules => wamod,
  Types => watpe
}

/** `borrow<r>` handles of imported resources that an export receives.
 *
 *  When an export lifts a `borrow<r>` of a resource that another component
 *  implements, the Canonical ABI gives it a borrow handle, and the export
 *  must drop that handle (`[resource-drop]r`) before it returns, or the call
 *  traps with "borrow handles still remain at the end of the call".
 *
 *  Lifting such a handle (at any depth: in a record, a list, ...) records it
 *  with the resource on a stack. An export whose parameters can contain one
 *  remembers the stack height before it lifts them, and drops every handle
 *  recorded above it after the method returns.
 */
object BorrowedHandles {
  private implicit val pos: Position = Position.NoPosition

  private val InitialCapacity = 16

  /** The `[resource-drop]` import of an imported resource. */
  private def dropFunctionID(resource: WitTypeDef.Resource) = {
    genFunctionID.forComponentFunction(WitScope.importModuleName(resource.scope),
        WitFunctionName.ResourceDrop(resource.name))
  }

  private def importedResource(className: ClassName)(
      implicit ctx: WasmContext): Option[WitTypeDef.Resource] = {
    ctx.getClassInfoOption(className).filter(_.kind == ClassKind.WasmComponentResourceClass)
      .flatMap(_.witTypeDef).collect { case r: WitTypeDef.Resource => r }
  }

  /** Records the handle on top of the stack, which stays there. */
  def genRecord(fb: FunctionBuilder, className: ClassName)(implicit ctx: WasmContext): Unit = {
    val kind = ctx.registerLiftedBorrowResource(className)
    val handle = fb.addLocal(OriginalName.NoOriginalName, watpe.Int32)
    fb += wa.LocalTee(handle)
    fb += wa.I32Const(kind)
    fb += wa.LocalGet(handle)
    fb += wa.Call(genFunctionID.recordLiftedBorrow)
  }

  /** Can a value of `tpe` contain a `borrow<r>` of an imported resource? */
  def mayContainImportedBorrow(tpe: wit.ValType)(implicit ctx: WasmContext): Boolean = {
    val visiting = scala.collection.mutable.Set.empty[ClassName]

    def loop(t: wit.ValType): Boolean = ctx.dealiasWit(t) match {
      case wit.ResourceType(className, ResourceOwnership.Borrow) =>
        importedResource(className).isDefined
      case wit.ListType(elem, _)        => loop(elem)
      case wit.OptionType(inner, _)     => loop(inner)
      case wit.ResultType(ok, err, _)   => ok.exists(loop) || err.exists(loop)
      case wit.TupleType(ts, _)         => ts.exists(loop)
      case wit.RecordTypeRef(className) =>
        visiting.add(className) && (ctx.getWitTypeDef(className) match {
          case r: WitTypeDef.Record => r.fields.exists(f => loop(f.tpe))
          case _                    => false
        })
      case tpe: wit.VariantTypeRef =>
        visiting.add(tpe.className) &&
        WitTypeOps.getVariantCases(tpe).exists(_.tpe.exists(loop))
      case _ =>
        false
    }

    loop(tpe)
  }

  /** Pushes the current height of the stack. */
  def genMark(fb: FunctionBuilder): Unit =
    fb += wa.GlobalGet(genGlobalID.liftedBorrowsCount)

  /** Drops the handles above the height in `mark`. */
  def genDropAbove(fb: FunctionBuilder, mark: wa.Instr): Unit = {
    fb += mark
    fb += wa.Call(genFunctionID.dropLiftedBorrows)
  }

  /** Generates the stack and its functions, if anything records handles. */
  def genSupport()(implicit ctx: WasmContext): Unit = {
    val resources = ctx.liftedBorrowResources
    if (resources.isEmpty)
      return

    val arrayType = genTypeID.i32Array
    val stackType = watpe.RefType.nullable(arrayType)

    ctx.addGlobal(
      wamod.Global(genGlobalID.liftedBorrows,
          OriginalName(genGlobalID.liftedBorrows.toString()), isMutable = true,
          stackType, wa.Expr(List(wa.RefNull(watpe.HeapType.None))))
    )
    ctx.addGlobal(
      wamod.Global(genGlobalID.liftedBorrowsCount,
          OriginalName(genGlobalID.liftedBorrowsCount.toString()), isMutable = true,
          watpe.Int32, wa.Expr(List(wa.I32Const(0))))
    )

    // The `[resource-drop]` imports (shared with `@WitResourceDrop` methods).
    for ((className, _) <- resources) {
      val resource = importedResource(className).getOrElse(
          throw new AssertionError(s"$className is not an imported WIT resource"))
      val module = WitScope.importModuleName(resource.scope)
      val name = WitFunctionName.wasmName(WitFunctionName.ResourceDrop(resource.name))
      if (ctx.registerComponentImport(module, name)) {
        ctx.moduleBuilder.addImport(
          wamod.Import(module, name,
              wamod.ImportDesc.Func(dropFunctionID(resource),
                  OriginalName(s"$module#$name"),
                  ctx.moduleBuilder.functionTypeToTypeID(
                      watpe.FunctionType(List(watpe.Int32), Nil))))
        )
      }
    }

    // record(kind, handle): push both, growing the stack (x2) when it is full.
    locally {
      val fb = new FunctionBuilder(ctx.moduleBuilder, genFunctionID.recordLiftedBorrow,
          OriginalName(genFunctionID.recordLiftedBorrow.toString()), pos)
      val kind = fb.addParam("kind", watpe.Int32)
      val handle = fb.addParam("handle", watpe.Int32)
      val count = fb.addLocal("count", watpe.Int32)
      val oldStack = fb.addLocal("oldStack", stackType)
      val newStack = fb.addLocal("newStack", stackType)

      fb += wa.GlobalGet(genGlobalID.liftedBorrowsCount)
      fb += wa.LocalSet(count)
      fb += wa.GlobalGet(genGlobalID.liftedBorrows)
      fb += wa.LocalTee(oldStack)
      fb += wa.RefIsNull
      fb.ifThenElse() {
        fb += wa.I32Const(InitialCapacity)
        fb += wa.ArrayNewDefault(arrayType)
        fb += wa.GlobalSet(genGlobalID.liftedBorrows)
      } {
        fb += wa.LocalGet(count)
        fb += wa.LocalGet(oldStack)
        fb += wa.ArrayLen
        fb += wa.I32Eq
        fb.ifThen() {
          fb += wa.LocalGet(count)
          fb += wa.I32Const(2)
          fb += wa.I32Mul
          fb += wa.ArrayNewDefault(arrayType)
          fb += wa.LocalTee(newStack)
          fb += wa.I32Const(0)
          fb += wa.LocalGet(oldStack)
          fb += wa.I32Const(0)
          fb += wa.LocalGet(count)
          fb += wa.ArrayCopy(arrayType, arrayType)
          fb += wa.LocalGet(newStack)
          fb += wa.GlobalSet(genGlobalID.liftedBorrows)
        }
      }

      fb += wa.GlobalGet(genGlobalID.liftedBorrows)
      fb += wa.LocalGet(count)
      fb += wa.LocalGet(kind)
      fb += wa.ArraySet(arrayType)
      fb += wa.GlobalGet(genGlobalID.liftedBorrows)
      fb += wa.LocalGet(count)
      fb += wa.I32Const(1)
      fb += wa.I32Add
      fb += wa.LocalGet(handle)
      fb += wa.ArraySet(arrayType)
      fb += wa.LocalGet(count)
      fb += wa.I32Const(2)
      fb += wa.I32Add
      fb += wa.GlobalSet(genGlobalID.liftedBorrowsCount)
      fb.buildAndAddToModule()
    }

    // dropAbove(mark): pop and drop every (kind, handle) above `mark`.
    locally {
      val fb = new FunctionBuilder(ctx.moduleBuilder, genFunctionID.dropLiftedBorrows,
          OriginalName(genFunctionID.dropLiftedBorrows.toString()), pos)
      val mark = fb.addParam("mark", watpe.Int32)
      val count = fb.addLocal("count", watpe.Int32)
      val kind = fb.addLocal("kind", watpe.Int32)
      val handle = fb.addLocal("handle", watpe.Int32)

      fb.block() { done =>
        fb.loop() { next =>
          fb += wa.GlobalGet(genGlobalID.liftedBorrowsCount)
          fb += wa.LocalGet(mark)
          fb += wa.I32LeS
          fb += wa.BrIf(done)

          fb += wa.GlobalGet(genGlobalID.liftedBorrowsCount)
          fb += wa.I32Const(2)
          fb += wa.I32Sub
          fb += wa.LocalTee(count)
          fb += wa.GlobalSet(genGlobalID.liftedBorrowsCount)
          fb += wa.GlobalGet(genGlobalID.liftedBorrows)
          fb += wa.LocalGet(count)
          fb += wa.ArrayGet(arrayType)
          fb += wa.LocalSet(kind)
          fb += wa.GlobalGet(genGlobalID.liftedBorrows)
          fb += wa.LocalGet(count)
          fb += wa.I32Const(1)
          fb += wa.I32Add
          fb += wa.ArrayGet(arrayType)
          fb += wa.LocalSet(handle)

          for ((className, k) <- resources) {
            fb += wa.LocalGet(kind)
            fb += wa.I32Const(k)
            fb += wa.I32Eq
            fb.ifThen() {
              fb += wa.LocalGet(handle)
              fb += wa.Call(dropFunctionID(importedResource(className).get))
            }
          }
          fb += wa.Br(next)
        }
      }
      fb.buildAndAddToModule()
    }
  }
}
