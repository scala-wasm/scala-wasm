package org.scalajs.linker.backend.wasmemitter.canonicalabi

import org.scalajs.ir.OriginalName
import org.scalajs.ir.OriginalName.NoOriginalName
import org.scalajs.ir.Trees.{ComponentNativeMemberDef, MemberNamespace, WasmComponentExportDef}

import org.scalajs.linker.standard.LinkedClass

import org.scalajs.linker.backend.wasmemitter.VarGen.genFunctionID
import org.scalajs.linker.backend.wasmemitter.WasmContext
import org.scalajs.linker.backend.wasmemitter.TypeTransformer._
import org.scalajs.linker.backend.wasmemitter.FunctionEmitter

import org.scalajs.linker.backend.webassembly.FunctionBuilder
import org.scalajs.linker.backend.webassembly.{Instructions => wa}
import org.scalajs.linker.backend.webassembly.{Modules => wamod}
import org.scalajs.linker.backend.webassembly.{Identitities => wanme}
import org.scalajs.linker.backend.webassembly.component.Flatten

object InteropEmitter {
  // import

  def genComponentNativeInterop(clazz: LinkedClass, member: ComponentNativeMemberDef)(
    implicit ctx: WasmContext
  ): Unit = {
    val importFunctionID = genFunctionID.forComponentFunction(member.importModule, member.importName)
    val coreWasmFunctionType = Flatten.lowerFlattenFuncType(member.signature)
    ctx.moduleBuilder.addImport(
      wamod.Import(
        member.importModule,
        member.importName,
        wamod.ImportDesc.Func(
          importFunctionID,
          OriginalName(s"${member.importModule}#${member.importName}"),
          ctx.moduleBuilder.functionTypeToTypeID(coreWasmFunctionType)
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

    val paramsViaMemory = member.signature.paramTypes.flatMap(Flatten.flattenType).length > Flatten.MaxFlatParams
    val returnsViaMemory = Flatten.flattenType(member.signature.resultType).length > Flatten.MaxFlatResults

    // adapt params to CanonicalABI
    if (paramsViaMemory) {
      // TODO : put params onto linear memory
    } else {
      params.map { case (localID, tpe) =>
        fb += wa.LocalGet(localID)
        ScalaJSToCABI.genAdaptCABI(fb, tpe)
      }
    }
    if (returnsViaMemory) {
      // TODO : allocate memoery and pass the ofset
    }

    // Call the component function
    fb += wa.Call(importFunctionID)

    // Response back to Scala.js representation
    if (returnsViaMemory) {
      // TODO
    } else {
      val resultTypes = Flatten.flattenType(member.signature.resultType)
      val vi = resultTypes.map { t =>
        val id = fb.addLocal(NoOriginalName, t)
        fb += wa.LocalSet(id)
        id
      }.toIterator
      CABIToScalaJS.genAdaptScalaJS(fb, member.signature.resultType, vi)
    }
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

    val flatParamTypes = exportDef.signature.paramTypes.flatMap(Flatten.flattenType)
    val flatResultTypes = Flatten.flattenType(exportDef.signature.resultType)
    val paramsViaMemory = flatParamTypes.length > Flatten.MaxFlatParams
    val returnsViaMemory = flatResultTypes.length > Flatten.MaxFlatResults

    if (paramsViaMemory) {
      // TODO : put params onto linear memory
      ???
    } else {
      fb.setResultTypes(flatResultTypes)
      val vi = flatParamTypes.map { t =>
        fb.addParam(NoOriginalName, t)
      }.toIterator
      exportDef.signature.paramTypes.foreach { paramTy =>
        CABIToScalaJS.genAdaptScalaJS(fb, paramTy, vi)
      }
    }
    if (returnsViaMemory) {
      // TODO : allocate memoery and pass the ofset
    }

    fb += wa.Call(internalFunctionID)
    fb += wa.RefAsNonNull // TODO: NPE if null (based on semantcis)

    // export adapter function
    // Response back to Scala.js representation
    if (returnsViaMemory) {
      // TODO: read from return pointer
    } else {
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