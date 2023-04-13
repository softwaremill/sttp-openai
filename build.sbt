import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import Dependencies._

val scala2 = List("2.13.10")
val scala3 = List("3.2.2")

def dependenciesFor(version: String)(deps: (Option[(Long, Long)] => ModuleID)*): Seq[ModuleID] =
  deps.map(_.apply(CrossVersion.partialVersion(version)))

lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  organization := "com.softwaremill.sttp.openai"
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publish / skip := true, name := "sttp-openai", scalaVersion := scala2.head)
  .aggregate(core.projectRefs: _*)

lazy val core = (projectMatrix in file("core"))
  .jvmPlatform(
    scalaVersions = scala2
  )
  .settings(
    libraryDependencies ++= Seq(Libraries.uPickle) ++ Libraries.sttpClient ++ Seq(Libraries.scalaTest)
  )
