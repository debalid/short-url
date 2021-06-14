package io.debalid.shorturl.http

import io.debalid.shorturl.domain.urls._

import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.codec.newtype._
import sttp.tapir.codec.refined._
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi._
import sttp.tapir.openapi.circe.yaml._

// API description via tapir DSL for OpenAPI
object Endpoints {

  def openApiYaml: String =
    OpenAPIDocsInterpreter
      .toOpenAPI(
        List(
          GetFullUrlByHash,
          PostFullUrl
        ),
        Info(title = "short-url", version = "v1")
      )
      .toYaml

  val GetFullUrlByHash: Endpoint[UrlHash, Unit, FullUrl, Any] =
    endpoint.get
      .in(path[UrlHash]("urlHash").description("Url hash"))
      .out(plainBody[FullUrl] and statusCode(StatusCode.PermanentRedirect).description("Redirects to full url"))
      .errorOut(statusCode(StatusCode.NotFound).description("Doesn't have a linked URL for this hash"))
      .description("Get a redirect by short URL")

  val PostFullUrl: Endpoint[FullUrl, Unit, UrlHash, Any] =
    endpoint.post
      .in(plainBody[FullUrl])
      .out(plainBody[UrlHash] and statusCode(StatusCode.Ok).description("Short URL was created"))
      .description("Shorten an arbitrary URL")
}
