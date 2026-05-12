val scalaJSVersion = sys.props("plugin.version")

addSbtPlugin("io.github.scala-wasm" % "sbt-scalajs" % scalaJSVersion)
