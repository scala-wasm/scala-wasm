addSbtPlugin("io.github.scala-wasm" % "sbt-scalajs" % sys.props("plugin.version"))

libraryDependencies += "io.github.scala-wasm" %% "scalajs-env-wasmtime" % "0.0.2"
