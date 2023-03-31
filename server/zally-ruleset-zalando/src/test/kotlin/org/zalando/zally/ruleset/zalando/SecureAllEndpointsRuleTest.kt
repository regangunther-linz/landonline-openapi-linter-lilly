package org.zalando.zally.ruleset.zalando

import org.zalando.zally.test.ZallyAssertions
import org.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.core.rulesConfig

class SecureAllEndpointsRuleTest {

    private val rule = SecureAllEndpointsRule(rulesConfig)

    @Test
    fun `checkHasValidSecuritySchemes should not return violation if no security scheme is specified when audience is public`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience: "external-public"
            
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violation = rule.checkHasValidSecuritySchemes(context)

        assertThat(violation).isNull()
    }
    @Test
    fun `checkHasValidSecuritySchemes should not return violation if no security scheme is specified and audience is not provided`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violation = rule.checkHasValidSecuritySchemes(context)

        assertThat(violation).isNull()
    }
    @Test
    fun `checkHasValidSecuritySchemes should return violation if no security scheme is specified and audience is company-internal`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience: "company-internal"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violation = rule.checkHasValidSecuritySchemes(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).isEqualTo("API must be secured by Bearer Authentication")
        assertThat(violation.pointer.toString()).isEqualTo("/components/securitySchemes")
    }
    @Test
    fun `checkHasValidSecuritySchemes should return violation if no security scheme is specified and audience is component-internal`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience: "component-internal"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violation = rule.checkHasValidSecuritySchemes(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).isEqualTo("API must be secured by Bearer Authentication")
        assertThat(violation.pointer.toString()).isEqualTo("/components/securitySchemes")
    }

    @Test
    fun `checkHasValidSecuritySchemes should return a violation if OAuth2 security scheme is specified`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience: "company-internal"
            components:
              securitySchemes:
                company-oauth2:
                  type: oauth2
                  flows:
                    clientCredentials:
                      authorizationUrl: https://identity.company.com/oauth2/auth
                      refreshUrl: https://identity.company.com/oauth2/refresh
                      tokenUrl: https://identity.company.com/oauth2/token
                      scopes:
                        read: read access to the resources of this API
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violation = rule.checkHasValidSecuritySchemes(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).isEqualTo("API must be secured by Bearer Authentication")
        assertThat(violation.pointer.toString()).isEqualTo("/components/securitySchemes")
    }

    @Test
    fun `checkHasValidSecuritySchemes should not return violation if Bearer Authorization security scheme is specified`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            x-audience: "company-internal"
            components:
              securitySchemes:
                bearer:
                  type: http
                  scheme: bearer
                  bearerFormat: JWT
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violation = rule.checkHasValidSecuritySchemes(context)

        assertThat(violation).isNull()
    }

    @Test
    fun `checkHasValidSecuritySchemes should return violation if invalid apikey security scheme is specified`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience: "company-internal"
            components:
              securitySchemes:
                company-oauth2:
                  type: apiKey
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violation = rule.checkHasValidSecuritySchemes(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).isEqualTo("API must be secured by Bearer Authentication")
        assertThat(violation.pointer.toString()).isEqualTo("/components/securitySchemes")
    }

    @Test
    fun `checkHasNoInvalidSecuritySchemes should return no violation if no security scheme is specified`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkHasNoInvalidSecuritySchemes(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `checkHasNoInvalidSecuritySchemes should return violation if OAuth2 security scheme is specified`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience: "company-internal"
            components:
              securitySchemes:
                company-oauth2:
                  type: oauth2
                  flows:
                    clientCredentials:
                      authorizationUrl: https://identity.company.com/oauth2/auth
                      refreshUrl: https://identity.company.com/oauth2/refresh
                      tokenUrl: https://identity.company.com/oauth2/token
                      scopes:
                        read: read access to the resources of this API
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkHasNoInvalidSecuritySchemes(context)

        ZallyAssertions.assertThat(violations)
            .descriptionsEqualTo("API must be secured by Bearer Authentication")
            .pointersEqualTo("/components/securitySchemes/company-oauth2")
    }

    @Test
    fun `checkHasNoInvalidSecuritySchemes should return no violation if Bearer Authorization security scheme is specified`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience: "company-internal"
            components:
              securitySchemes:
                company-oauth2:
                  type: http
                  scheme: bearer
                  bearerFormat: JWT
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkHasNoInvalidSecuritySchemes(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `checkHasNoInvalidSecuritySchemes should return violation if invalid apikey security definition is specified`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience: "company-internal"
            components:
              securitySchemes:
                company-oauth2:
                  type: apiKey
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkHasNoInvalidSecuritySchemes(context)

        ZallyAssertions.assertThat(violations)
            .descriptionsEqualTo("API must be secured by Bearer Authentication")
            .pointersEqualTo("/components/securitySchemes/company-oauth2")
    }

    @Test
    fun `checkUsedScopesAreDefined should return violation for each undefined scope`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience: "company-internal"
            paths:
              /article:
                post:
                  security:
                    - oauth2:
                        - write # is not defined in the security schemes
            components:
              securitySchemes:
                oauth2:
                  type: oauth2
                  flows:
                    clientCredentials:
                      authorizationUrl: https://identity.company.com/oauth2/auth
                      refreshUrl: https://identity.company.com/oauth2/refresh
                      tokenUrl: https://identity.company.com/oauth2/token
                      scopes:
                        read: read access to the resources of this API
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkUsedScopesAreSpecified(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).contains("The scope 'oauth2/write' is not specified")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1article/post/security/0/oauth2/0")
    }

    @Test
    fun `checkUsedScopesAreSpecified should return no violation if only defined scopes are used`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience: "company-internal"
            paths:
              /article:
                post:
                  security:
                    - oauth2:
                        - read-1 # is defined in the security schemes
                        - read-2 # is defined in the security schemes
            components:
              securitySchemes:
                oauth2:
                  type: oauth2
                  flows:
                    clientCredentials:
                      authorizationUrl: https://identity.company.com/oauth2/auth
                      refreshUrl: https://identity.company.com/oauth2/refresh
                      tokenUrl: https://identity.company.com/oauth2/token
                      scopes:
                        read-1: read access to the resources of this API
                    implicit:
                      authorizationUrl: https://identity.company.com/oauth2/auth
                      refreshUrl: https://identity.company.com/oauth2/refresh
                      scopes:
                        read-2: read access to the resources of this API
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkUsedScopesAreSpecified(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `checkUsedScopesAreDefined should return no violation for Bearer Authentication`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience: "company-internal"
            paths:
              /article:
                post:
                  security:
                    - bearer:
                        - write # is not defined in the security schemes
            components:
              securitySchemes:
                bearer:
                  type: http
                  scheme: bearer
                  bearerFormat: JWT
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)
        val violations = rule.checkUsedScopesAreSpecified(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `checkUsedScopesAreSpecified should ignore OpenAPI 2 (Swagger) specification`() {
        @Language("YAML")
        val content = """
            swagger: 2.0
            info:
              title: Old API
              version: 1
        """.trimIndent()
        val context = DefaultContextFactory().getSwaggerContext(content)

        val violations = rule.checkUsedScopesAreSpecified(context)

        assertThat(violations).isEmpty()
    }
}
