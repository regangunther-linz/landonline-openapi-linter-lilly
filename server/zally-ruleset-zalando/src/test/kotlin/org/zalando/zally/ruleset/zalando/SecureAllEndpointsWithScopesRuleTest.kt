package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.ruleset.zalando.util.getConfigFromContent
import org.zalando.zally.test.ZallyAssertions
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.test.ZallyAssertions.assertThat

/**
 * Tests for SecureAllEndpointsWithScopesRule
 */
@Suppress("StringLiteralDuplication", "UndocumentedPublicFunction", "UnsafeCallOnNullableType", "TooManyFunctions")
class SecureAllEndpointsWithScopesRuleTest {

    private val config = getConfigFromContent(
        """
        SecureAllEndpointsWithScopesRule {
          scope_regex: "linz-domain:full"
          path_whitelist: [
            "^/whitelisted/.*",
            /obscure/
          ]
        }
        """.trimIndent()
    )

    private val rule = SecureAllEndpointsWithScopesRule(config)

    @Test
    fun `Expect no violations from checkDefinedScopeFormats When no security`() {
        @Language("YAML")
        val yaml = """
            swagger: 2.0
        """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkDefinedScopeFormats(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `Expect no violations from checkDefinedScopeFormats with valid oauth2 scopes`() {
        @Language("YAML")
        val yaml = """
            swagger: 2.0
            securityDefinitions:
              implicit-oauth2:
                type: oauth2
                scopes:
                  uid: Any logged in user
                  fulfillment-order.read: Can read fulfillment-order app
                  sales-order.shipment-order.write: Can create shipment-orders in the sales-order app
        """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkDefinedScopeFormats(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `Expect no violations from checkDefinedScopeFormats with basic scopes`() {
        @Language("YAML")
        val yaml = """
            swagger: 2.0
            securityDefinitions:
              lazy-in-house-scripts:
                type: basic
                scopes:
                  indexer: Can perform nightly indexing operations
                  expiry: Can perform automated expiry operations
        """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkDefinedScopeFormats(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `Expect violations from checkDefinedScopeFormats with invalid oauth2 scopes`() {
        @Language("YAML")
        val yaml = """
            swagger: 2.0
            securityDefinitions:
              implicit-oauth2:
                type: oauth2
                flow: implicit
                scopes:
                  max: Any user called Max
        """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkDefinedScopeFormats(context)

        ZallyAssertions.assertThat(violations)
            .descriptionsAllEqualTo("scope 'max' does not match regex 'linz-domain:full'")
            .pointersEqualTo("/securityDefinitions/implicit-oauth2/scopes")
    }

    @Test
    fun `Expect no violations from checkOperationsAreScoped with empty swagger`() {
        @Language("YAML")
        val yaml = """
            swagger: 2.0
        """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `Expect violations from checkOperationsAreScoped with no security on path`() {
        @Language("YAML")
        val yaml = """
            swagger: "2.0"
            securityDefinitions:
              oauth2:
                type: oauth2
                flow: password
                scopes:
                  defined-scope: A defined scope
            paths:
              /things:
                get:
                  responses:
                    200:
                      description: Success
        """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        assertThat(violations)
            .descriptionsEqualTo("Endpoint is not secured by scope(s)")
            .pointersEqualTo("/paths/~1things/get")
    }

    @Test
    fun `Expect no violations from checkOperationsAreScoped with path defined oauth scope`() {
        @Language("YAML")
        val yaml = """
            swagger: "2.0"
            securityDefinitions:
              oauth2:
                type: oauth2
                flow: password
                scopes:
                  defined-scope: A defined scope
            paths:
              /things:
                get:
                  responses:
                    200:
                      description: Success
                  security:
                  - oauth2:
                    - defined-scope
        """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `Expect violations from checkOperationsAreScoped with undefined path scope`() {
        @Language("YAML")
        val yaml = """
            swagger: "2.0"
            securityDefinitions:
              oauth2:
                type: oauth2
                flow: password
                scopes:
                  defined-scope: A defined scope
            paths:
              /things:
                get:
                  responses:
                    200:
                      description: Success
                  security:
                  - oauth2:
                    - undefined-scope
        """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        assertThat(violations)
            .descriptionsEqualTo("Endpoint is secured by undefined OAuth2 scope(s): oauth2:undefined-scope")
            .pointersEqualTo("/paths/~1things/get/security")
    }

    @Test
    fun `Expect no violations from checkOperationsAreScoped with defined top level scope`() {
        @Language("YAML")
        val yaml = """
            swagger: "2.0"
            securityDefinitions:
              oauth2:
                type: oauth2
                flow: password
                scopes:
                  defined-scope: A defined scope
            security:
              - oauth2:
                - defined-scope
            paths:
              /things:
                get:
                  responses:
                    200:
                      description: Success
        """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `Expect no violations from checkOperationsAreScoped with no scope on whitelisted path`() {
        @Language("YAML")
        val yaml = """
            swagger: "2.0"
            securityDefinitions:
              oauth2:
                type: oauth2
                flow: password
                scopes:
                  defined-scope: A defined scope
            paths:
              /whitelisted/path:
                get:
                  responses:
                    200:
                      description: Success
              /really/long/and/obscure/secret/path:
                get:
                  responses:
                    200:
                      description: Success
        """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `Expect violaton, when checkOperationsAreScoped with no scopes defined with OpenAPI components`() {
        @Language("YAML")
        val yaml = """
            openapi: 3.0.1
            
            paths:
              '/things':
                get:
                  responses:
                    200:
                      description: OK
            
            components:      
              securitySchemes:
                oauth2:
                  type: oauth2
                  flows:
                    clientCredentials:
                      tokenUrl: 'https://example.com'
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        assertThat(violations)
            .descriptionsEqualTo("Endpoint is not secured by scope(s)")
            .pointersEqualTo("/paths/~1things/get")
    }

    @Test
    fun `Expect no violaton when Bearer auth set at path level`() {
        @Language("YAML")
        val yaml = """
            openapi: 3.0.1
            
            paths:
              '/things':
                get:
                  responses:
                    200:
                      description: OK
                  security:
                    - BearerAuth: ['linz-domain:full']
            components:      
              securitySchemes:
                BearerAuth:
                  type: http
                  scheme: bearer
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `Expect violations when path is does not define security`() {
        @Language("YAML")
        val yaml = """
            openapi: 3.0.1
            
            paths:
              '/unsecured-path':
                get:
                  responses:
                    200:
                      description: OK
            components:      
              securitySchemes:
                BearerAuth:
                  type: http
                  scheme: bearer
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)
        assertThat(violations).hasSize(1)
    }

    @Test
    fun `Expect violatons when empty Bearer Security Set on path  `() {
        @Language("YAML")
        val yaml = """
            openapi: 3.0.1
            paths:
              '/things':
                get:
                  responses:
                    200:
                      description: OK
                  security:
                    - BearerAuth: []
            components:      
              securitySchemes:
                BearerAuth:
                  type: http
                  scheme: bearer
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)
        assertThat(violations).hasSize(1)
    }
    @Test
    fun `Expect violatons when empty Bearer Security Set on top level  `() {
        @Language("YAML")
        val yaml = """
            openapi: 3.0.1
            security:
              - BearerAuth: []            
            paths:
              '/things':
                get:
                  responses:
                    200:
                      description: OK

            components:      
              securitySchemes:
                BearerAuth:
                  type: http
                  scheme: bearer
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)
        assertThat(violations).hasSize(1)
    }

    @Test
    fun `Expect no violation, When Bearer global security scheme`() {
        @Language("YAML")
        val yaml = """
            openapi: 3.0.1
            security:
              - BearerAuth: ['linz-domain:full']
            paths:
              '/things':
                get:
                  responses:
                    200:
                      description: OK
              '/other-things':
                get:
                  responses:
                    200:
                      description: OK
            components:      
              securitySchemes:
                BearerAuth:
                  type: http
                  scheme: bearer
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)
        assertThat(violations).isEmpty()
    }
    @Test
    fun `Expect violaton, When scope linz-domain-full is not set for Bearer global security scheme then expect errors`() {
        @Language("YAML")
        val yaml = """
            openapi: 3.0.1
            security:
              - BearerAuth: ['some-scope:full']
            paths:
              '/things':
                get:
                  responses:
                    200:
                      description: OK
            components:      
              securitySchemes:
                BearerAuth:
                  type: http
                  scheme: bearer
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)
        assertThat(violations).hasSize(1)
    }

    @Test
    fun `Expect violaton, When Security scheme names don't match`() {
        @Language("YAML")
        val yaml = """
            openapi: 3.0.1
            security:
              - AnotherSecurityScheme: ['scope.execute']
            paths:
              '/things':
                get:
                  responses:
                    200:
                      description: OK
              '/other-things':
                get:
                  responses:
                    200:
                      description: OK
            components:      
              securitySchemes:
                BearerAuth:
                  type: http
                  scheme: bearer
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)
        assertThat(violations).hasSize(2)
    }
}
