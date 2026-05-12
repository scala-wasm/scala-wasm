val scalaJSVersion = sys.props("plugin.version")

addSbtPlugin("io.github.scala-wasm" % "sbt-scalajs" % scalaJSVersion)
addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0")
