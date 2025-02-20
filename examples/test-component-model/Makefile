WASMTIME := /Users/tanishiking/ghq/github.com/bytecodealliance/wasmtime/target/release/wasmtime

fetch:
	wkg wit fetch

build:
	cd rust-exports; cargo component build --target wasm32-wasip2 -r
	cd rust-run; cargo component build --target wasm32-wasip2 -r

embed:
	wasm-tools component embed wit .2.12/target/scala-2.12/testing-module-for-component-model-fastopt/main.wasm -o main.wasm -w scala --encoding utf16

new: embed
	wasm-tools component new main.wasm -o main.wasm

compose: new
	wac plug --plug rust-exports/target/wasm32-wasip2/release/rust_exports.wasm main.wasm -o scala.wasm
	wac plug --plug scala.wasm rust-run/target/wasm32-wasip2/release/rust_run.wasm -o out.wasm

run: compose
	$(WASMTIME) -W function-references,gc -C collector=null out.wasm

clean:
	rm -f main.wasm out.wasm

.PHONY: fetch build embed new compose run
