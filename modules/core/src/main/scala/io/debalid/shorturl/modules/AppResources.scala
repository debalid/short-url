package io.debalid.shorturl.modules

import io.debalid.shorturl.config.model.AppConfig

import cats.effect.{ Concurrent, Resource }
import cats.syntax.parallel._
import dev.profunktor.redis4cats.codecs.Codecs
import dev.profunktor.redis4cats.codecs.splits.stringLongEpi
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.effect.MkRedis
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import eu.timepit.refined.auto._
import org.typelevel.log4cats.Logger

trait AppResources[F[_]] {
  def redisCache: RedisCommands[F, String, String]
  // Redis4Cats forces to use a separate RedicCodec for INCR operation by design
  def redisCounters: RedisCommands[F, String, Long]
}

object AppResources {

  def make[F[_]: Concurrent: MkRedis: Logger](cfg: AppConfig): Resource[F, AppResources[F]] =
    (
      Redis[F].utf8(cfg.redis.uri.value),
      Redis[F].simple(cfg.redis.uri.value, Codecs.derive(RedisCodec.Utf8, stringLongEpi))
    ).parMapN { (cache, counters) =>
      new AppResources[F] {
        override def redisCache: RedisCommands[F, String, String]  = cache
        override def redisCounters: RedisCommands[F, String, Long] = counters
      }
    }
}
