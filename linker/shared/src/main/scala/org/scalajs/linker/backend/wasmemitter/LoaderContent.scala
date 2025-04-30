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

package org.scalajs.linker.backend.wasmemitter

import java.nio.charset.StandardCharsets

import scala.collection.mutable

import EmbeddedConstants._

/** Contents of the `__loader.js` file that we emit in every output. */
object LoaderContent {
  val bytesContent: Array[Byte] =
    stringContent.getBytes(StandardCharsets.UTF_8)

  val pureWasmBytesContent: Array[Byte] =
    pureWasmStringContent.getBytes(StandardCharsets.UTF_8)

  private def doLoadFunctionContent: String = {
    raw"""
async function doLoad(wasmFileURL, importsObj, options) {
  const resolvedURL = new URL(wasmFileURL, import.meta.url);
  if (resolvedURL.protocol === 'file:') {
    const { fileURLToPath } = await import("node:url");
    const { readFile } = await import("node:fs/promises");
    const wasmPath = fileURLToPath(resolvedURL);
    const body = await readFile(wasmPath);
    return WebAssembly.instantiate(body, importsObj, options);
  } else {
    return WebAssembly.instantiateStreaming(fetch(resolvedURL), importsObj, options);
  }
}
    """
  }

  private def stringContent: String = {
    raw"""
// This implementation follows no particular specification, but is the same as the JS backend.
// It happens to coincide with java.lang.Long.hashCode() for common values.
function bigintHashCode(x) {
  var res = 0;
  if (x < 0n)
    x = ~x;
  while (x !== 0n) {
    res ^= Number(BigInt.asIntN(32, x));
    x >>= 32n;
  }
  return res;
}

// JSSuperSelect support -- directly copied from the output of the JS backend
function resolveSuperRef(superClass, propName) {
  var getPrototypeOf = Object.getPrototyeOf;
  var getOwnPropertyDescriptor = Object.getOwnPropertyDescriptor;
  var superProto = superClass.prototype;
  while (superProto !== null) {
    var desc = getOwnPropertyDescriptor(superProto, propName);
    if (desc !== (void 0)) {
      return desc;
    }
    superProto = getPrototypeOf(superProto);
  }
}
function superSelect(superClass, self, propName) {
  var desc = resolveSuperRef(superClass, propName);
  if (desc !== (void 0)) {
    var getter = desc.get;
    return getter !== (void 0) ? getter.call(self) : getter.value;
  }
}
function superSelectSet(superClass, self, propName, value) {
  var desc = resolveSuperRef(superClass, propName);
  if (desc !== (void 0)) {
    var setter = desc.set;
    if (setter !== (void 0)) {
      setter.call(self, value);
      return;
    }
  }
  throw new TypeError("super has no setter '" + propName + "'.");
}

const scalaJSHelpers = {
  // JSTag
  JSTag: WebAssembly.JSTag,

  // BinaryOp.===
  is: Object.is,

  // undefined
  undef: void 0,
  isUndef: (x) => x === (void 0),

  // Constant boxes
  bFalse: false,
  bTrue: true,

  // Boxes (upcast) -- most are identity at the JS level but with different types in Wasm
  bIFallback: (x) => x,
  bF: (x) => x,
  bD: (x) => x,

  // Unboxes (downcast, null is converted to the zero of the type as part of ToWebAssemblyValue)
  uZ: (x) => x, // ToInt32 turns false into 0 and true into 1, so this is also an identity
  uIFallback: (x) => x,
  uF: (x) => x,
  uD: (x) => x,

  // Type tests
  tZ: (x) => typeof x === 'boolean',
  tI: (x) => typeof x === 'number' && Object.is(x | 0, x),
  tF: (x) => typeof x === 'number' && (Math.fround(x) === x || x !== x),
  tD: (x) => typeof x === 'number',

  // Strings
  jsValueToString: (x) => (x === void 0) ? "undefined" : x.toString(),
  jsValueToStringForConcat: (x) => "" + x,
  booleanToString: (b) => b ? "true" : "false",
  intToString: (i) => "" + i,
  longToString: (l) => "" + l, // l must be a bigint here
  doubleToString: (d) => "" + d,

  // Get the type of JS value of `x` in a single JS helper call, for the purpose of dispatch.
  jsValueType: (x) => {
    if (typeof x === 'number')
      return $JSValueTypeNumber;
    if (typeof x === 'string')
      return $JSValueTypeString;
    if (typeof x === 'boolean')
      return x | 0; // JSValueTypeFalse or JSValueTypeTrue
    if (typeof x === 'undefined')
      return $JSValueTypeUndefined;
    if (typeof x === 'bigint')
      return $JSValueTypeBigInt;
    if (typeof x === 'symbol')
      return $JSValueTypeSymbol;
    return $JSValueTypeOther;
  },

  // JS side of the `valueDescription` helper
  // TODO: only emit this when required by checked behaviors
  jsValueDescription: ((x) =>
    (typeof x === 'number')
      ? (Object.is(x, -0) ? "number(-0)" : ("number(" + x + ")"))
      : (typeof x)
  ),

  // Identity hash code
  bigintHashCode,
  symbolDescription: (x) => {
    var desc = x.description;
    return (desc === void 0) ? null : desc;
  },
  idHashCodeGet: (map, obj) => map.get(obj) | 0, // undefined becomes 0
  idHashCodeSet: (map, obj, value) => map.set(obj, value),

  // Some support functions for CoreWasmLib
  makeTypeError: (msg) => new TypeError(msg),

  // JS interop
  jsNewArray: () => [],
  jsNewObject: () => ({}),
  jsSelect: (o, p) => o[p],
  jsSelectSet: (o, p, v) => o[p] = v,
  jsNewNoArg: (constr) => new constr(),
  jsImportCall: (s) => import(s),
  jsImportMeta: () => import.meta,
  jsAwait: (WebAssembly.Suspending ? new WebAssembly.Suspending((x) => x) : ((x) => {
    /* This should not happen. We cannot get here without going through a
     * `WebAssembly.promising()` function. If that one succeeded,
     * `WebAssembly.Suspending` should also exist.
     * TODO Remove this fallback when JSPI support is widespread.
     */
    throw new Error("Unexpected js.await() without JSPI support.");
  })),
  jsDelete: (o, p) => { delete o[p]; },
  jsForInStart: function*(o) { for (var k in o) yield k; },
  jsForInNext: (g) => { var r = g.next(); return [r.value, r.done]; },
  jsIsTruthy: (x) => !!x,

  // Non-native JS class support
  newSymbol: Symbol,
  jsSuperSelect: superSelect,
  jsSuperSelectSet: superSelectSet,
}

const stringBuiltinPolyfills = {
  test: (x) => typeof x === 'string',
  fromCharCode: (c) => String.fromCharCode(c),
  fromCodePoint: (cp) => String.fromCodePoint(cp),
  charCodeAt: (s, i) => s.charCodeAt(i),
  codePointAt: (s, i) => s.codePointAt(i),
  length: (s) => s.length,
  concat: (a, b) => "" + a + b, // "" tells the JIT that this is *always* a string concat operation
  substring: (str, start, end) => str.substring(start >>> 0, end >>> 0),
  equals: (a, b) => a === b,
};

const stringConstantsPolyfills = new Proxy({}, {
  get(target, property, receiver) {
    return property;
  },
});

$doLoadFunctionContent

export function load(wasmFileURL, exportSetters, customJSHelpers, wtf16Strings) {
  const myScalaJSHelpers = {
    ...scalaJSHelpers,
    idHashCodeMap: new WeakMap()
  };
  const importsObj = {
    "$CoreHelpersModule": myScalaJSHelpers,
    "$ExportSettersModule": exportSetters,
    "$CustomHelpersModule": customJSHelpers,
    "$WTF16StringConstantsModule": wtf16Strings,
    "$JSStringBuiltinsModule": stringBuiltinPolyfills,
    "$UTF8StringConstantsModule": stringConstantsPolyfills,
  };
  const options = {
    builtins: ["js-string"],
    importedStringConstants: "$UTF8StringConstantsModule",
  };
  return doLoad(wasmFileURL, importsObj, options);
}
    """
  }

  private def pureWasmStringContent: String = {
    import org.scalajs.ir.OriginalName.NoOriginalName
    import org.scalajs.ir.OriginalName
    import org.scalajs.ir.Position.{NoPosition => NoPos}

    import org.scalajs.linker.backend.webassembly.{BinaryWriter, FunctionBuilder, ModuleBuilder}
    import org.scalajs.linker.backend.webassembly.Identitities._
    import org.scalajs.linker.backend.webassembly.Instructions._
    import org.scalajs.linker.backend.webassembly.Modules._
    import org.scalajs.linker.backend.webassembly.Types._

    import VarGen.genTypeID.i16Array

    lazy val arrayModuleBuilder: ModuleBuilder = new ModuleBuilder(new ModuleBuilder.FunctionTypeProvider {
      private val functionTypes = mutable.LinkedHashMap.empty[FunctionType, TypeID]

      def functionTypeToTypeID(sig: FunctionType): TypeID = {
        functionTypes.getOrElseUpdate(
          sig, {
            val typeID = VarGen.genTypeID.forFunction(functionTypes.size)
            arrayModuleBuilder.addRecType(typeID, NoOriginalName, sig)
            typeID
          }
        )
      }
    })

    arrayModuleBuilder.addRecType(i16Array,
        OriginalName(i16Array.toString()), ArrayType(FieldType(Int16, true)))

    object create extends FunctionID
    object length extends FunctionID
    object get extends FunctionID
    object set extends FunctionID

    def newFunctionBuilder(functionID: FunctionID): FunctionBuilder =
      new FunctionBuilder(arrayModuleBuilder, functionID, OriginalName(functionID.toString()), NoPos)

    locally {
      val fb = newFunctionBuilder(create)
      val lengthParam = fb.addParam("length", Int32)
      fb.setResultType(RefType(i16Array))

      fb += LocalGet(lengthParam)
      fb += ArrayNewDefault(i16Array)

      fb.buildAndAddToModule()
    }

    locally {
      val fb = newFunctionBuilder(length)
      val arrayParam = fb.addParam("array", RefType(i16Array))
      fb.setResultType(Int32)

      fb += LocalGet(arrayParam)
      fb += ArrayLen

      fb.buildAndAddToModule()
    }

    locally {
      val fb = newFunctionBuilder(get)
      val arrayParam = fb.addParam("array", RefType(i16Array))
      val indexParam = fb.addParam("index", Int32)
      fb.setResultType(Int32)

      fb += LocalGet(arrayParam)
      fb += LocalGet(indexParam)
      fb += ArrayGetU(i16Array)

      fb.buildAndAddToModule()
    }

    locally {
      val fb = newFunctionBuilder(set)
      val arrayParam = fb.addParam("array", RefType(i16Array))
      val indexParam = fb.addParam("index", Int32)
      val valueParam = fb.addParam("value", Int32)

      fb += LocalGet(arrayParam)
      fb += LocalGet(indexParam)
      fb += LocalGet(valueParam)
      fb += ArraySet(i16Array)

      fb.buildAndAddToModule()
    }

    arrayModuleBuilder.addExport(Export("create", ExportDesc.Func(create)))
    arrayModuleBuilder.addExport(Export("length", ExportDesc.Func(length)))
    arrayModuleBuilder.addExport(Export("get", ExportDesc.Func(get)))
    arrayModuleBuilder.addExport(Export("set", ExportDesc.Func(set)))

    val arrayModule = arrayModuleBuilder.build()

    val arrayModuleBytes = BinaryWriter.write(arrayModule, emitDebugInfo = false)
    val bytesArray = new Array[Byte](arrayModuleBytes.remaining())
    arrayModuleBytes.get(bytesArray)

    raw"""
const arrayModuleBytes = new Uint8Array([${bytesArray.map(_ & 0xff).mkString(",")}]);

const wasmArray = (await WebAssembly.instantiate(arrayModuleBytes)).instance.exports;

function wasmArrayToString(strArray) {
  var str = "";
  var len = wasmArray.length(strArray);
  for (var i = 0; i !== len; i++)
    str += String.fromCharCode(wasmArray.get(strArray, i));
  return str;
}

function stringToWasmArray(str) {
  var len = str.length;
  var strArray = wasmArray.create(len);
  for (var i = 0; i !== len; i++)
    wasmArray.set(strArray, i, str.charCodeAt(i));
  return strArray;
}

// Essential external features to test everything without the component model
const essentialExterns = {
  print: (strArray) => console.log(wasmArrayToString(strArray)),
  nanoTime: () => performance.now(),
  currentTimeMillis: () => Date.now(),
  random: () => Math.random(),
};

$doLoadFunctionContent

export function load(wasmFileURL) {
  const importsObj = {
    "$EssentialExternsModule": essentialExterns,
  }
  return doLoad(wasmFileURL, importsObj, {});
}
    """
  }
}
