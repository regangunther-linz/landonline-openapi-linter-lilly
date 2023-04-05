package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.ruleset.zalando.model.ApiAudience

class VersionInUriRuleTest {

    private val rule = VersionInUriRule()

    @Test
    fun `checkServerURLs should not return a violation if version correct`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            servers:
              - url: "https://inter.govt.nz/v1/titles"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `checkServerURLs should return a violation if version in between two resources`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            servers:
              - url: "https://inter.govt.nz/api/v1/titles"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).contains("URL must contain a single Major version number e.g. v1, v2 etc.")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0")
    }

    @Test
    fun `checkServerURLs should return a violation if version has dot in a server URL is second resouce in base path`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            servers:
              - url: "https://inter.govt.nz/api/v1.0"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).contains("URL must contain a single Major version number e.g. v1, v2 etc.")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0")
    }

    @Test
    fun `checkServerURLs should return a violation if version has dot in a server URL is first resouce in base path`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            servers:
              - url: "https://inter.govt.nz/v1.0/api"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).contains("URL must contain a single Major version number e.g. v1, v2 etc.")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0")
    }

    @Test
    fun `checkServerURLs should return violation if a server URL does not contain a version as base path`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            servers:
              - url: "https://inter.govt.nz/api/"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).contains("URL must contain a single Major version number e.g. v1, v2 etc.")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0")
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

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(2)
        assertThat(violations[0].description).contains("URL must contain a single Major version number e.g. v1, v2 etc.")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0")
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

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(2)
        assertThat(violations[0].description).contains("URL must contain a single Major version number e.g. v1, v2 etc.")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0")
    }
}
