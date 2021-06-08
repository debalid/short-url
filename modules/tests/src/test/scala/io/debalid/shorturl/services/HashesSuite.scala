package io.debalid.shorturl.services

import cats.effect.IO
import cats.syntax.show._
import org.scalacheck.Gen
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger
import weaver.SimpleIOSuite
import weaver.scalacheck.{CheckConfig, Checkers}

object HashesSuite extends SimpleIOSuite with Checkers {

  implicit val logger: Logger[IO] = NoOpLogger[IO]

  val Base63Max: Long = math.pow(63, 10).toLong

  val longGen: Gen[Long] = Gen.choose(1L, Base63Max)

  test("should create valid hashes for all possible longs") {
    forall.withConfig(CheckConfig.default.copy(
      minimumSuccessful = 10000,
    ))(longGen) { longValue =>
      val hashes = Hashes.make[IO]
      for {
        hash <- hashes.create(longValue)
      } yield expect(hash.show.matches("[A-Za-z0-9_]{10}"))
    }
  }

  test("should create a valid hash for 0") {
      val hashes = Hashes.make[IO]
      for {
        hash <- hashes.create(0)
      } yield expect.same(hash.show, "AAAAAAAAAA")
  }

  test("should create a valid hash for 1") {
    val hashes = Hashes.make[IO]
    for {
      hash <- hashes.create(1)
    } yield expect.same(hash.show, "AAAAAAAAAB")
  }

  test("should create a valid hash for Base63Max") {
    val hashes = Hashes.make[IO]
    for {
      hash <- hashes.create(Base63Max)
    } yield expect.same(hash.show, "__________")
  }

}
