val scalaJSVersion = sys.props("plugin.version")
val scalaJSOrganization = "io.github.scala-wasm"

addSbtPlugin("io.github.scala-wasm" % "sbt-scalajs" % scalaJSVersion)
addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0")

libraryDependencies += scalaJSOrganization %% "scalajs-linker" % scalaJSVersion
