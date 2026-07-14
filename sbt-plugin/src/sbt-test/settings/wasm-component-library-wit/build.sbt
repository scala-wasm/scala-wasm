import sbtcompat.PluginCompat._
import org.scalajs.linker.interface.ESVersion
import org.scalajs.linker.interface.WasmComponentModuleInitializerExport
import org.scalajs.jsenv.wasmtime.WasmtimeEnv

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
          "-S", "cli,inherit-env"
      ))
      .withEnv(envVars.value)
    new WasmtimeEnv(config)
  }
)

def witLibrarySettings(world: String, packageName: String) = Def.settings(
  scalaJSWitDirectory := baseDirectory.value / "wit",
  scalaJSWitWorld := Some(world),
  scalaJSWitPackage := Some(packageName),
  scalaJSWitBindgenWith := Map.empty,
  Compile / scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.WasmComponent))
)

lazy val libA = project
  .in(file("lib-a"))
  .enablePlugins(ScalaJSPlugin)
  .settings(witLibrarySettings("random", "liba"))

lazy val libB = project
  .in(file("lib-b"))
  .enablePlugins(ScalaJSPlugin)
  .settings(witLibrarySettings("clock", "libb"))
  .settings(exportJars := true)

lazy val app = project
  .in(file("app"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(libA, libB)
  .settings(wasmtimeEnvSettings)
  .settings(
      scalaJSUseMainModuleInitializer := true,
      mainClass := Some("app.Main"),
      scalaJSWitDirectory := baseDirectory.value / "wit",
      scalaJSWitWorld := Some("app"),
      scalaJSWitPackage := Some("app"),
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
              .withAutoIncludeWasiImports(false)
              .withModuleInitializerExport(Some(WasmComponentModuleInitializerExport(
                  moduleName = "wasi:cli/run@0.2.0",
                  functionName = "run",
                  resultType =
                    WasmComponentModuleInitializerExport.ResultType.ResultUnitUnit)))
          }
      }
  )
