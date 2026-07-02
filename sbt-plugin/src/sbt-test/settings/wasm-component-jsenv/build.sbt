import sbtcompat.PluginCompat._
import org.scalajs.linker.interface.ESVersion
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
      scalaJSWitDirectory := baseDirectory.value / "wit",
      scalaJSWitWorld := Some("command"),
      Compile / scalaJSLinkerConfig := {
        val witDir = scalaJSWitDirectory.value
        val witWorld = scalaJSWitWorld.value
        (Compile / scalaJSLinkerConfig).value
          .withExperimentalUseWebAssembly(true)
          .withESFeatures(_.withESVersion(ESVersion.ES2022))
          .withModuleKind(ModuleKind.WasmComponent)
          .withWasmFeatures { features =>
            features
              .withWitDirectory(Some(witDir.getAbsolutePath))
              .withWitWorld(witWorld)
          }
      }
  )

lazy val testComponent = project
  .in(file("test-component"))
  .enablePlugins(ScalaJSPlugin, ScalaJSJUnitPlugin)
  .settings(wasmtimeEnvSettings)
  .settings(
      Test / scalaJSLinkerConfig ~= {
        _.withExperimentalUseWebAssembly(true)
          .withESFeatures(_.withESVersion(ESVersion.ES2022))
          .withModuleKind(ModuleKind.WasmComponent)
      }
  )
