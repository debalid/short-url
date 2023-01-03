import Dependencies._

ThisBuild / version := "1.1"
ThisBuild / scalaVersion := "2.13.6"
ThisBuild / organization := "io.debalid"

ThisBuild / evictionErrorLevel := Level.Warn
ThisBuild / scalafixDependencies += Libraries.organizeImports

resolvers += Resolver.sonatypeRepo("snapshots")

val scalafixCommonSettings = inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest))

lazy val root = (project in file("."))
  .settings(
    name := "short-url"
  )
  .aggregate(core, tests)

lazy val core = (project in file("modules/core"))
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    name := "short-url-core",
    scalacOptions ++= List("-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info"),
    scalafmtOnCompile := true,
    resolvers += Resolver.sonatypeRepo("snapshots"),
    Defaults.itSettings,
    scalafixCommonSettings,
    Docker / packageName := "short-url",
    dockerBaseImage := "openjdk:11-jre-slim-buster",
    dockerExposedPorts ++= Seq(8080),
    makeBatScripts := Seq(),
    dockerUpdateLatest := true,
    libraryDependencies ++= Seq(
      Compiler.semanticDB,
      Libraries.cats,
      Libraries.catsEffect,
      Libraries.newtype,
      Libraries.refinedCore,
      Libraries.refinedCats,
      Libraries.derevoCore,
      Libraries.derevoCats,
      Libraries.cirisCore,
      Libraries.cirisEnum,
      Libraries.cirisRefined,
      Libraries.http4sDsl,
      Libraries.http4sServer,
      Libraries.tapirCore,
      Libraries.tapirNewtype,
      Libraries.tapirRefiend,
      Libraries.tapirHttp4s,
      Libraries.tapirSwaggerUiBundle,
      Libraries.log4cats,
      Libraries.logback % Runtime,
      Libraries.redis4catsEffects,
      Libraries.redis4catsLog4cats
    )
  )

lazy val tests = (project in file("modules/tests"))
  .configs(IntegrationTest)
  .settings(
    name := "short-url-test-suite",
    scalacOptions ++= List("-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info"),
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    testFrameworks += new TestFramework("munit.Framework"),
    Defaults.itSettings,
    scalafixCommonSettings,
    libraryDependencies ++= Seq(
      Compiler.semanticDB,
      Libraries.http4sClient,
      Libraries.log4catsNoOp,
      Libraries.refinedScalacheck,
      Libraries.weaverCats,
      Libraries.weaverScalaCheck,
      Libraries.munit,
      Libraries.munitScalaCheck,
      Libraries.munitCatsEffect,
      Libraries.munitScalaCheckCats,
      Libraries.testContainers
    )
  )
  .dependsOn(core)

addCommandAlias("runLinter", ";scalafixAll --rules OrganizeImports")
