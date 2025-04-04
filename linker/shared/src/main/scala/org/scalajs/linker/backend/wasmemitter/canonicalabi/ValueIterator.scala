package org.scalajs.linker.backend.wasmemitter.canonicalabi

import org.scalajs.ir.{WasmInterfaceTypes => wit}
import org.scalajs.ir.OriginalName.NoOriginalName

import org.scalajs.linker.backend.webassembly.{Types => watpe}
import org.scalajs.linker.backend.webassembly.{Instructions => wa}
import org.scalajs.linker.backend.webassembly.FunctionBuilder
import org.scalajs.linker.backend.webassembly.Identitities.{LocalID, MemoryID}

object ValueIterators {
  class ValueIterator private (
      fb: FunctionBuilder,
      underlying: List[(LocalID, watpe.Type)],
      init: Int
  ) {
    private var i = init

    def this(fb: FunctionBuilder, underlying: List[(LocalID, watpe.Type)]) = {
      this(fb, underlying, 0)
    }

    def hasNext(): Boolean = {
      i < underlying.length
    }

    def skip(t: watpe.Type): Unit = {
      i += 1
    }

    def next(t: watpe.Type): Unit = {
      val (id, tpe) = underlying(i)
      assert(t == tpe)
      fb += wa.LocalGet(id)
      i += 1
    }

    def copy(): ValueIterator = {
      new ValueIterator(
        fb,
        underlying,
        i
      )
    }
  }

  object ValueIterator {

    def apply(fb: FunctionBuilder, types: List[watpe.Type]): ValueIterator = {
      new ValueIterator(
        fb,
        // Pop and store the top stack values into local variables.
        // When iterating over the values, push them back while preserving the original order.
        types.reverse.map { t =>
          val id = fb.addLocal(NoOriginalName, t)
          fb += wa.LocalSet(id)
          (id, t)
        }.reverse,
        0
      )
    }
  }
}