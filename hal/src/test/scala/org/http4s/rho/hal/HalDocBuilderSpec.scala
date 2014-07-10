package org.http4s.rho.hal

import org.http4s.Uri
import org.http4s.UriTemplate
import org.http4s.UriTemplate._
import org.specs2.mutable.Specification

object HalDocBuilderSpec extends Specification {

  "HalDocBuilder" should {

    "create empty ResourceObject" in {
      new HalDocBuilder().build() must equalTo(ResourceObject())
    }

    "create LinkObject from Uri" in {
      new HalDocBuilder()
        .link("self", Uri(path = "/some/where"))
        .build() must equalTo(ResourceObject(List("self" -> Single(LinkObject("/some/where")))))
    }

    "create LinkObject from UriTemplate" in {
      new HalDocBuilder()
        .link("self", UriTemplate(path = List(PathElm("some"), PathExp("where"))))
        .build() must equalTo(ResourceObject(List("self" -> Single(LinkObject("/some{/where}", templated = Some(true))))))
    }

    val document = ResourceObject(

      links = List(
        "self" -> Single(LinkObject("/orders")),
        "curies" -> Many(LinkObject(name = Some("ea"), href = "http://example.com/docs/rels/{rel}", templated = Some(true))),
        "next" -> Single(LinkObject("/orders?page=2")),
        "ea:find" -> Single(LinkObject("/orders{?id}", templated = Some(true))),
        "ea:admin" -> Many(LinkObject("/admins/2", title = Some("Fred")), LinkObject("/admins/5", title = Some("Kate")))),

      embedded = List(
        "ea:order" ->
          Many(
            ResourceObject[Map[String, Any]](
              List(
                "self" -> Single(LinkObject("/orders/123")),
                "ea:basket" -> Single(LinkObject("/baskets/98712")),
                "ea:customer" -> Single(LinkObject("/customers/7809"))),
              Nil,
              Some(Map("total" -> 30.00,
                "currency" -> "USD",
                "status" -> "shipped"))),
            ResourceObject[Map[String, Any]](
              List(
                "self" -> Single(LinkObject("/orders/124")),
                "ea:basket" -> Single(LinkObject("/baskets/97213")),
                "ea:customer" -> Single(LinkObject("/customers/12369"))),
              Nil,
              Some(Map("total" -> 20.00,
                "currency" -> "USD",
                "status" -> "processing"))))),

      content = Some(
        Map(
          "currentlyProcessing" -> 14,
          "shippedToday" -> 20)))

    // our data structure
    val documentBuilder =
      new HalDocBuilder[Map[String, Int]]()
        .link("self", "/orders")
        .links("curies", LinkObject(name = Some("ea"), href = "http://example.com/docs/rels/{rel}", templated = Some(true)))
        .link("next", "/orders?page=2")
        .link("ea:find", "/orders{?id}", Some(true))
        .links("ea:admin", LinkObject("/admins/2", title = Some("Fred")), LinkObject("/admins/5", title = Some("Kate")))
        .resources("ea:order",
          new HalDocBuilder[Map[String, Any]]()
            .link("self", LinkObject("/orders/123"))
            .link("ea:basket", LinkObject("/baskets/98712"))
            .link("ea:customer", LinkObject("/customers/7809"))
            .content(Map("total" -> 30.00,
              "currency" -> "USD",
              "status" -> "shipped")).build,
          new HalDocBuilder[Map[String, Any]]()
            .link("self", LinkObject("/orders/124"))
            .link("ea:basket", LinkObject("/baskets/97213"))
            .link("ea:customer", LinkObject("/customers/12369"))
            .content(Map("total" -> 20.00,
              "currency" -> "USD",
              "status" -> "processing")).build)
        .content(Map(
          "currentlyProcessing" -> 14,
          "shippedToday" -> 20))
        .build

    "build a ResourceObject" in {
      documentBuilder must be equalTo document
    }
  }

}
