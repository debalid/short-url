package io.debalid.shorturl.http

import io.debalid.shorturl.domain.urls._
import io.debalid.shorturl.ext.refined._
import io.debalid.shorturl.programs.ShortUrl
import io.debalid.shorturl.services.Urls

import cats.effect.Concurrent
import cats.syntax.flatMap._
import cats.syntax.show._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final case class UrlsRoute[F[_]: Concurrent](urls: Urls[F], shortUrl: ShortUrl[F]) extends Http4sDsl[F] {

  private val urlsRoute = HttpRoutes.of[F] {

    case GET -> Root / Base63String(hash) =>
      urls.getFullUrl(UrlHash(hash)).flatMap {
        case Some(fullUrl) => MovedPermanently(fullUrl.value.value)
        case None          => NotFound()
      }

    // Not Base63{10}
    case GET -> Root / _ => NotFound()

    case req @ POST -> Root =>
      req.decode[String] {
        case UrlString(url) =>
          shortUrl.makeShortUrl(FullUrl(url)).flatMap { urlHash =>
            urls.link(urlHash, FullUrl(url)) >> Ok(urlHash.show)
          }
        case _ => NotFound()
      }
  }

  val routes: HttpRoutes[F] = Router(
    "/" -> urlsRoute
  )
}
