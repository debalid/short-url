package io.debalid.shorturl.services

import cats.Functor
import cats.syntax.functor._
import cats.syntax.show._
import derevo.cats.show
import derevo.derive
import dev.profunktor.redis4cats.RedisCommands
import io.estatico.newtype.macros.newtype

trait Counter[F[_]] {
  def incr: F[Long]
  def get: F[Long]
}

object Counter {

  @derive(show)
  @newtype case class CounterKey(value: String)

  def make[F[_]: Functor](key: CounterKey, cmd: RedisCommands[F, String, Long]): Counter[F] = new Counter[F] {
    override def incr: F[Long] = cmd.incr(key.show)
    override def get: F[Long]  = cmd.get(key.show).map(_.getOrElse(0))
  }

}
