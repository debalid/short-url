package io.debalid.shorturl.domain

import cats.{ Eq, Show }
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string._
import io.estatico.newtype.macros.newtype

object urls {

  @newtype case class FullUrl(value: String Refined Url)
  object FullUrl {
    implicit val showForFullUrl: Show[FullUrl] = Show.fromToString[FullUrl]
    implicit val eqForFullUrl: Eq[FullUrl]     = Eq.fromUniversalEquals
  }

  type Base63Predicate = MatchesRegex["[A-Za-z0-9_]{10}"]
  @newtype case class UrlHash(value: String Refined Base63Predicate)
  object UrlHash {
    implicit val showForUrlHash: Show[UrlHash] = Show.fromToString[UrlHash]
    implicit val eqForUrlHash: Eq[UrlHash]     = Eq.fromUniversalEquals
  }

}
