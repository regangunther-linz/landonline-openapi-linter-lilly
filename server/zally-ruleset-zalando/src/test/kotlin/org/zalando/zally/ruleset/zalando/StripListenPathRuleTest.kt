package org.zalando.zally.ruleset.zalando

import org.junit.jupiter.api.Test
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.rule.api.Context
import org.zalando.zally.test.ZallyAssertions

class StripListenPathRuleTest {

    private val rule = StripListenPathRule()

    @Test
    fun pass_whenStripIsFalse() {
        val context = withValue("false")

        val violation = rule.validate(context)

        ZallyAssertions.assertThat(violation)
            .isNull()
    }
    @Test
    fun pass_whenStripIsTruePass() {
        val context = withValue("true")

        val violation = rule.validate(context)

        ZallyAssertions.assertThat(violation)
            .isNull()
    }
    @Test
    fun pass_whenAbsent() {
        val context = withValue()

        val violation = rule.validate(context)

        ZallyAssertions.assertThat(violation)
            .isNull()
    }
    @Test
    fun fail_whenStripIsNotBoolean() {
        val context = withValue("sure-strip-path")

        val violation = rule.validate(context)

        ZallyAssertions.assertThat(violation)
            .pointerEqualTo("/x-gateway-strip-listen-path")
            .descriptionMatches("Strip listen path must be a boolean")
    }

    private fun withValue(stripValue: String? = null): Context {
        var stripString = ""
        if (stripValue != null) {
            stripString = "x-gateway-strip-listen-path: $stripValue"
        }

        val content = """
            openapi: '3.0.0'
            info:
              title: Lorem Ipsum
              version: 1.0.0
            paths: {}
            $stripString
        """.trimIndent()

        return DefaultContextFactory().getOpenApiContext(content)
    }
}
