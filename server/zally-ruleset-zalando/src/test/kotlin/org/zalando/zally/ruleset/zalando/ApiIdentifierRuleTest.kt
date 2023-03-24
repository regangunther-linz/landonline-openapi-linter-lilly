package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.rule.api.Context
import org.zalando.zally.test.ZallyAssertions.assertThat
import org.junit.jupiter.api.Test

class ApiIdentifierRuleTest {

    private val rule = ApiIdentifierRule()

    @Test
    fun correctPublicApiIdIsSet() {
        val context = withApiId("public-titles-v1")

        val violation = rule.validate(context)

        assertThat(violation)
            .isNull()
    }
    @Test
    fun correctInternalApiIdIsSet() {
        val context = withApiId("internal-titles-v1")

        val violation = rule.validate(context)

        assertThat(violation)
            .isNull()
    }
    @Test
    fun incorrectPartnerApiIdIsSet() {
        val context = withApiId("partner-titles-v1")

        val violation = rule.validate(context)

        assertThat(violation)
            .pointerEqualTo("/info/x-api-id")
            .descriptionMatches(".*doesn't match.*")
    }

    @Test
    fun incorrectResourceLengthApiIdIsSet() {
        val context = withApiId("partner-api-v1")

        val violation = rule.validate(context)

        assertThat(violation)
            .pointerEqualTo("/info/x-api-id")
            .descriptionMatches(".*doesn't match.*")
    }

    @Test
    fun incorrectVersion() {
        val context = withApiId("partner-api-v")

        val violation = rule.validate(context)

        assertThat(violation)
            .pointerEqualTo("/info/x-api-id")
            .descriptionMatches(".*doesn't match.*")
    }

    @Test
    fun incorrectVersionLengthLengthApiIdIsSet() {
        val context = withApiId("partner-api-v123")

        val violation = rule.validate(context)

        assertThat(violation)
            .pointerEqualTo("/info/x-api-id")
            .descriptionMatches(".*doesn't match.*")
    }

    @Test
    fun incorrectNoVersion() {
        val context = withApiId("partner-api")

        val violation = rule.validate(context)

        assertThat(violation)
            .pointerEqualTo("/info/x-api-id")
            .descriptionMatches(".*doesn't match.*")
    }

    @Test
    fun incorrectOnlyTwoElements() {
        val context = withApiId("partner-v1")

        val violation = rule.validate(context)

        assertThat(violation)
            .pointerEqualTo("/info/x-api-id")
            .descriptionMatches(".*doesn't match.*")
    }

    @Test
    fun incorrectOnlyOneElement() {
        val context = withApiId("partner")

        val violation = rule.validate(context)

        assertThat(violation)
            .pointerEqualTo("/info/x-api-id")
            .descriptionMatches(".*doesn't match.*")
    }

    @Test
    fun incorrectCharactersUsed() {
        val context = withApiId("This?iS//some|Incorrect+&ApI)(id!!!")
        val violation = rule.validate(context)!!

        assertThat(violation)
            .pointerEqualTo("/info/x-api-id")
            .descriptionMatches(".*doesn't match.*")
    }

    @Test
    fun noApiIdIsSet() {
        val context = withApiId("null")
        val violation = rule.validate(context)!!

        assertThat(violation)
            .pointerEqualTo("/info/x-api-id")
            .descriptionMatches(".*should be provided.*")
    }

    private fun withApiId(apiId: String): Context {
        val content = """
            openapi: '3.0.0'
            info:
              x-api-id: $apiId
            paths: {}
        """.trimIndent()

        return DefaultContextFactory().getOpenApiContext(content)
    }
}
