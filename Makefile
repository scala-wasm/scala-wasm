TEST_MODULE := ./examples/test-suite-wasi/.2.12/target/scala-2.12/test-suite-wasi-fastopt/main.wasm
HELLOWORLD_MODULE := examples/helloworld/.2.12/target/scala-2.12/helloworld-fastopt/main.wasm
TEST_SCRIPT := ./index.mjs

test:
	node $(TEST_SCRIPT) $(TEST_MODULE)

helloworld:
	node $(TEST_SCRIPT) $(HELLOWORLD_MODULE)
