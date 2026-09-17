addSbtPlugin("io.github.scala-wasm" % "sbt-scalajs" % sys.props("plugin.version"))
addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0")

libraryDependencies +=
  "io.github.scala-wasm" %% "scalajs-env-wasmtime" % "0.1.0"
