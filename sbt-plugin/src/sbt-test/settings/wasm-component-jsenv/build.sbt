import sbtcompat.PluginCompat._
import org.scalajs.linker.interface.{ESVersion, WasmComponentModuleInitializerExport}
import org.scalajs.jsenv.wasmtime.WasmtimeEnv

// TODO: currently we can't run tests using the wasmtime env,
// if the Compile scope has it's own WIT files

inThisBuild(Def.settings(
  version := scalaJSVersion,
  scalaVersion := "2.12.21",
))

val wasmtimeEnvSettings = Def.settings(
  jsEnv := Def.uncached {
    val config = WasmtimeEnv.Config()
      .withArgs(List(
          "run",
          "-W", "gc,function-references,exceptions",
          "-S", "cli,http,inherit-env,inherit-network,tcp"
      ))
      .withEnv(envVars.value)
    new WasmtimeEnv(config)
  }
)

lazy val runComponent = project
  .in(file("run-component"))
  .enablePlugins(ScalaJSPlugin)
  .settings(wasmtimeEnvSettings)
  .settings(
      Compile / scalaJSLinkerConfig := {
        (Compile / scalaJSLinkerConfig).value
          .withExperimentalUseWebAssembly(true)
          .withESFeatures(_.withESVersion(ESVersion.ES2022))
          .withModuleKind(ModuleKind.WasmComponent)
          .withWasmFeatures { features =>
            features
              .withWitDirectory(Some((baseDirectory.value / "wit").getAbsolutePath))
              .withWitWorld(Some("command"))
          }
      }
  )

lazy val testComponent = project
  .in(file("test-component"))
  .enablePlugins(ScalaJSPlugin, ScalaJSJUnitPlugin)
  .settings(wasmtimeEnvSettings)
  .settings(
      Test / scalaJSLinkerConfig := {
        (Test / scalaJSLinkerConfig).value
          .withExperimentalUseWebAssembly(true)
          .withESFeatures(_.withESVersion(ESVersion.ES2022))
          .withModuleKind(ModuleKind.WasmComponent)
          .withWasmFeatures { features =>
            features
              .withWitDirectory(Some((baseDirectory.value / "wit").getAbsolutePath))
              .withWitWorld(Some("test"))
              .withModuleInitializerExport(Some(WasmComponentModuleInitializerExport(
                  "wasi:cli/run@0.2.0",
                  "run",
                  WasmComponentModuleInitializerExport.ResultType.ResultUnitUnit)))
          }
      }
  )
