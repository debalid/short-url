package io.debalid.shorturl.programs

import io.debalid.shorturl.domain.urls.{ FullUrl, UrlHash }
import io.debalid.shorturl.services.{ Counter, Hashes, Urls }

import cats.Monad
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.log4cats.Logger

final case class ShortUrl[F[_]: Monad: Logger](urls: Urls[F], counter: Counter[F], hashes: Hashes[F]) {

  def makeShortUrl(fullUrl: FullUrl): F[UrlHash] =
    urls.getShortUrl(fullUrl).flatMap {
      case Some(value) =>
        Logger[F].info(s"Found existing record: $fullUrl -> $value") >>
          value.pure
      case None =>
        for {
          currentNum <- counter.incr
          _          <- Logger[F].info(s"Creating a new hash for $currentNum")
          newHash    <- hashes.create(currentNum)
          _          <- Logger[F].info(s"Attempt to link $fullUrl with $newHash")
          _          <- urls.link(newHash, fullUrl)
          res        <- urls.getShortUrl(fullUrl)
          _          <- Logger[F].info(s"Linked $fullUrl with $res")
        } yield res.getOrElse(newHash)
    }

}
