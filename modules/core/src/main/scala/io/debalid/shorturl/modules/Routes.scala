package io.debalid.shorturl.modules

import org.http4s.HttpRoutes
import io.debalid.shorturl.http.UrlsEndpoints
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import cats.effect.kernel.Async
import org.http4s.server.Router

trait Routes[F[_]] {
  def routes: HttpRoutes[F]
}

object Routes {

  def make[F[_]: Async](
      services: Services[F],
      programs: Programs[F]
  ): Routes[F] = new Routes[F] {

    val urls = UrlsEndpoints(services.urls, programs.shortUrl)

    val api     = urls.endpoints
    val docsApi = SwaggerInterpreter().fromServerEndpoints(api, "short-url", "v1")

    val interpteter = Http4sServerInterpreter[F]()
    override val routes: HttpRoutes[F] =
      Router("/" -> interpteter.toRoutes(docsApi ++ api))

  }

}
