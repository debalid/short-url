import sbt._

object Dependencies {

  object Versions {
    val cats       = "2.9.0"
    val catsEffect = "3.4.3"
    val derevo     = "0.13.0"
    val http4s     = "0.23.16"
    val ciris      = "3.0.0"
    val log4cats   = "2.5.0"
    val redis4cats = "1.3.0"

    val newtype = "0.4.4"
    val refined = "0.10.1"

    val tapir = "1.2.4"

    val logback         = "1.4.5"
    val organizeImports = "0.6.0"
    val semanticDB      = "4.7.0"

    val weaver              = "0.8.1"
    val munit               = "1.0.0-M7"
    val munitScalaCheckCats = "2.0.0-M2"
    val munitCatsEffect     = "1.0.7"
    val testContainers      = "0.40.12"

  }

  object Libraries {

    val cats       = "org.typelevel" %% "cats-core"   % Versions.cats
    val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect

    def http4s(artifact: String): ModuleID = "org.http4s" %% s"http4s-$artifact" % Versions.http4s
    val http4sDsl                          = http4s("dsl")
    val http4sServer                       = http4s("ember-server")
    val http4sClient                       = http4s("client")

    def ciris(artifact: String): ModuleID = "is.cir" %% artifact % Versions.ciris
    val cirisCore                         = ciris("ciris")
    val cirisEnum                         = ciris("ciris-enumeratum")
    val cirisRefined                      = ciris("ciris-refined")

    def derevo(artifact: String): ModuleID = "tf.tofu" %% s"derevo-$artifact" % Versions.derevo
    val derevoCore                         = derevo("core")
    val derevoCats                         = derevo("cats")

    val newtype = "io.estatico" %% "newtype" % Versions.newtype

    def refined(artifact: String): ModuleID = "eu.timepit" %% artifact % Versions.refined
    val refinedCore                         = refined("refined")
    val refinedCats                         = refined("refined-cats")
    val refinedScalacheck                   = refined("refined-scalacheck")

    def tapir(artifact: String): ModuleID = "com.softwaremill.sttp.tapir" %% s"tapir-$artifact" % Versions.tapir
    val tapirCore                         = tapir("core")
    val tapirHttp4s                       = tapir("http4s-server")
    val tapirNewtype                      = tapir("newtype")
    val tapirRefiend                      = tapir("refined")
    val tapirSwaggerUiBundle              = tapir("swagger-ui-bundle")

    val log4cats     = "org.typelevel"  %% "log4cats-slf4j" % Versions.log4cats
    val log4catsNoOp = "org.typelevel"  %% "log4cats-noop"  % Versions.log4cats
    val logback      = "ch.qos.logback" % "logback-classic" % Versions.logback

    def redis4cats(artifact: String): ModuleID = "dev.profunktor" %% s"redis4cats-$artifact" % Versions.redis4cats
    val redis4catsEffects                      = redis4cats("effects")
    val redis4catsLog4cats                     = redis4cats("log4cats")

    // For Unit tests
    val weaverCats       = "com.disneystreaming" %% "weaver-cats"       % Versions.weaver
    val weaverScalaCheck = "com.disneystreaming" %% "weaver-scalacheck" % Versions.weaver

    // For Integration tests (unfortunately, weaver does not support TestContainer yet, and I have an idea for a pet project)
    val munit               = "org.scalameta" %% "munit"                      % Versions.munit
    val munitScalaCheck     = "org.scalameta" %% "munit-scalacheck"           % Versions.munit
    val munitCatsEffect     = "org.typelevel" %% "munit-cats-effect-3"        % Versions.munitCatsEffect
    val munitScalaCheckCats = "org.typelevel" %% "scalacheck-effect-munit"    % Versions.munitScalaCheckCats
    val testContainers      = "com.dimafeng"  %% "testcontainers-scala-munit" % Versions.testContainers

    // For Scalafix
    val organizeImports = "com.github.liancheng" %% "organize-imports" % Versions.organizeImports
  }

  object Compiler {

    val semanticDB = compilerPlugin(
      "org.scalameta" % "semanticdb-scalac" % Versions.semanticDB cross CrossVersion.full
    )
  }

}
