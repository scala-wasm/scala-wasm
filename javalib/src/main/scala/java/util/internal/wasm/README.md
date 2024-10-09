We place the WASI-related code in `java.util.internal.wasm` because we need to re-implement some Java libraries based on WASI, and we do not have access to `scalalib/library` from `javalib`.

## Alternatives
- Currently, we define WASI-related IRs as `WasmTransients`, and we are unable to transform `javalib` IR within `JavalibIRCleaner`. In the future, when these IRs are included into the core IR, we might consider defining these APIs in `library`, allowing `JavalibIRCleaner` to transform them into the corresponding IR.
- Or, we could define these classes/methods in `javalibintf`, making them accessible from `library` or `scalalib` (but not sure we can expose non-static classes?)
- Another option is to simply deduplicate them into `scalalib` as well.
