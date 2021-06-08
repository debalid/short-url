package io.debalid.shorturl.programs

import io.debalid.shorturl.TestInstances._
import io.debalid.shorturl.domain.Generators._
import io.debalid.shorturl.domain.urls._

import cats.effect.{ IO, Ref }
import cats.syntax.semigroup._
import eu.timepit.refined.api.Refined
import org.scalacheck.Gen
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object ShortUrlSuite extends SimpleIOSuite with Checkers {

  implicit val logger: Logger[IO] = NoOpLogger[IO]

  val pairsGen: Gen[(UrlHash, FullUrl)] = for {
    hash <- urlHashGen
    url  <- fullUrlGen
  } yield hash -> url

  test("should take existing link (hash <-> url)") {
    forall(pairsGen) {
      case (hash, fullUrl) =>
        for {
          urls    <- Ref.of[IO, Map[UrlHash, FullUrl]](Map(hash -> fullUrl)).map(makeTestUrls)
          counter <- Ref.of[IO, Long](0).map(makeTestCounter)
          nonExistingHash = UrlHash(Refined.unsafeApply("0000000000"))
          hashes          = makeTestHashes(_ => IO.pure(nonExistingHash))
          shortUrl        = ShortUrl[IO](urls, counter, hashes)
          resHash         <- shortUrl.makeShortUrl(fullUrl)
          resMaybeFullUrl <- urls.getFullUrl(resHash)
          resCounter      <- counter.get
        } yield {
          expect.same(hash, resHash) |+|
            expect.same(Option(fullUrl), resMaybeFullUrl) |+|
            expect.same(resCounter, 0)
        }
    }
  }

  test("should create a new link (hash <-> url) if url is missing") {
    forall(fullUrlGen) { fullUrl =>
      for {
        urls    <- Ref.of[IO, Map[UrlHash, FullUrl]](Map.empty).map(makeTestUrls)
        counter <- Ref.of[IO, Long](0).map(makeTestCounter)
        newlyCreatedHash = UrlHash(Refined.unsafeApply("0000000000"))
        hashes           = makeTestHashes(_ => IO.pure(newlyCreatedHash))
        shortUrl         = ShortUrl[IO](urls, counter, hashes)
        resHash         <- shortUrl.makeShortUrl(fullUrl)
        resMaybeFullUrl <- urls.getFullUrl(resHash)
        resCounter      <- counter.get
      } yield {
        expect.same(newlyCreatedHash, resHash) |+|
          expect.same(Option(fullUrl), resMaybeFullUrl) |+|
          expect.same(resCounter, 1)
      }
    }
  }

}
