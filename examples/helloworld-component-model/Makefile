# Scala version: 2.12 or 2.13 (default: 2.13)
SCALA_VERSION ?= 2.13

SCALA_MODULE := .$(SCALA_VERSION)/target/scala-$(SCALA_VERSION)/helloworld-component-model-fastopt/main.wasm
RUST_MODULE := rust-greeter/target/wasm32-wasip1/release/rust_greeter.wasm
OUT := out.wasm

fetch:
	wkg wit fetch

build-rust:
	cd rust-greeter; cargo component build -r

component:
	wac plug --plug $(RUST_MODULE) $(SCALA_MODULE) -o $(OUT)

run: component
	wasmtime -W function-references,gc,exceptions $(OUT)

clean:
	rm -f $(OUT)

.PHONY: fetch build-rust component run clean

