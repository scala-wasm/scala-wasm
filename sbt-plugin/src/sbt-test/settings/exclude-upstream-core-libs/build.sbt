val check = taskKey[Unit]("Check which core libraries are on the classpath")

val UpstreamOrg = "org.scala-js"

def upstreamCoreLibs(report: UpdateReport, config: String): Set[String] = {
  report.configuration(ConfigRef(config)).toList.flatMap(_.allModules).collect {
    case m if m.organization == UpstreamOrg && m.name.startsWith("scalajs-") &&
        !m.name.startsWith("scalajs-dom") =>
      m.name
  }.toSet
}

def checkSetting(expected: Set[String]): Setting[_] = {
  check := {
    val report = update.value
    for (config <- List("compile", "test")) {
      val actual = upstreamCoreLibs(report, config)
      assert(actual == expected,
          s"[$config] upstream core libraries: expected $expected but got $actual")
    }
    val ours = report.allModules.filter { m =>
      m.organization == scalaJSOrganization && m.name.startsWith("scalajs-library_")
    }
    assert(ours.nonEmpty, s"$scalaJSOrganization scalajs-library is missing")
  }
}

// scalajs-dom is published with upstream Scala.js: it depends on the
// org.scala-js core libraries, which must be excluded.
lazy val scala213 = project
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "2.13.18",
    libraryDependencies += UpstreamOrg %%% "scalajs-dom" % "2.8.0",
    scalaJSUseMainModuleInitializer := true,
    checkSetting(Set.empty)
  )

// scala3-library_sjs1_3 depends on the upstream scalajs-library_2.13.
lazy val scala3 = project
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "3.3.4",
    libraryDependencies += UpstreamOrg %%% "scalajs-dom" % "2.8.0",
    scalaJSUseMainModuleInitializer := true,
    checkSetting(Set.empty)
  )

// On Scala 3.8+, the upstream scalajs-scalalib_2.13 is the Scala 3 standard
// library: it must stay.
lazy val scala38 = project
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "3.8.3",
    libraryDependencies += UpstreamOrg %%% "scalajs-dom" % "2.8.0",
    checkSetting(Set("scalajs-scalalib_2.13"))
  )

// An explicit dependency on an upstream core library disables the exclusions.
lazy val explicitUpstream = project
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "2.13.18",
    libraryDependencies += UpstreamOrg % "scalajs-library_2.13" % "1.18.2",
    check := {
      val actual = upstreamCoreLibs(update.value, "compile")
      assert(actual.contains("scalajs-library_2.13") && actual.contains("scalajs-javalib"),
          s"upstream core libraries were excluded: $actual")
    }
  )
