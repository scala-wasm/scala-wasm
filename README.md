# scala-wasm

[![CI](https://github.com/scala-wasm/scala-wasm/actions/workflows/ci.yml/badge.svg)](https://github.com/scala-wasm/scala-wasm/actions/workflows/ci.yml)

This is a friendly fork of Scala.js, targeting stand-alone Wasm runtimes such as wasmtime, leveraging WASIp2 and Wasm Component Model.

## Getting Started

scala-wasm is available as `1.21.1-wasm.4`.

In `project/plugins.sbt`:

```scala
addSbtPlugin("io.github.scala-wasm" % "sbt-scalajs" % "1.21.1-wasm.4")
```

## Prerequisites
- [wasm-tools](https://github.com/bytecodealliance/wasm-tools)
- [wasmtime](https://github.com/bytecodealliance/wasmtime)
- [wit-bindgen-scala](https://github.com/scala-wasm/wit-bindgen-scala):

```sh
cargo install wit-bindgen-scala --version 0.1.0-rc.1
```

- (optional) [wkg](https://github.com/bytecodealliance/wasm-pkg-tools)
  - Required if you want to add Wasm Component Model dependencies.
- (optional) Required for running `test-component-model`:
  - [wac](https://github.com/bytecodealliance/wac)
  - [Setting up Rust](https://www.rust-lang.org/tools/install)
  - [cargo component](https://github.com/bytecodealliance/cargo-component)
    - `cargo component >= 0.18.0` may not work(?)

## WIT resources carried by libraries

`wit-bindgen-scala` and the Scala.js sbt plugin have an implicit contract for
carrying WIT through library dependencies. The plugin passes
`Compile / resourceManaged / META-INF / scalajs-wit` as `--wit-out-dir`, and
the generator writes resources using this layout:

```text
<wit-out-dir>/<input>/
├── world
├── world.wit
└── deps/
    └── <package>/
        └── package.wit
```

The `world` text file contains the name of the selected world.

## Examples
- [helloworld-wasi](./examples/helloworld-wasi/)
- [helloworld-component-model](./examples/helloworld-component-model/)
- [echo-server](./examples/echo-server/)
- [scala-wasm Spin templates](https://github.com/scala-wasm/scala-wasm-spin-templates)

## Test
### `test-suite`
```sh
sbt:Scala.js> set Global/enableWasmEverywhere := true
sbt:Scala.js> set scalaJSLinkerConfig in testSuite.v2_12 ~= (_.withModuleKind(ModuleKind.MinimalWasmModule))
sbt:Scala.js> testSuite2_12/test
```

Tests that do not link in pure Wasm are filtered out from `unmanagedSourceDirectories` and `sources`, see `testSuiteCommonSettings`.

### `test-component-model`
Test suites for Wasm Component Model based interop.

Build Wasm Component from Scala.
```sh
$ sbt
sbt:Scala.js> set Global/enableWasmEverywhere := true
sbt:Scala.js> testComponentModel2_12/fastLinkJS

$ cd examples/test-component-model
$ wasm-tools component embed wit .2.12/target/scala-2.12/testing-module-for-component-model-fastopt/main.wasm -o main.wasm -w scala --encoding utf16
$ wasm-tools component new main.wasm -o main.wasm
```

Build Wasm Components from Rust.
```sh
$ cd examples/test-component-model/rust-exports
$ cargo component build --target wasm32-wasip2 -r

$ cd examples/test-component-model/rust-run
$ cargo component build --target wasm32-wasip2 -r
```

Compose Wasm Components and run.
```sh
$ wac plug --plug rust-exports/target/wasm32-wasip1/release/rust_exports.wasm main.wasm -o scala.wasm
$ wac plug --plug scala.wasm rust-run/target/wasm32-wasip1/release/rust_run.wasm -o out.wasm

$ wasmtime -W function-references,gc out.wasm
```

---

What follows below is `README.md` of the upstream scala-js project

---

<p align="left">
  <a href="http://www.scala-js.org/">
    <img src="http://www.scala-js.org/assets/img/scala-js-logo.svg" height="128">
    <h1 align="left">Scala.js</h1>
  </a>
</p>

Chat: [#scala-js](https://discord.com/invite/scala) on Discord.

This is the repository for
[Scala.js, the Scala to JavaScript compiler](https://www.scala-js.org/).

* [Report an issue](https://github.com/scala-js/scala-js/issues)
* [Developer documentation](./DEVELOPING.md)
* [Contributing guidelines](./CONTRIBUTING.md)

## License

Scala.js is distributed under the
[Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
