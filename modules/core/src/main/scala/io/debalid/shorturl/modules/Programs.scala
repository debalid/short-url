package io.debalid.shorturl.modules

import io.debalid.shorturl.programs.ShortUrl

import cats.Monad
import org.typelevel.log4cats.Logger

trait Programs[F[_]] {
  def shortUrl: ShortUrl[F]
}

object Programs {

  def make[F[_]: Monad: Logger](services: Services[F]): Programs[F] = new Programs[F] {
    override val shortUrl: ShortUrl[F] = ShortUrl(services.urls, services.counter, services.hashes)
  }

}
