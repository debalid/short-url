package io.debalid.shorturl.domain

import io.debalid.shorturl.domain.urls.{ FullUrl, UrlHash }

import eu.timepit.refined.api.Refined
import org.scalacheck.Gen

object Generators {

  val fullUrlGen: Gen[FullUrl] = for {
    name     <- Gen.alphaLowerStr.suchThat(_.nonEmpty)
    protocol <- Gen.frequency(1 -> Gen.const("http"), 2 -> Gen.const("https"))
    www      <- Gen.oneOf("www.", "")
    zone     <- Gen.oneOf("com", "net", "org", "io", "ru")
    path     <- Gen.alphaLowerStr
  } yield FullUrl(Refined.unsafeApply(s"$protocol://${www}$name.$zone/$path"))

  val urlHashGen: Gen[UrlHash] = for {
    char <- Gen.oneOf(('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9') :+ ('_'))
    hash <- Gen.stringOfN(10, char)
  } yield UrlHash(Refined.unsafeApply(hash))

}
