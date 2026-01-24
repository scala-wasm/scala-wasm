# Scala version: 2.12 or 2.13 (default: 2.13)
SCALA_VERSION ?= 2.13

run:
	wasmtime serve -W function-references,gc,exceptions .$(SCALA_VERSION)/target/scala-$(SCALA_VERSION)/echo-fastopt/main.wasm

clean:
	rm -f main.wasm main.wasm

.PHONY: run clean
