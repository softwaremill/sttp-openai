import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import Dependencies._

lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  organization := "com.softwaremill.sttp-openapi",
  scalaVersion := "2.13.10"
)

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publishArtifact := false, name := "sttp-openapi")
  .settings(
    libraryDependencies ++= Libraries.jsoniter ++ Libraries.sttpClient ++ Seq(Libraries.scalaTest)
  )
