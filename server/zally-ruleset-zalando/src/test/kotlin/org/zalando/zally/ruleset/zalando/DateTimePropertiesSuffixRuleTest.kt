@file:Suppress("YamlSchema")

package org.zalando.zally.ruleset.zalando

import com.typesafe.config.ConfigValueFactory
import org.zalando.zally.core.rulesConfig
import org.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class DateTimePropertiesSuffixRuleTest {

    private val rule = DateTimePropertiesSuffixRule(rulesConfig)

    @Test
    fun `rule should pass with correct 'date-time' fields`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Pet:
                  properties:
                    createdAt:
                      type: string
                      format: date-time
                    modifiedAt:
                      type: string
                      format: date-time                      
                    occurredAt:
                      type: string
                      format: date-time                      
                    returnedAt:
                      type: string
                      format: date-time
        """.trimIndent()
        val violations = rule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `rule should pass with correct 'date' fields`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Car:
                  properties:
                    createdAt:
                      type: string
                      format: date
                    modifiedAt:
                      type: string
                      format: date                      
                    occurredAt:
                      type: string
                      format: date                      
                    returnedAt:
                      type: string
                      format: date                  
        """.trimIndent()
        val violations = rule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should ignore fields with non-date and time types`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Car:
                  properties:
                    created:
                      type: string                      
                    occurred:
                      type: string                      
                    returned:
                      type: string                      
                    modified:
                      type: int                                          
        """.trimIndent()
        val violations = rule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `rule should fail to validate schema`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Car:
                  properties:
                    created:
                      type: string
                      format: date-time
                    occurred:
                      type: string
                      format: date
                    returned:
                      type: string
                      format: date-time
                    modified:
                      type: string
                      format: date
        """.trimIndent()
        val violations = rule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations.map { it.description }).containsExactly(
            rule.generateMessage("created", "string", "date-time"),
            rule.generateMessage("occurred", "string", "date"),
            rule.generateMessage("returned", "string", "date-time"),
            rule.generateMessage("modified", "string", "date")
        )
    }

    @Test
    fun `rule should support different patterns`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Car:
                  properties:
                    created:
                      type: string
                      format: date-time
                    modified:
                      type: string
                      format: date
        """.trimIndent()
        val newConfig = rulesConfig.withValue("DateTimePropertiesSuffixRule/patterns", ConfigValueFactory.fromIterable(listOf("was_.*")))
        val customRule = DateTimePropertiesSuffixRule(newConfig)
        val violations = customRule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations.map { it.description }).containsExactly(
            customRule.generateMessage("created", "string", "date-time"),
            customRule.generateMessage("modified", "string", "date")
        )
    }
}
