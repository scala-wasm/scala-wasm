package org.scalajs.linker.backend.wasmemitter.canonicalabi

import org.scalajs.ir.OriginalName
import org.scalajs.ir.OriginalName.NoOriginalName
import org.scalajs.ir.Trees.{ComponentNativeMemberDef, MemberNamespace, WasmComponentExportDef}
import org.scalajs.ir.{WasmInterfaceTypes => wit}

import org.scalajs.linker.standard.LinkedClass

import org.scalajs.linker.backend.wasmemitter.VarGen.{genFunctionID, genGlobalID}
import org.scalajs.linker.backend.wasmemitter.WasmContext
import org.scalajs.linker.backend.wasmemitter.TypeTransformer._
import org.scalajs.linker.backend.wasmemitter.FunctionEmitter

import org.scalajs.linker.backend.webassembly.FunctionBuilder
import org.scalajs.linker.backend.webassembly.{Instructions => wa}
import org.scalajs.linker.backend.webassembly.{Modules => wamod}
import org.scalajs.linker.backend.webassembly.{Identitities => wanme}
import org.scalajs.linker.backend.webassembly.{Types => watpe}
import org.scalajs.linker.backend.webassembly.component.Flatten
import org.scalajs.linker.backend.wasmemitter.canonicalabi.ValueIterators.ValueIterator


object InteropEmitter {


  def genComponentNativeInterop(clazz: LinkedClass, member: ComponentNativeMemberDef)(
    implicit ctx: WasmContext
  ): Unit = {
    val importFunctionID = genFunctionID.forComponentFunction(member.importModule, member.importName)
    val loweredFuncType = Flatten.lowerFlattenFuncType(member.signature)
    ctx.moduleBuilder.addImport(
      wamod.Import(
        member.importModule,
        member.importName,
        wamod.ImportDesc.Func(
          importFunctionID,
          OriginalName(s"${member.importModule}#${member.importName}"),
          ctx.moduleBuilder.functionTypeToTypeID(loweredFuncType.funcType)
        )
      )
    )
    genComponentAdapterFunction(clazz, member, importFunctionID)
  }

  private def genComponentAdapterFunction(clazz: LinkedClass, member: ComponentNativeMemberDef,
      importFunctionID: wanme.FunctionID)(
      implicit ctx: WasmContext): wanme.FunctionID = {
    val functionID = genFunctionID.forMethod(
      MemberNamespace.PublicStatic,
      clazz.className,
      member.name.name
    )
    val fb = new FunctionBuilder(
      ctx.moduleBuilder,
      functionID,
      OriginalName(s"${member.importModule}#${member.importName}-adapter"),
      member.pos,
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
        case None => Nil
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
        val returnSize = wit.elemSize(member.signature.resultType)
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
  def genWasmComponentExportDef(exportDef: WasmComponentExportDef)(
      implicit ctx: WasmContext): Unit = {
    implicit val pos = exportDef.pos

    val method = exportDef.methodDef
    val internalMethodName = exportDef.exportName + "$internal"
    val internalFunctionID = genFunctionID.forExport(internalMethodName)
    FunctionEmitter.emitFunction(
      internalFunctionID,
      OriginalName(internalMethodName),
      enclosingClassName = None,
      captureParamDefs = None,
      receiverType = None,
      method.args,
      None,
      method.body.get,
      method.resultType
    )

    // gen export adapter func
    val exportFunctionID = genFunctionID.forExport(exportDef.exportName)
    val flatFuncType = Flatten.liftFlattenFuncType(exportDef.signature)


    locally {
      val fb = new FunctionBuilder(
        ctx.moduleBuilder,
        exportFunctionID,
        OriginalName(exportDef.exportName),
        pos,
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
          fb += wa.I32Const(wit.elemSize(exportDef.signature.resultType))
          fb += wa.Call(genFunctionID.malloc)
          fb += wa.LocalTee(returnOffsetID)
          Some(returnOffsetID)
        case None => // do nothing
          None
      }

      flatFuncType.paramsOffset match {
        case Some(paramsOffset) => ??? // TODO read params from linear memory
        case None =>
          val vi = flatFuncType.stackParams.map { t =>
             (fb.addParam(NoOriginalName, t), t)
          }
          val iterator = new ValueIterator(fb, vi)
          exportDef.signature.paramTypes.foreach { paramTy =>
            CABIToScalaJS.genLoadStack(fb, paramTy, iterator)
          }
      }

      fb += wa.Call(internalFunctionID)

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
          exportDef.exportName,
          wamod.ExportDesc.Func(exportFunctionID)
        )
      )
    }

    // post return
    locally {
      // wasm-tools convention: prefixed with cabi_post_${func} will be post-return of $func
      // https://github.com/alexcrichton/wasm-tools/blob/da3e9730810c2e8782eb30db9a450aaa5fce881b/crates/wit-parser/src/resolve.rs#L2339-L2341
      // In future, we'd like to follow the spec https://github.com/WebAssembly/component-model/pull/378
      val postReturnName = "cabi_post_" + exportDef.exportName
      val postReturnFunctionID = genFunctionID.forExport(postReturnName)

      val fb = new FunctionBuilder(
        ctx.moduleBuilder,
        postReturnFunctionID,
        OriginalName(postReturnName),
        pos,
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