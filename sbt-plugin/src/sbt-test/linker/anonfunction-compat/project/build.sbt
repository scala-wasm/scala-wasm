val scalaJSVersion = sys.props("plugin.version")
val scalaJSOrganization = "io.github.scala-wasm"

addSbtPlugin("io.github.scala-wasm" % "sbt-scalajs" % scalaJSVersion)

libraryDependencies += scalaJSOrganization %% "scalajs-linker" % scalaJSVersion
