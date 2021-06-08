package io.debalid.shorturl

import io.debalid.shorturl.domain.urls.{ FullUrl, UrlHash }
import io.debalid.shorturl.services.{ Counter, Hashes, Urls }

import cats.effect.{ IO, Ref }
import cats.syntax.eq._

object TestInstances {
  def makeTestUrls(ref: Ref[IO, Map[UrlHash, FullUrl]]): Urls[IO] = new Urls[IO] {
    override def getFullUrl(shortUrl: UrlHash): IO[Option[FullUrl]] = ref.get.map(_.get(shortUrl))
    override def getShortUrl(fullUrl: FullUrl): IO[Option[UrlHash]] =
      ref.get.map(_.collectFirst {
        case (hash, url) if url === fullUrl => hash
      })
    override def link(shortUrl: UrlHash, fullUrl: FullUrl): IO[Unit] =
      ref.getAndUpdate(_.updated(shortUrl, fullUrl)).void
  }

  def makeTestCounter(ref: Ref[IO, Long]): Counter[IO] = new Counter[IO] {
    override def incr: IO[Long] = ref.updateAndGet(_ + 1)
    override def get: IO[Long]  = ref.get
  }

  def makeTestHashes(returnValue: Long => IO[UrlHash]): Hashes[IO] = (from: Long) => returnValue(from)
}
