MODULE := .2.12/target/scala-2.12/test-suite-wasi-fastopt/main.wasm

test: new
	wasmtime -W function-references,gc -C collector=null $(MODULE)

print:
	wasm-tools print $(MODULE)

clean:
	rm -f main.wasm

.PHONY: embed new test
