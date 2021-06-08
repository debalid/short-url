package io.debalid.shorturl.services

import io.debalid.shorturl.domain.urls._
import io.debalid.shorturl.ext.refined._

import cats.FlatMap
import cats.syntax.functor._
import cats.syntax.show._
import dev.profunktor.redis4cats.RedisCommands

trait Urls[F[_]] {
  def getFullUrl(shortUrl: UrlHash): F[Option[FullUrl]]
  def getShortUrl(fullUrl: FullUrl): F[Option[UrlHash]]
  def link(shortUrl: UrlHash, fullUrl: FullUrl): F[Unit]
}

object Urls {

  def make[F[_]: FlatMap](cmd: RedisCommands[F, String, String]): Urls[F] = new Urls[F] {

    override def getFullUrl(shortUrl: UrlHash): F[Option[FullUrl]] =
      cmd
        .get(shortUrl.show)
        .map(_.collect({ case UrlString(url) => FullUrl(url) }))

    override def getShortUrl(fullUrl: FullUrl): F[Option[UrlHash]] =
      cmd
        .get(fullUrl.show)
        .map(_.collect({ case Base63String(url) => UrlHash(url) }))

    // atomic, does not replace if fullUrl already exists (or shortUrl)
    override def link(shortUrl: UrlHash, fullUrl: FullUrl): F[Unit] =
      cmd.mSetNx(Map(shortUrl.show -> fullUrl.show, fullUrl.show -> shortUrl.show)).void
  }

}
