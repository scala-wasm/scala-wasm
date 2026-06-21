```sh
$ sbt helloworldComponentModel2_12/fastLinkJS
[info] Fast optimizing .../scala-wasm/examples/helloworld-wasi/.2.12/target/scala-2.12/helloworld-wasi-fastopt

# Download WIT dependencies
$ make fetch
# Build Wasm Component Model from Rust project
$ make build-rust
# Compose Scala and Rust components
$ make component
# Run using wasmtime
$ make run
```