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

import org.scalajs.ir.{Names, OriginalName, Trees => js, WasmInterfaceTypes => wit}
import org.scalajs.ir.WitScope
import org.scalajs.ir.OriginalName.NoOriginalName

import org.scalajs.linker.standard.LinkedClass

import org.scalajs.linker.backend.wasmemitter.VarGen.{genFunctionID, genGlobalID}
import org.scalajs.linker.backend.wasmemitter.WasmContext
import org.scalajs.linker.backend.wasmemitter.TypeTransformer._
import org.scalajs.linker.backend.wasmemitter.FunctionEmitter

import org.scalajs.linker.backend.webassembly.{
  FunctionBuilder,
  Instructions => wa,
  Modules => wamod,
  Identitities => wanme,
  Types => watpe
}
import org.scalajs.linker.backend.webassembly.component.Flatten
import org.scalajs.linker.backend.wasmemitter.canonicalabi.ValueIterators.ValueIterator

object InteropEmitter {

  def genComponentNativeInterop(clazz: LinkedClass, member: js.WitNativeMemberDef)(
      implicit ctx: WasmContext
  ): Unit = {
    val importModuleName = WitScope.importModuleName(member.scope)
    val importFunctionID = genFunctionID.forComponentFunction(
        importModuleName, member.name)
    val importName = js.WitFunctionName.wasmName(member.name)
    val loweredFuncType = Flatten.lowerFlattenFuncType(member.signature)
    genComponentAdapterFunction(clazz, member, importFunctionID)

    val originalName = s"$importModuleName#$importName"

    if (ctx.registerComponentImport(importModuleName, importName)) {
      ctx.moduleBuilder.addImport(
        wamod.Import(
          importModuleName,
          importName,
          wamod.ImportDesc.Func(
            importFunctionID,
            OriginalName(originalName),
            ctx.moduleBuilder.functionTypeToTypeID(loweredFuncType.funcType)
          )
        )
      )
    }
  }

  private def genComponentAdapterFunction(clazz: LinkedClass, member: js.WitNativeMemberDef,
      importFunctionID: wanme.FunctionID)(
      implicit ctx: WasmContext): wanme.FunctionID = {
    val functionID = genFunctionID.forMethod(
      js.MemberNamespace.PublicStatic,
      clazz.className,
      member.method.name
    )
    val importName = js.WitFunctionName.wasmName(member.name)
    val fb = new FunctionBuilder(
      ctx.moduleBuilder,
      functionID,
      OriginalName(
          s"${WitScope.importModuleName(member.scope)}#$importName-adapter"),
      member.pos
    )

    val savedStackPointer = fb.addLocal("saved_sp", watpe.Int32)

    fb += wa.GlobalGet(genGlobalID.stackPointer)
    fb += wa.LocalSet(savedStackPointer)

    val params = member.signature.paramTypes.map { p =>
      val irType = p.toIRType()
      val localID = fb.addParam(
        NoOriginalName,
        transformParamType(p.toIRType())
      )
      (localID, p)
    }
    val resultType = member.signature.resultType match {
      case None        => Nil
      case Some(value) => List(value.toIRType())
    }
    fb.setResultTypes(resultType.flatMap(transformResultType(_)))

    val loweredFuncType = Flatten.lowerFlattenFuncType(member.signature)

    // adapt params to CanonicalABI
    loweredFuncType.paramsOffset match {
      case Some(offset) =>
        // TODO : put params onto linear memory
      case None =>
        params.foreach { case (localID, tpe) =>
          fb += wa.LocalGet(localID)
          ScalaJSToCABI.genStoreStack(fb, tpe)
        }
    }

    loweredFuncType.returnOffset match {
      case Some(_) =>
        val returnPtr = fb.addLocal(NoOriginalName, watpe.Int32)
        val returnSize = WitTypeOps.elemSize(member.signature.resultType)
        fb += wa.I32Const(returnSize)
        fb += wa.Call(genFunctionID.malloc)
        fb += wa.LocalTee(returnPtr)

        fb += wa.Call(importFunctionID)

        // Response back to Scala.js representation

        member.signature.resultType.foreach { resultType =>
          fb += wa.LocalGet(returnPtr)
          CABIToScalaJS.genLoadMemory(fb, resultType)
        }

      case None =>
        fb += wa.Call(importFunctionID)
        // Response back to Scala.js representation
        member.signature.resultType.foreach { resultType =>
          val resultTypes = Flatten.flattenType(resultType)
          val vi = ValueIterator(fb, resultTypes)
          CABIToScalaJS.genLoadStack(fb, resultType, vi)
        }
    }

    fb += wa.LocalGet(savedStackPointer)
    fb += wa.GlobalSet(genGlobalID.stackPointer)

    // Call the component function
    fb.buildAndAddToModule()
    functionID
  }

  // Export
  def genWitExportDef(owningClass: Names.ClassName, exportDef: js.WitExportDef)(
      implicit ctx: WasmContext): Unit = {
    implicit val pos = exportDef.pos

    // Core export naming follows the wasm-tools convention
    // (see https://github.com/WebAssembly/component-model/issues/422).
    val exportName = {
      val funcName = js.WitFunctionName.wasmName(exportDef.name)
      WitScope.exportName(exportDef.scope, funcName)
    }
    val method = exportDef.methodDef

    val methodFunctionID =
      genFunctionID.forMethod(method.flags.namespace, owningClass, method.name.name)

    // gen export adapter func
    val exportFunctionID = genFunctionID.forExport(exportName)
    val flatFuncType = Flatten.liftFlattenFuncType(exportDef.signature)
    locally {
      val fb = new FunctionBuilder(
        ctx.moduleBuilder,
        exportFunctionID,
        OriginalName(exportName),
        pos
      )
      fb.setResultTypes(flatFuncType.funcType.results)

      val savedStackPointer = fb.addLocal("saved_sp", watpe.Int32)

      // prepare clean up
      // save a stack pointer to restore in global, and restore the stack pointer
      // in post-return function
      fb += wa.GlobalGet(genGlobalID.stackPointer)
      fb += wa.GlobalSet(genGlobalID.savedStackPointer)

      val returnOffsetOpt = flatFuncType.returnOffset match {
        case Some(offsetType) =>
          val returnOffsetID = fb.addLocal("ret_addr", watpe.Int32)
          fb += wa.I32Const(WitTypeOps.elemSize(exportDef.signature.resultType))
          fb += wa.Call(genFunctionID.malloc)
          fb += wa.LocalTee(returnOffsetID)
          Some(returnOffsetID)
        case None => // do nothing
          None
      }

      // Load module instance for instance methods
      fb += wa.Call(genFunctionID.loadModule(owningClass))

      flatFuncType.paramsOffset match {
        case Some(paramsOffset) => ??? // TODO read params from linear memory
        case None               =>
          val vi = flatFuncType.stackParams.map { t =>
            (fb.addParam(NoOriginalName, t), t)
          }
          val iterator = new ValueIterator(fb, vi)
          exportDef.signature.paramTypes.foreach { paramTy =>
            CABIToScalaJS.genLoadStack(fb, paramTy, iterator)
          }
      }

      fb += wa.Call(methodFunctionID)

      returnOffsetOpt match {
        case Some(offset) =>
          exportDef.signature.resultType.foreach { resultType =>
            ScalaJSToCABI.genStoreMemory(fb, resultType)
          }
          fb += wa.LocalGet(offset)
        case None =>
          // CABI expects to have a return value on stack
          exportDef.signature.resultType.foreach { resultType =>
            ScalaJSToCABI.genStoreStack(fb, resultType)
          }
      }

      fb.buildAndAddToModule()
      ctx.moduleBuilder.addExport(
        wamod.Export(
          exportName,
          wamod.ExportDesc.Func(exportFunctionID)
        )
      )
    }

    // post return
    locally {
      // wasm-tools convention: prefixed with cabi_post_${func} will be post-return of $func
      // https://github.com/alexcrichton/wasm-tools/blob/da3e9730810c2e8782eb30db9a450aaa5fce881b/crates/wit-parser/src/resolve.rs#L2339-L2341
      // In future, we'd like to follow the spec https://github.com/WebAssembly/component-model/pull/378
      val postReturnName = "cabi_post_" + exportName
      val postReturnFunctionID = genFunctionID.forExport(postReturnName)

      val fb = new FunctionBuilder(
        ctx.moduleBuilder,
        postReturnFunctionID,
        OriginalName(postReturnName),
        pos
      )

      // > if a post-return is present, it has type (func (param flatten_functype({}, $ft, 'lift').results))
      // https://github.com/WebAssembly/component-model/blob/main/design/mvp/CanonicalABI.md#canon-lift
      for (r <- flatFuncType.funcType.results) {
        fb.addParam(NoOriginalName, r)
      }

      // must be 0 (if exported function calls an external function, which call our function)
      // fb += wa.GlobalGet(genGlobalID.savedStackPointer)
      // fb += wa.Call(genFunctionID.printlnInt)

      fb += wa.GlobalGet(genGlobalID.savedStackPointer)
      fb += wa.GlobalSet(genGlobalID.stackPointer)

      fb.buildAndAddToModule()
      ctx.moduleBuilder.addExport(
        wamod.Export(
          // wasm-tools convention: prefixed with cabi_post_${func} will be post-return of $func
          // https://github.com/alexcrichton/wasm-tools/blob/da3e9730810c2e8782eb30db9a450aaa5fce881b/crates/wit-parser/src/resolve.rs#L2339-L2341
          postReturnName,
          wamod.ExportDesc.Func(postReturnFunctionID)
        )
      )
    }
  }
}
