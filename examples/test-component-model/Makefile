# Scala version: 2.12 or 2.13 (default: 2.13)
SCALA_VERSION ?= 2.13

fetch:
	wkg wit fetch

build:
	cd rust-exports; cargo component build --target wasm32-wasip2 -r
	cd rust-run; cargo component build --target wasm32-wasip2 -r

compose:
	wac plug --plug rust-exports/target/wasm32-wasip1/release/rust_exports.wasm .$(SCALA_VERSION)/target/scala-$(SCALA_VERSION)/testing-module-for-component-model-fastopt/main.wasm -o scala.wasm
	wac plug --plug scala.wasm rust-run/target/wasm32-wasip1/release/rust_run.wasm -o out.wasm

run: compose
	wasmtime -W function-references,gc,exceptions out.wasm

clean:
	rm -f scala.wasm out.wasm

.PHONY: fetch build compose run clean
