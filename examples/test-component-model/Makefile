# Scala version: 2.12 or 2.13 (default: 2.13)
SCALA_VERSION ?= 2.13

SCALA_MAIN = .$(SCALA_VERSION)/target/scala-$(SCALA_VERSION)/testing-module-for-component-model-fastopt/main.wasm

# `async func` types need the component-model-async feature on older wasmtime.
WASMTIME_FLAGS = -W function-references,gc,exceptions,component-model-async=y

build:
	cd rust-exports; cargo component build --target wasm32-wasip2 -r
	cd rust-run; cargo component build --target wasm32-wasip2 -r
	cd rust-async-run; cargo build --target wasm32-wasip2 -r

compose:
	wac plug --plug rust-exports/target/wasm32-wasip1/release/rust_exports.wasm $(SCALA_MAIN) -o scala.wasm
	wac plug --plug scala.wasm rust-run/target/wasm32-wasip1/release/rust_run.wasm -o out.wasm

# @WitAsync functions must be `async func` in the component type. Scala lifts
# them with the synchronous Canonical ABI: the host and rust-async-run (sync-ABI
# and async-ABI calls) must be able to call them.
check-async: compose
	wasm-tools component wit $(SCALA_MAIN) | grep -q 'export bare-add-one: async func(a: s32) -> s32;'
	wasm-tools component wit $(SCALA_MAIN) | grep -qE '^ +add-one: async func\(a: s32\) -> s32;'
	test "$$(wasmtime run $(WASMTIME_FLAGS) --invoke 'bare-add-one(41)' scala.wasm)" = "42"
	wac plug --plug scala.wasm rust-async-run/target/wasm32-wasip2/release/rust_async_run.wasm -o async-out.wasm
	test "$$(wasmtime run $(WASMTIME_FLAGS) --invoke 'run-async-checks()' async-out.wasm)" = "5"

run: compose check-async
	wasmtime $(WASMTIME_FLAGS) out.wasm

clean:
	rm -f scala.wasm out.wasm async-out.wasm

.PHONY: build compose check-async run clean
