package org.zalando.zally.ruleset.zalando

import org.assertj.core.api.Assertions
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.zalando.zally.core.rulesConfig
import org.zalando.zally.test.ZallyAssertions.assertThat
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.rule.api.Context
import org.junit.jupiter.api.Test

class ServerRuleTest {

    private val rule = ServersRule(rulesConfig)



    @Test
    fun `correct servers are correctly identified`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: "company-internal"
        servers:
          - url: "https://api.landonline.govt.nz/v22/anything"
          - url: "https://api{env}.landonline.govt.nz/v22/anything"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        //assertEquals(2, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

}
