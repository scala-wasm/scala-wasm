val scalaJSVersion = sys.props("plugin.version")
val scalaJSOrganization = "io.github.scala-wasm"

addSbtPlugin("io.github.scala-wasm" % "sbt-scalajs" % scalaJSVersion)
addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0")

// see: anonfunction-compat/project/build.sbt
libraryDependencies += (scalaJSOrganization %% "scalajs-linker" % scalaJSVersion)
  .cross(CrossVersion.for3Use2_13)
  .exclude(scalaJSOrganization, "scalajs-linker-interface_2.13")
  .exclude(scalaJSOrganization, "scalajs-ir_2.13")
  .exclude(scalaJSOrganization, "scalajs-logging_2.13")
