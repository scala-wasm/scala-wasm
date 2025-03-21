WASMTIME := /Users/tanishiking/ghq/github.com/bytecodealliance/wasmtime/target/release/wasmtime
MODULE := .2.12/target/scala-2.12/test-suite-wasi-fastopt/main.wasm

test: new
	$(WASMTIME) -W function-references,gc -C collector=null $(MODULE)

test-node: new
	node index.mjs $(MODULE)

print:
	wasm-tools print $(MODULE)

clean:
	rm -f main.wasm

.PHONY: embed new test
