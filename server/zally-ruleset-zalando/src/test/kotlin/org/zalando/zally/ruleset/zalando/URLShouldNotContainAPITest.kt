package org.zalando.zally.ruleset.zalando

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.test.ZallyAssertions

class URLShouldNotContainAPITest {

    private val rule = URLShouldNotContainAPI()

    @Test
    fun `Expect no violations, when correct server missing`() {
        @Language("YAML")
        val yaml = """
          openapi: 3.0.1
          "servers": [
            {
            },
            {
              "url": "https://api{env}.landonline.govt.nz/my-proxy-generation-test-api",
              "variables": {
                "env": {
                  "enum": [
                    ".dev",
                    ".env"
                  ],
                  "default": ".dev",
                  "description": "Not prod environments"
                }
              }
            }
          ]

        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.validate(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `Expect no violations, when no servers set`() {
        @Language("YAML")
        val yaml = """
          openapi: 3.0.1

        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.validate(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `Expect no violations, when correct internal api gateway and has appropriate basePath`() {
        @Language("YAML")
        val yaml = """
          openapi: 3.0.1
          "servers": [
            {
              "url": "https://api.landonline.govt.nz/my-proxy-generation-test-api"
            },
            {
              "url": "https://api{env}.landonline.govt.nz/my-proxy-generation-test-api",
              "variables": {
                "env": {
                  "enum": [
                    ".dev",
                    ".env"
                  ],
                  "default": ".dev",
                  "description": "Not prod environments"
                }
              }
            }
          ]

        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.validate(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `Expect no violations, when correct public api gateway, and has appropriate basePath`() {
        @Language("YAML")
        val yaml = """
          openapi: 3.0.1
          "servers": [
            {
              "url": "https://public.api.landonline.govt.nz/my-proxy-generation-test-api"
            },
            {
              "url": "https://public.api{env}.landonline.govt.nz/my-proxy-generation-test-api",
              "variables": {
                "env": {
                  "enum": [
                    ".dev",
                    ".env"
                  ],
                  "default": ".dev",
                  "description": "Not prod environments"
                }
              }
            }
          ]

        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.validate(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `Expect violations, when api in first resource`() {
        @Language("YAML")
        val yaml = """
          openapi: 3.0.1
          "servers": [
            {
              "url": "https://api.landonline.govt.nz/api/resource"
            },
            {
              "url": "https://api{env}.landonline.govt.nz/api/resource",
              "variables": {
                "env": {
                  "enum": [
                    ".dev",
                    ".env"
                  ],
                  "default": ".dev",
                  "description": "Not prod environments"
                }
              }
            }
          ]

        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.validate(context)

        ZallyAssertions.assertThat(violations)
            .descriptionsAllEqualTo("URL shouldn't contain /api")
    }

    @Test
    fun `Expect violations, when api in second resource`() {
        @Language("YAML")
        val yaml = """
          openapi: 3.0.1
          "servers": [
            {
              "url": "https://api.landonline.govt.nz/resource/api"
            },
            {
              "url": "https://api{env}.landonline.govt.nz/resource/api",
              "variables": {
                "env": {
                  "enum": [
                    ".dev",
                    ".env"
                  ],
                  "default": ".dev",
                  "description": "Not prod environments"
                }
              }
            }
          ]

        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.validate(context)

        ZallyAssertions.assertThat(violations)
            .descriptionsAllEqualTo("URL shouldn't contain /api")
    }
}
