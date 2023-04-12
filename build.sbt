import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import Dependencies._

val scala2 = List("2.13.10")
val scala3 = List("3.2.2")

lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  organization := "com.softwaremill.sttp.openai",
  scalaVersion := "2.13.10"
)

lazy val rootProject = (projectMatrix in file("."))
  .settings(commonSettings: _*)
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3
  )
  .settings(publishArtifact := false, name := "sttp-openai")
  .settings(
    libraryDependencies ++= Seq(Libraries.uPickle) ++ Libraries.sttpClient ++ Seq(Libraries.scalaTest)
  )
