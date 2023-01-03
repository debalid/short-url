package io.debalid.shorturl.http

import io.debalid.shorturl.TestInstances._
import io.debalid.shorturl.domain.Generators._
import io.debalid.shorturl.domain.urls.{ FullUrl, UrlHash }
import io.debalid.shorturl.programs.ShortUrl

import cats.effect.{ IO, Ref }
import cats.syntax.eq._
import cats.syntax.show._
import eu.timepit.refined.api.Refined
import org.http4s.Method.{ GET, POST }
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax
import org.scalacheck.Gen
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers
import sttp.tapir.server.http4s.Http4sServerInterpreter
import org.http4s.headers.Location

object UrlsRoutesSuite extends SimpleIOSuite with Checkers {

  implicit val logger: Logger[IO] = NoOpLogger[IO]

  private val linksGen = for {
    shortUrl  <- urlHashGen
    urlString <- fullUrlGen
  } yield shortUrl -> urlString

  test("Should return 301 MovedPermanently on valid existing short urls") {
    forall(linksGen) {
      case (shortUrl, fullUrl) =>
        for {
          urlsRoute <- makeTestUrlsRoute(Map(shortUrl -> fullUrl))
          req = GET(Uri.unsafeFromString(s"/${shortUrl.show}"))
          response <- urlsRoute.run(req).value
        } yield response match {
          case Some(response)
              if response.status === Status.MovedPermanently &&
                response.headers.get[Location].contains(Location(Uri.unsafeFromString(fullUrl.show))) =>
            success
          case Some(response) => failure(s"Invalid server response $response on request $req")
          case None           => failure(s"Route not found $req")
        }
    }
  }

  test("Should return 404 NotFound on valid non-existing short urls") {
    forall(linksGen) {
      case (shortUrl, _) =>
        for {
          urlsRoute <- makeTestUrlsRoute(Map.empty)
          req = GET(Uri.unsafeFromString(s"/${shortUrl.show}"))
          response <- urlsRoute.run(req).value
        } yield response match {
          case Some(response) if response.status === Status.NotFound => success
          case Some(response)                                        => failure(s"Invalid server response $response on request $req")
          case None                                                  => failure(s"Route not found $req")
        }
    }
  }

  test("Should return 400 BadRequest on invalid short links") {
    forall(Gen.alphaStr.suchThat(testCase => testCase.nonEmpty && !testCase.matches("[A-Za-z0-9_]{10}"))) { url =>
      for {
        urlsRoute <- makeTestUrlsRoute()
        req = GET(Uri.unsafeFromString(s"/$url"))
        response <- urlsRoute.run(req).value
      } yield response match {
        case Some(response) if response.status === Status.BadRequest => success
        case None                                                    => failure(s"Route not found $req")
        case _                                                       => failure(s"Unexpected server response $response on request $req")
      }
    }
  }

  test("Should return 201 Created on shortening valid url") {
    forall(linksGen) {
      case (shortUrl, fullUrl) =>
        for {
          urlsRoute <- makeTestUrlsRoute(Map(shortUrl -> fullUrl))
          req = POST(fullUrl.show, uri"/")
          response <- urlsRoute.run(req).value
        } yield response match {
          case Some(response)
              if response.status === Status.Created &&
                response.contentType.get.mediaType === MediaType.text.plain =>
            success
          case None => failure(s"Route not found $req")
          case _    => failure(s"Invalid server response $response on request $req")
        }
    }
  }

  test("Should return 400 BadRequest on shortening invalid full urls") {
    forall(Gen.alphaStr.suchThat(_.nonEmpty)) { url =>
      for {
        urlsRoute <- makeTestUrlsRoute()
        req = POST(url.show, uri"/")
        response <- urlsRoute.run(req).value
      } yield response match {
        case Some(response) if response.status === Status.BadRequest => success
        case None                                                    => failure(s"Route not found $req")
        case _                                                       => failure(s"Unexpected server response $response on request $req")
      }
    }
  }

  private def makeTestUrlsRoute(links: Map[UrlHash, FullUrl] = Map.empty) =
    for {
      urls    <- Ref.of[IO, Map[UrlHash, FullUrl]](links).map(makeTestUrls)
      counter <- Ref.of[IO, Long](0).map(makeTestCounter)
      hashes       = makeTestHashes(_ => IO(UrlHash(Refined.unsafeApply("AaABbBCcCD"))))
      urlEndpoints = UrlsEndpoints(urls, ShortUrl[IO](urls, counter, hashes))
    } yield Http4sServerInterpreter[IO]().toRoutes(urlEndpoints.endpoints)

}
