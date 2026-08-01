import sbtcompat.PluginCompat._
import org.scalajs.linker.interface.ESVersion
import org.scalajs.linker.interface.WasmComponentModuleInitializerExport
import org.scalajs.linker.interface.WasmComponentModuleInitializerExport._
import org.scalajs.ir.WitScope
import org.scalajs.jsenv.wasmtime.WasmtimeEnv

inThisBuild(Def.settings(
  version := scalaJSVersion,
  scalaVersion := "2.12.21",
))

// TODO: pre-generate each bindings
val wasmtimeEnvSettings = Def.settings(
  jsEnv := Def.uncached {
    new WasmtimeEnv(
      WasmtimeEnv.Config()
        .withArgs(List(
          "run",
          "-W", "gc,function-references,exceptions",
          "-S", "cli,inherit-env,inherit-network,tcp",
        ))
        .withEnv(envVars.value)
    )
  }
)

/* A library that implements `println` on top of `wasi:io` */
lazy val libIo = project
  .in(file("lib-io"))
  .enablePlugins(ScalaJSPlugin)

/* A library that reads the monotonic clock through `wasi:clocks`. */
lazy val libClock = project
  .in(file("lib-clock"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    exportJars := true,
  )

lazy val runComponent = project
  .in(file("run-component"))
  .enablePlugins(ScalaJSPlugin, ScalaJSJUnitPlugin)
  .dependsOn(libIo, libClock)
  .settings(wasmtimeEnvSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies +=
      "io.github.scala-wasm" % "scalajs-wasm-system-wasi02_sjs1_2.12" % scalaJSVersion,
    scalaJSLinkerConfig ~= {
      _.withESFeatures(_.withESVersion(ESVersion.ES2022).withUseWebAssembly(true))
        .withModuleKind(ModuleKind.WasmComponent)
        .withWasmFeatures(_.withModuleInitializerExport(
            Some(WasmComponentModuleInitializerExport(
              scope = WitScope.Interface("wasi", "cli", "run", Some("0.2.0")),
              functionName = "run",
              resultType = ResultType.ResultUnitUnit,
            ))))
    }
  )
