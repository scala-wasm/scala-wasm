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

import org.scalajs.ir.{ClassKind, OriginalName, WitScope, WitTypeDef}
import org.scalajs.ir.Names.ClassName
import org.scalajs.ir.OriginalName.NoOriginalName
import org.scalajs.ir.Position
import org.scalajs.ir.ResourceOwnership

import org.scalajs.linker.interface.LinkingException
import org.scalajs.linker.standard.LinkedClass

import org.scalajs.linker.backend.wasmemitter.VarGen._
import org.scalajs.linker.backend.wasmemitter.WasmContext
import org.scalajs.linker.backend.wasmemitter.TypeTransformer.transformClassType

import org.scalajs.linker.backend.webassembly.{
  FunctionBuilder,
  Instructions => wa,
  Modules => wamod,
  Types => watpe
}

/** Exported WIT resources (`@WitResourceExport` classes).
 *
 *  The Canonical ABI represents a resource that this component defines by an
 *  `i32` "rep", which the component chooses. A Scala instance is a GC
 *  reference, so it cannot be a rep. Instead, a table maps reps to instances:
 *
 *  - lowering an `own<r>` (a result of an export, or a parameter of an
 *    import) puts the instance in a new slot and calls `[resource-new]r` with
 *    the slot's rep;
 *  - lifting a `borrow<r>` (a parameter of an export) gets the rep directly
 *    and reads the slot;
 *  - lifting an `own<r>` (a parameter of an export, or a result of an import)
 *    calls `[resource-rep]r` to read the slot, then `[resource-drop]r`: the
 *    handle is consumed, and the instance continues as an ordinary Scala
 *    value;
 *  - the destructor `[dtor]r`, which the host calls when the last handle is
 *    dropped, frees the slot.
 *
 *  So the component keeps an instance alive while a handle to it exists, and
 *  after that the garbage collector owns it. Reps are slot index + 1, and one
 *  table serves all exported resources. Free slots hold an `i31ref` with the
 *  index of the next free slot (or -1).
 */
object ExportedResources {
  private implicit val pos: Position = Position.NoPosition

  private val InitialCapacity = 16

  private object Intrinsic {
    val New = "resource-new"
    val Rep = "resource-rep"
    val Drop = "resource-drop"
  }

  /** Is `className` a `@WitResourceExport` class (not a resource import)? */
  def isExportedResource(className: ClassName)(implicit ctx: WasmContext): Boolean = {
    ctx.getClassInfoOption(className).exists { info =>
      info.kind != ClassKind.WasmComponentResourceClass &&
      info.witTypeDef.exists(_.isInstanceOf[WitTypeDef.Resource])
    }
  }

  private def resourceDef(className: ClassName)(
      implicit ctx: WasmContext): WitTypeDef.Resource = {
    ctx.getWitTypeDef(className) match {
      case r: WitTypeDef.Resource => r
      case other                  =>
        throw new AssertionError(s"$className is not a WIT resource: $other")
    }
  }

  /** Lifts the `i32` on the stack (a rep for `borrow`, a handle for `own`)
   *  to the instance of `className`.
   */
  def genLift(fb: FunctionBuilder, className: ClassName,
      ownership: ResourceOwnership)(implicit ctx: WasmContext): Unit = {
    val classType = transformClassType(className, nullable = true, exact = false)
    ownership match {
      case ResourceOwnership.Borrow =>
        fb += wa.Call(genFunctionID.exportedResourceGet)
        fb += wa.RefCast(classType)

      case ResourceOwnership.Own =>
        val handle = fb.addLocal(NoOriginalName, watpe.Int32)
        val instance = fb.addLocal(NoOriginalName, classType)
        fb += wa.LocalTee(handle)
        fb += wa.Call(genFunctionID.forExportedResourceIntrinsic(className, Intrinsic.Rep))
        fb += wa.Call(genFunctionID.exportedResourceGet)
        fb += wa.RefCast(classType)
        fb += wa.LocalSet(instance)
        fb += wa.LocalGet(handle)
        fb += wa.Call(genFunctionID.forExportedResourceIntrinsic(className, Intrinsic.Drop))
        fb += wa.LocalGet(instance)
    }
  }

  /** Lowers the instance of `className` on the stack to a new `own` handle. */
  def genLower(fb: FunctionBuilder, className: ClassName,
      ownership: ResourceOwnership)(implicit ctx: WasmContext): Unit = {
    ownership match {
      case ResourceOwnership.Own =>
        fb += wa.Call(genFunctionID.exportedResourceAlloc)
        fb += wa.Call(genFunctionID.forExportedResourceIntrinsic(className, Intrinsic.New))

      case ResourceOwnership.Borrow =>
        throw new LinkingException(
            s"Passing borrow<${resourceDef(className).name}> of an exported " +
            "resource to an imported function is not supported")
    }
  }

  /** Generates the rep table, and for each exported resource its intrinsic
   *  imports and its destructor export.
   */
  def genSupport(classes: List[LinkedClass])(implicit ctx: WasmContext): Unit = {
    val resources = classes.filter(c => isExportedResource(c.className))
    if (resources.nonEmpty) {
      genTable()
      resources.foreach(c => genResource(c.className))
    }
  }

  private def genResource(className: ClassName)(implicit ctx: WasmContext): Unit = {
    val resource = resourceDef(className)
    // wit-component: the intrinsics of an exported resource are imported from
    // `[export]<interface>`, and its destructor is exported as `<interface>#[dtor]<name>`.
    val module = "[export]" + WitScope.importModuleName(resource.scope)

    def addImport(intrinsic: String, params: List[watpe.Type],
        results: List[watpe.Type]): Unit = {
      val name = s"[$intrinsic]${resource.name}"
      if (ctx.registerComponentImport(module, name)) {
        val functionType = watpe.FunctionType(params, results)
        ctx.moduleBuilder.addImport(
          wamod.Import(
            module,
            name,
            wamod.ImportDesc.Func(
              genFunctionID.forExportedResourceIntrinsic(className, intrinsic),
              OriginalName(s"$module#$name"),
              ctx.moduleBuilder.functionTypeToTypeID(functionType)
            )
          )
        )
      }
    }

    addImport(Intrinsic.New, List(watpe.Int32), List(watpe.Int32))
    addImport(Intrinsic.Rep, List(watpe.Int32), List(watpe.Int32))
    addImport(Intrinsic.Drop, List(watpe.Int32), Nil)

    val dtorName = WitScope.exportName(resource.scope, s"[dtor]${resource.name}")
    val dtorID = genFunctionID.forExport(dtorName)
    val fb = new FunctionBuilder(ctx.moduleBuilder, dtorID, OriginalName(dtorName), pos)
    val rep = fb.addParam("rep", watpe.Int32)
    fb += wa.LocalGet(rep)
    fb += wa.Call(genFunctionID.exportedResourceFree)
    fb.buildAndAddToModule()
    ctx.moduleBuilder.addExport(wamod.Export(dtorName, wamod.ExportDesc.Func(dtorID)))
  }

  private def genTable()(implicit ctx: WasmContext): Unit = {
    val arrayType = genTypeID.anyArray
    val tableType = watpe.RefType.nullable(arrayType)

    def addGlobal(id: genGlobalID.ExportedResourceGlobalID, tpe: watpe.Type,
        init: wa.Instr): Unit = {
      ctx.addGlobal(
        wamod.Global(id, OriginalName(id.toString()), isMutable = true, tpe,
            wa.Expr(List(init)))
      )
    }
    addGlobal(genGlobalID.exportedResourceTable, tableType,
        wa.RefNull(watpe.HeapType.None))
    addGlobal(genGlobalID.exportedResourceUsed, watpe.Int32, wa.I32Const(0))
    addGlobal(genGlobalID.exportedResourceFreeHead, watpe.Int32, wa.I32Const(-1))

    // alloc(instance: anyref) -> rep
    locally {
      val fb = new FunctionBuilder(ctx.moduleBuilder, genFunctionID.exportedResourceAlloc,
          OriginalName(genFunctionID.exportedResourceAlloc.toString()), pos)
      val instance = fb.addParam("instance", watpe.RefType.anyref)
      fb.setResultType(watpe.Int32)
      val index = fb.addLocal("index", watpe.Int32)
      val oldTable = fb.addLocal("oldTable", tableType)
      val newTable = fb.addLocal("newTable", tableType)

      fb += wa.GlobalGet(genGlobalID.exportedResourceFreeHead)
      fb += wa.I32Const(0)
      fb += wa.I32GeS
      fb.ifThenElse() {
        // Reuse the first free slot; its i31ref holds the next free slot.
        fb += wa.GlobalGet(genGlobalID.exportedResourceFreeHead)
        fb += wa.LocalSet(index)
        fb += wa.GlobalGet(genGlobalID.exportedResourceTable)
        fb += wa.LocalGet(index)
        fb += wa.ArrayGet(arrayType)
        fb += wa.RefCast(watpe.RefType(watpe.HeapType.I31))
        fb += wa.I31GetS
        fb += wa.GlobalSet(genGlobalID.exportedResourceFreeHead)
      } {
        // Take a new slot; grow the table (x2) when it is full.
        fb += wa.GlobalGet(genGlobalID.exportedResourceUsed)
        fb += wa.LocalSet(index)
        fb += wa.GlobalGet(genGlobalID.exportedResourceTable)
        fb += wa.LocalTee(oldTable)
        fb += wa.RefIsNull
        fb.ifThenElse() {
          fb += wa.I32Const(InitialCapacity)
          fb += wa.ArrayNewDefault(arrayType)
          fb += wa.GlobalSet(genGlobalID.exportedResourceTable)
        } {
          fb += wa.LocalGet(index)
          fb += wa.LocalGet(oldTable)
          fb += wa.ArrayLen
          fb += wa.I32Eq
          fb.ifThen() {
            fb += wa.LocalGet(index)
            fb += wa.I32Const(2)
            fb += wa.I32Mul
            fb += wa.ArrayNewDefault(arrayType)
            fb += wa.LocalTee(newTable)
            fb += wa.I32Const(0)
            fb += wa.LocalGet(oldTable)
            fb += wa.I32Const(0)
            fb += wa.LocalGet(index)
            fb += wa.ArrayCopy(arrayType, arrayType)
            fb += wa.LocalGet(newTable)
            fb += wa.GlobalSet(genGlobalID.exportedResourceTable)
          }
        }
        fb += wa.LocalGet(index)
        fb += wa.I32Const(1)
        fb += wa.I32Add
        fb += wa.GlobalSet(genGlobalID.exportedResourceUsed)
      }

      fb += wa.GlobalGet(genGlobalID.exportedResourceTable)
      fb += wa.LocalGet(index)
      fb += wa.LocalGet(instance)
      fb += wa.ArraySet(arrayType)

      fb += wa.LocalGet(index)
      fb += wa.I32Const(1)
      fb += wa.I32Add
      fb.buildAndAddToModule()
    }

    // get(rep) -> anyref
    locally {
      val fb = new FunctionBuilder(ctx.moduleBuilder, genFunctionID.exportedResourceGet,
          OriginalName(genFunctionID.exportedResourceGet.toString()), pos)
      val rep = fb.addParam("rep", watpe.Int32)
      fb.setResultType(watpe.RefType.anyref)
      fb += wa.GlobalGet(genGlobalID.exportedResourceTable)
      fb += wa.LocalGet(rep)
      fb += wa.I32Const(1)
      fb += wa.I32Sub
      fb += wa.ArrayGet(arrayType)
      fb.buildAndAddToModule()
    }

    // free(rep)
    locally {
      val fb = new FunctionBuilder(ctx.moduleBuilder, genFunctionID.exportedResourceFree,
          OriginalName(genFunctionID.exportedResourceFree.toString()), pos)
      val rep = fb.addParam("rep", watpe.Int32)
      val index = fb.addLocal("index", watpe.Int32)
      fb += wa.LocalGet(rep)
      fb += wa.I32Const(1)
      fb += wa.I32Sub
      fb += wa.LocalSet(index)
      fb += wa.GlobalGet(genGlobalID.exportedResourceTable)
      fb += wa.LocalGet(index)
      fb += wa.GlobalGet(genGlobalID.exportedResourceFreeHead)
      fb += wa.RefI31
      fb += wa.ArraySet(arrayType)
      fb += wa.LocalGet(index)
      fb += wa.GlobalSet(genGlobalID.exportedResourceFreeHead)
      fb.buildAndAddToModule()
    }
  }
}
