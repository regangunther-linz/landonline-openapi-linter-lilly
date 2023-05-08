package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.core.rulesConfig

class GatewayTargetsRuleTest {

    private val rule = GatewayTargetsRule(rulesConfig)

    @Test
    fun `should pass when x-gateway-upstream-targets is present and contains all allowed targets`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.0
            x-gateway-upstream-targets:
              prod: http://httpbin.org/
              dev: http://httpbin.org/
              env: http://httpbin.org/
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)
        val violation = rule.validateExtension(context)
        assertThat(violation).isNull()
    }

    @Test
    fun `should fail when x-gateway-upstream-targets is missing`() {
        @Language("YAML")
        val content = """
                openapi: 3.0.0
                info:
                  title: Lorem Ipsum
                  version: 1.0.0
                paths: {} 
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)
        val violation = rule.validateExtension(context)
        assertThat(violation).describedAs("x-gateway-upstream-targets extension missing")
    }

    @Test
    fun `should fail when x-gateway-upstream-targets is missing targets`() {
        @Language("YAML")
        val content = """
                openapi: 3.0.0
                info:
                  title: Lorem Ipsum
                  version: 1.0.0
                paths: {}
                x-gateway-upstream-targets:
                  prod: http://httpbin.org/
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)
        val violation = rule.validateExtension(context)
        assertThat(violation).describedAs("x-gateway-upstream-targets is missing the expected targets")
    }
}
