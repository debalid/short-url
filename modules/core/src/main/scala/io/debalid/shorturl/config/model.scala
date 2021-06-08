package io.debalid.shorturl.config

import com.comcast.ip4s.{ Host, Port }
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.string.{ StartsWith, Uri }
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

object model {

  case class AppConfig(
      http: HttpServerConfig,
      redis: RedisConfig,
      counter: CounterConfig
  )

  case class HttpServerConfig(
      host: Host,
      port: Port
  )

  @newtype case class RedisUri(value: String Refined (Uri And StartsWith["redis://"]))
  case class RedisConfig(uri: RedisUri)

  case class CounterConfig(name: NonEmptyString)

}
