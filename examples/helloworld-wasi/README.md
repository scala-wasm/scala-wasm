```sh
$ sbt helloworldWASI2_12/fastLinkJS
[info] Fast optimizing .../scala-wasm/examples/helloworld-wasi/.2.12/target/scala-2.12/helloworld-wasi-fastopt

# Download WIT dependencies
$ make fetch
# Build Wasm Component Model binary
$ make component

$ ls -lh main.wasm
... 124K ... main.wasm

$ time make run
wasmtime run -W function-references,gc -C collector=null main.wasm
Hello world!
make run  0.02s user 0.01s system 94% cpu 0.027 total
```