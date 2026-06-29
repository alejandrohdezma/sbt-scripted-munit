ThisBuild / scalaVersion                  := _root_.scalafix.sbt.BuildInfo.scala212
ThisBuild / crossScalaVersions            := Seq(scalaVersion.value, "3.8.4")
ThisBuild / organization                  := "com.alejandrohdezma"
ThisBuild / pluginCrossBuild / sbtVersion := scalaVersion.value.on(2)("1.12.12").getOrElse("2.0.0")
ThisBuild / versionPolicyIntention        := Compatibility.None

addCommandAlias("ci-test", "fix --check; +versionPolicyCheck; mdoc; +scripted")
addCommandAlias("ci-docs", "github; mdoc; headerCreateAll")
addCommandAlias("ci-publish", "versionCheck; github; ci-release")

lazy val documentation = project
  .enablePlugins(MdocPlugin)
  .settings(mdocOut := file("."))

lazy val `sbt-scripted-munit` = module
  .settings(libraryDependencies += "org.scalameta" %% "munit" % "1.3.3")
  .enablePlugins(SbtPlugin)
  .settings(scriptedBufferLog := false)
  .settings(scriptedLaunchOpts += s"-Dplugin.version=${version.value}")
