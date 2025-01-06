```sh
sbt:Scala.js> set Global/enableWasmEverywhere := true; helloworld2_12/fastLinkJS

$ make embed
wasm-tools component embed wit examples/helloworld/.2.12/target/scala-2.12/hello-world-scalajs-example-fastopt/main.wasm -o main.wasm --encoding utf16

$ make new
wasm-tools component new main.wasm -o main.wasm

$ wac plug --plug tanishiking:ferris-impl@0.0.2 main.wasm -o out.wasm
```

https://github.com/tanishiking/kitchensink/tree/main/ferris-says-component
