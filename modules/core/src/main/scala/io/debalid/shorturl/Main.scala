package io.debalid.shorturl

import io.debalid.shorturl.config.ConfigLoader
import io.debalid.shorturl.http.UrlsRoute
import io.debalid.shorturl.modules.{ AppResources, Programs, Services }

import cats.effect.{ IO, IOApp }
import dev.profunktor.redis4cats.effect.Log.Stdout._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    ConfigLoader.make[IO].load.flatMap { cfg =>
      Logger[IO].info(s"Starting application with config: $cfg") >>
        AppResources
          .make[IO](cfg)
          .map { resources =>
            val services = Services.make[IO](cfg, resources)
            val programs = Programs.make[IO](services)
            cfg.http -> UrlsRoute(services.urls, programs.shortUrl)
          }
          .flatMap {
            case (cfg, httpApi) =>
              import org.http4s.implicits._

              EmberServerBuilder
                .default[IO]
                .withHost(cfg.host)
                .withPort(cfg.port)
                .withHttpApp(CORS(httpApi.routes).orNotFound)
                .build
          }
          .evalTap { server =>
            Logger[IO].info(s"Ready to accept HTTP requests on ${server.address}")
          }
          .useForever
    }

}
