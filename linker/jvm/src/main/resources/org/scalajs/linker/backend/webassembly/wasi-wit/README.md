# WASI Bindings

Generated from WIT files in `linker/jvm/src/main/resources/org/scalajs/linker/backend/webassembly/wasi-wit/`

## Regenerating bindings

Install `wit-bindgen-scala`:

```bash
cargo install wit-bindgen-scala --version 0.1.0-rc.1
```

From project root:

```bash
wit-bindgen-scala linker/jvm/src/main/resources/org/scalajs/linker/backend/webassembly/wasi-wit \
  --world wasi-bindings \
  --out-dir library/src/main/scala \
  --base-package scala.scalajs
```
