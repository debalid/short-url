package io.debalid.shorturl.config

import io.debalid.shorturl.config.model._

import ciris._
import com.comcast.ip4s.IpLiteralSyntax
import enumeratum.EnumEntry.Lowercase
import enumeratum.{ CirisEnum, Enum, EnumEntry }
import eu.timepit.refined.auto._

object ConfigLoader {

  sealed trait Environment extends EnumEntry with Lowercase

  object Environment extends Enum[Environment] with CirisEnum[Environment] {

    case object Test extends Environment
    case object Prod extends Environment

    override val values: IndexedSeq[Environment] = findValues
  }

  def make[F[_]]: ConfigValue[F, AppConfig] =
    env("SU_APP_ENV")
      .default("test")
      .as[Environment]
      .map { environment =>
        AppConfig(
          http = HttpServerConfig(
            host = host"0.0.0.0",
            port = port"8080"
          ),
          redis = RedisConfig(
            uri = environment match {
              case Environment.Test => RedisUri("redis://localhost:6379")
              case Environment.Prod => RedisUri("redis://redis:6379")
            }
          ),
          counter = CounterConfig(
            name = "requests_counter"
          )
        )
      }

}
