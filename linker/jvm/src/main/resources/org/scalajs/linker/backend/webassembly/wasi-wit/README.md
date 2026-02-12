# WASI Bindings

Generated from WIT files in `linker/jvm/src/main/resources/org/scalajs/linker/backend/webassembly/wasi-wit/`

## Regenerating bindings

Install `scala-wasm` supported `wit-bindgen` from https://github.com/scala-wasm/wit-bindgen

From project root:

```bash
wit-bindgen scala linker/jvm/src/main/resources/org/scalajs/linker/backend/webassembly/wasi-wit \
  --world wasi-bindings \
  --out-dir library/src/main/scala \
  --base-package scala.scalajs
```
