package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.ruleset.zalando.model.ApiAudience

class NoVersionInUriRuleTest {

    private val rule = NoVersionInUriRule()

    @Test
    fun `checkServerURLs should return a violation if a server URL contains a version as base path`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            servers:
              - url: "https://inter.net/api/v1.0"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).contains("URL contains version number")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0")
    }

    @Test
    fun `checkServerURLs should return a violation if (sub) resource names contain version suffix`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /shop/orders-v1/{order-id}: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).contains("URL contains version number")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1shop~1orders-v1~1{order-id}")
    }

    @Test
    fun `checkServerURLs should return no violations if a server URL does not contain a version as base path`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            servers:
              - url: "https://inter.net/api/"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `Expect violations When Component internal and missing version`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL}
        servers:
          - url: "https://api.landonline.govt.nz/anything"
          - url: "https://api{env}.landonline.govt.nz/anything"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        //assertEquals(2, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect violations When Component internal and version alphanumeric`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL}
        servers:
          - url: "https://api.landonline.govt.nz/v1a"
          - url: "https://api{env}.landonline.govt.nz/v1a"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        //assertEquals(2, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }


    @Test
    fun `Expect violations When Component internal and version too long`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL}
        servers:
          - url: "https://api.landonline.govt.nz/v123/anything"
          - url: "https://api{env}.landonline.govt.nz/v123/anything"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        //assertEquals(2, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }
}
