embed:
	wasm-tools component embed wit examples/helloworld/.2.12/target/scala-2.12/hello-world-scalajs-example-fastopt/main.wasm -o main.wasm -w socket --encoding utf16

new: embed
	wasm-tools component new main.wasm -o main.wasm

run: new
	wasmtime -W function-references,gc main.wasm
