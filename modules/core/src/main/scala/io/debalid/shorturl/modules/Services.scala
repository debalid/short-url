package io.debalid.shorturl.modules

import io.debalid.shorturl.config.model.AppConfig
import io.debalid.shorturl.services._

import cats.effect.Sync

trait Services[F[_]] {
  def urls: Urls[F]
  def hashes: Hashes[F]
  def counter: Counter[F]
}

object Services {

  def make[F[_]: Sync](
      cfg: AppConfig,
      redisResources: AppResources[F]
  ): Services[F] = new Services[F] {
    override def urls: Urls[F]     = Urls.make[F](redisResources.redisCache)
    override def hashes: Hashes[F] = Hashes.make[F]
    override def counter: Counter[F] =
      Counter.make(Counter.CounterKey(cfg.counter.name.value), redisResources.redisCounters)
  }

}
