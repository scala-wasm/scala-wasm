package org.scalajs.linker.backend.wasmemitter.canonicalabi

import org.scalajs.ir.{WasmInterfaceTypes => wit}
import org.scalajs.ir.OriginalName.NoOriginalName

import org.scalajs.linker.backend.webassembly.{Types => watpe}
import org.scalajs.linker.backend.webassembly.{Instructions => wa}
import org.scalajs.linker.backend.webassembly.FunctionBuilder
import org.scalajs.linker.backend.webassembly.Identitities.{LocalID, MemoryID}

object ValueIterators {
  class ValueIterator(fb: FunctionBuilder, underlying: Iterator[(LocalID, watpe.Type)]) {
    def this(fb: FunctionBuilder, types: List[watpe.Type]) = {
      this(
        fb,
        types.reverse.map { t =>
          val id = fb.addLocal(NoOriginalName, t)
          fb += wa.LocalSet(id)
          (id, t)
        }.toIterator
      )
    }

    def next(t: watpe.Type): Unit = {
      val (id, tpe) = underlying.next()
      assert(t == tpe)
      fb += wa.LocalGet(id)
    }
  }
}