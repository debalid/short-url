package io.debalid.shorturl.ext

import io.debalid.shorturl.domain.urls.Base63Predicate

import eu.timepit.refined._
import eu.timepit.refined.api.{ Refined, Validate }
import eu.timepit.refined.string.Url

object refined {

  object Base63String {
    def unapply(value: String)(implicit ev: Validate[String, Base63Predicate]): Option[String Refined Base63Predicate] =
      refineV[Base63Predicate](value).toOption
  }

  object UrlString {
    def unapply(value: String)(implicit ev: Validate[String, Url]): Option[String Refined Url] =
      refineV[Url](value).toOption
  }

}
