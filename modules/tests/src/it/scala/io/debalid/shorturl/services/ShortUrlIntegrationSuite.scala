package io.debalid.shorturl.services

import io.debalid.shorturl.domain.Generators._
import io.debalid.shorturl.domain.urls.FullUrl
import io.debalid.shorturl.programs.ShortUrl
import io.debalid.shorturl.services.Counter.CounterKey
import io.debalid.shorturl.testcontainers.RedisSuite

import cats.effect.IO
import cats.syntax.eq._
import cats.syntax.show._
import com.dimafeng.testcontainers.munit.TestContainersForAll
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.codecs.Codecs
import dev.profunktor.redis4cats.codecs.splits.stringLongEpi
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.effect.Log.Stdout._
import eu.timepit.refined.api.Refined
import munit.{ CatsEffectSuite, ScalaCheckEffectSuite }
import org.scalacheck.effect.PropF
import org.scalacheck.util.Pretty
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

// Subject to change due to mix of scalacheck / cats effect / testcontainers
class ShortUrlIntegrationSuite
    extends CatsEffectSuite
    with ScalaCheckEffectSuite
    with TestContainersForAll
    with RedisSuite {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private def redisResource =
    for {
      counters <- Redis[IO].simple(redisAddress, Codecs.derive(RedisCodec.Utf8, stringLongEpi))
      storage  <- Redis[IO].utf8(redisAddress)
    } yield counters -> storage

  test("Short URL should match [A-Za-z0-9_]{10} and link to correct URL") {
    redisResource.use {
      case (counters, storage) =>
        PropF
          .forAllF(fullUrlGen) { fullUrl =>
            val counter  = Counter.make[IO](CounterKey("urls_count"), counters)
            val urls     = Urls.make[IO](storage)
            val hashes   = Hashes.make[IO]
            val shortUrl = ShortUrl(urls, counter, hashes)
            for {
              resHash    <- shortUrl.makeShortUrl(fullUrl)
              resFullUrl <- urls.getFullUrl(resHash)
            } yield {
              assert(resHash.show.matches("[A-Za-z0-9_]{10}"), s"Hash $resHash does not match [A-Za-z0-9_]{10}")
              assert(resFullUrl === Option(fullUrl), s"Hash $resHash match $resFullUrl instead of $fullUrl")
            }
          }
          .check()
          .flatMap { result =>
            if (result.passed) IO.unit
            else fail(Pretty.pretty(result, scalaCheckPrettyParameters))
          }
    }
  }

  test("Same URL should have the same short link") {
    redisResource.use {
      case (counters, storage) =>
        PropF
          .forAllF(fullUrlGen) { fullUrl =>
            val counter  = Counter.make[IO](CounterKey("urls_count"), counters)
            val urls     = Urls.make[IO](storage)
            val hashes   = Hashes.make[IO]
            val shortUrl = ShortUrl(urls, counter, hashes)
            for {
              res1   <- shortUrl.makeShortUrl(fullUrl)
              res2   <- shortUrl.makeShortUrl(fullUrl)
              reqNum <- counter.get
            } yield assert(res1 === res2, s"Hashes not the same: $res1 $res2, counter: $reqNum}")
          }
          .check()
          .flatMap { result =>
            if (result.passed) IO.unit
            else fail(Pretty.pretty(result, scalaCheckPrettyParameters))
          }
    }
  }

  test("Unique URLs should increase counter") {
    redisResource.use {
      case (counters, storage) =>
        val counter  = Counter.make[IO](CounterKey("urls_count"), counters)
        val urls     = Urls.make[IO](storage)
        val hashes   = Hashes.make[IO]
        val shortUrl = ShortUrl(urls, counter, hashes)
        for {
          prevCount <- counter.get
          res1      <- shortUrl.makeShortUrl(FullUrl(Refined.unsafeApply("http://myuniqueaddress.io")))
          res2      <- shortUrl.makeShortUrl(FullUrl(Refined.unsafeApply("http://myuniqueaddress2.io")))
          nextCount <- counter.get
        } yield assert(
          nextCount - 2 >= prevCount,
          s"Counter was not increased for $res1 $res2 by 2, prev=$prevCount, next=$nextCount"
        )
    }
  }
}
