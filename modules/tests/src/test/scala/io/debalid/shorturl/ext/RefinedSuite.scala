package io.debalid.shorturl.ext

import io.debalid.shorturl.domain.Generators._
import io.debalid.shorturl.ext.refined._

import cats.effect.IO
import cats.syntax.show._
import org.scalacheck.Gen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object RefinedSuite extends SimpleIOSuite with Checkers {

  test("Google url string should be unapplied") {
    IO {
      UrlString.unapply("http://google.com/somepath") match {
        case Some(value) => success
        case None        => failure("Correct URL was nt unapplied correctly")
      }
    }
  }

  test("Correct url string should be unapplied") {
    forall(fullUrlGen.map(_.show)) { strValue =>
      UrlString.unapply(strValue) match {
        case Some(_) => success
        case None    => failure("Correct URL was nt unapplied correctly")
      }
    }
  }

  test("Invalid Google url string should not be unapplied") {
    IO {
      UrlString.unapply("http//google.com/somepath") match {
        case Some(value) => failure(s"Invalid URL was unapplied! $value")
        case None    => success
      }
    }
  }

  test("Invalid URL string should not be unapplied [gen]") {
    forall(Gen.alphaStr) { strValue =>
      UrlString.unapply(strValue) match {
        case Some(_) => failure("Invalid URL was unapplied!")
        case None    => success
      }
    }
  }

}
