import org.scalajs.linker.interface.ModuleKind
import org.scalajs.jsenv.wasmtime.WasmtimeEnv

ThisBuild / scalaVersion := "2.12.21"

lazy val commonSettings = Def.settings(
  version := scalaJSVersion,
  scalaJSLinkerConfig ~= {
    _.withExperimentalUseWebAssembly(true)
      .withModuleKind(ModuleKind.WasmComponent)
  },
  jsEnv := new WasmtimeEnv(
    WasmtimeEnv.Config()
      .withArgs(List(
        "run",
        "-W", "gc,function-references,exceptions",
        "-S", "cli,inherit-env,inherit-network,tcp,http"))
  )
)

lazy val testApp = project
  .in(file("test-app"))
  .enablePlugins(ScalaJSPlugin, ScalaJSJUnitPlugin)
  .settings(
    commonSettings,
    scalaJSWitDirectory := baseDirectory.value / "wit",
    scalaJSWitWorld := Some("app"),
    scalaJSWitPackage := Some("example"),
    scalaJSUseMainModuleInitializer := true
  )
