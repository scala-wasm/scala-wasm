TEST_MODULE := ./examples/test-suite-wasi/.2.12/target/scala-2.12/test-suite-wasi-fastopt/main.wasm
HELLOWORLD_MODULE := examples/helloworld/.2.12/target/scala-2.12/helloworld-fastopt/main.wasm
TEST_SCRIPT := ./index.mjs

test:
	node --experimental-wasm-exnref $(TEST_SCRIPT) $(TEST_MODULE)

helloworld:
	node --experimental-wasm-exnref $(TEST_SCRIPT) $(HELLOWORLD_MODULE)

embed:
	wasm-tools component embed wit examples/helloworld/.2.12/target/scala-2.12/hello-world-scalajs-example-fastopt/main.wasm -o main.wasm -w socket --encoding utf16

new: embed
	wasm-tools component new main.wasm -o main.wasm

run: new
	wasmtime -W function-references,gc main.wasm
