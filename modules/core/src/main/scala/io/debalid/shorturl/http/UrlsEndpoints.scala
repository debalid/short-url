package io.debalid.shorturl.http

import io.debalid.shorturl.domain.urls._
import io.debalid.shorturl.programs.ShortUrl
import io.debalid.shorturl.services.Urls

import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.codec.newtype._
import sttp.tapir.codec.refined._

final case class UrlsEndpoints[F[_]](urls: Urls[F], shortUrl: ShortUrl[F]) {

  val getFullUrlByHashEndpoint =
    endpoint.get
      .in(path[UrlHash]("urlHash").description("Url hash"))
      .out(header[FullUrl]("Location") and statusCode(StatusCode.MovedPermanently).description("Redirects to full url"))
      .errorOut(statusCode(StatusCode.NotFound).description("Doesn't have a linked URL for this hash"))
      .description("Get a redirect by short URL")

  val getFullUrlByHashServerEndpoint =
    getFullUrlByHashEndpoint.serverLogicOption(urls.getFullUrl)

  val postFullUrlEndpoint =
    endpoint.post
      .in(plainBody[FullUrl])
      .out(plainBody[UrlHash] and statusCode(StatusCode.Created).description("Short URL was created"))
      .description("Shorten an arbitrary URL")

  val postFullUrlServerEndpoint =
    postFullUrlEndpoint.serverLogicSuccess(shortUrl.makeShortUrl)

  val endpoints = getFullUrlByHashServerEndpoint :: postFullUrlServerEndpoint :: Nil
}
