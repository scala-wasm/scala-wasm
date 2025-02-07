package org.scalajs.linker.backend.wasmemitter.canonicalabi

import org.scalajs.ir.OriginalName
import org.scalajs.ir.OriginalName.NoOriginalName
import org.scalajs.ir.Trees.{ComponentNativeMemberDef, MemberNamespace, WasmComponentExportDef}
import org.scalajs.ir.{WasmInterfaceTypes => wit}

import org.scalajs.linker.standard.LinkedClass

import org.scalajs.linker.backend.wasmemitter.VarGen.genFunctionID
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
  // import

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

    val params = member.signature.paramTypes.map { p =>
      val irType = p.toIRType()
      val localID = fb.addParam(
        NoOriginalName,
        transformParamType(p.toIRType())
      )
      (localID, p)
    }
    fb.setResultTypes(transformResultType(member.signature.resultType.toIRType()))

    val loweredFuncType = Flatten.lowerFlattenFuncType(member.signature)

    // adapt params to CanonicalABI
    loweredFuncType.paramsOffset match {
      case Some(offset) =>
        // TODO : put params onto linear memory
      case None =>
        params.map { case (localID, tpe) =>
          fb += wa.LocalGet(localID)
          ScalaJSToCABI.genAdaptCABI(fb, tpe)
        }
    }

    loweredFuncType.returnOffset match {
      case Some(_) =>
        val returnPtr = fb.addLocal(NoOriginalName, watpe.Int32)
        val ptr = fb.addLocal(NoOriginalName, watpe.Int32)
        val returnSize = wit.elemSize(member.signature.resultType)
        fb += wa.I32Const(returnSize)
        fb += wa.Call(genFunctionID.malloc)
        fb += wa.LocalTee(returnPtr)
        fb += wa.LocalTee(ptr)

        fb += wa.Call(importFunctionID)

        // Response back to Scala.js representation

        CABIToScalaJS.genLoadMemory(fb, member.signature.resultType, ptr)
        fb += wa.LocalGet(returnPtr)
        fb += wa.Call(genFunctionID.free)

      case None =>
        fb += wa.Call(importFunctionID)
        // Response back to Scala.js representation
        val resultTypes = Flatten.flattenType(member.signature.resultType)
        val vi = new ValueIterator(fb, resultTypes)
        CABIToScalaJS.genLoadStack(fb, member.signature.resultType, vi)
    }

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
    val fb = new FunctionBuilder(
      ctx.moduleBuilder,
      exportFunctionID,
      OriginalName(exportDef.exportName),
      pos,
    )

    val flatFuncType = Flatten.liftFlattenFuncType(exportDef.signature)

    fb.setResultTypes(flatFuncType.funcType.results)

    val returnOffsetOpt = flatFuncType.returnOffset match {
      case Some(offsetType) =>
        val returnOffsetID = fb.addLocal(NoOriginalName, watpe.Int32)
        // for storing the result data onto memory, after inner function call
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
        }.toIterator
        val iterator = new ValueIterator(fb, vi)
        exportDef.signature.paramTypes.foreach { paramTy =>
          CABIToScalaJS.genLoadStack(fb, paramTy, iterator)
        }
    }

    fb += wa.Call(internalFunctionID)
    // fb += wa.RefAsNonNull // TODO: NPE if null (based on semantcis)

    returnOffsetOpt match {
      case Some(offset) =>
        ScalaJSToCABI.genStoreMemory(fb, exportDef.signature.resultType)
        fb += wa.LocalGet(offset)
      case None =>
        // CABI expects to have a return value on stack
        ScalaJSToCABI.genAdaptCABI(fb, exportDef.signature.resultType)
    }

    fb.buildAndAddToModule()
    ctx.moduleBuilder.addExport(
      wamod.Export(
        exportDef.exportName,
        wamod.ExportDesc.Func(exportFunctionID)
      )
    )
  }
}