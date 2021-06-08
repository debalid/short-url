package io.debalid.shorturl.services

import scala.annotation.tailrec

import io.debalid.shorturl.domain.urls.UrlHash

import cats.effect.kernel.Sync
import eu.timepit.refined.api.Refined

trait Hashes[F[_]] {
  def create(from: Long): F[UrlHash]
}

object Hashes {

  def make[F[_]: Sync]: Hashes[F] = new Hashes[F] {

    private val Base63Schema       = ('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9') :+ '_'
    private val PaddingLength: Int = 10

    override def create(from: Long): F[UrlHash] = Sync[F].delay {
      val res = leftPad(toBase63(from)).mkString
      // Completely safe by generation
      UrlHash(Refined.unsafeApply(res))
    }

    @tailrec
    private def toBase63(current: Long, acc: List[Char] = List.empty): List[Char] =
      if (current < Base63Schema.length) {
        Base63Schema(current.toInt) :: acc
      } else {
        toBase63(current / Base63Schema.length, Base63Schema((current % Base63Schema.length).toInt) :: acc)
      }

    private def leftPad(value: List[Char]): List[Char] = {
      val remaining = PaddingLength - value.length
      if (remaining > 0) {
        List.fill(remaining)(Base63Schema(0)) ++ value
      } else value
    }
  }
}
