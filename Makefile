WASMTIME := /Users/tanishiking/ghq/github.com/bytecodealliance/wasmtime/target/release/wasmtime

embed:
	wasm-tools component embed wit examples/helloworld/.2.12/target/scala-2.12/hello-world-scalajs-example-fastopt/main.wasm -o main.wasm -w socket --encoding utf16

new: embed
	wasm-tools component new main.wasm -o main.wasm

compose: new
	wac plug --plug plugin/target/wasm32-wasip1/release/plugin.wasm main.wasm -o out.wasm

run-new: new
	$(WASMTIME) -W function-references,gc main.wasm

run: compose
	$(WASMTIME) -W function-references,gc out.wasm
