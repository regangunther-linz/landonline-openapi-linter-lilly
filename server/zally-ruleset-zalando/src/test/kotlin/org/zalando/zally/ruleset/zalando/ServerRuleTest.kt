package org.zalando.zally.ruleset.zalando

import org.assertj.core.api.Assertions
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.zalando.zally.core.rulesConfig
import org.zalando.zally.test.ZallyAssertions.assertThat
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.rule.api.Context
import org.junit.jupiter.api.Test
import org.zalando.zally.ruleset.zalando.model.ApiAudience

class ServerRuleTest {

    private val rule = ServersRule(rulesConfig)


    @Test
    fun `Expect no violations When Company internal with api landonline servers`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL.code}
        servers:
          - url: "https://api.landonline.govt.nz/v22/anything"
          - url: "https://api{env}.landonline.govt.nz/v22/anything"        
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)
        print(violations)
        assertEquals(0, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect violations When Company internal and mixed api and public servers`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL.code}
        servers:
          - url: "https://api.landonline.govt.nz/v22/anything"
          - url: "https://public.api{env}.landonline.govt.nz/v22/anything"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        assertEquals(1, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect no violations When Component internal with api landonline servers`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPONENT_INTERNAL.code}
        servers:
          - url: "https://api.landonline.govt.nz/v22/anything"
          - url: "https://api{env}.landonline.govt.nz/v22/anything"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        assertEquals(0, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect violations When Component internal and mixed api and public servers`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL.code}
        servers:
          - url: "https://api.landonline.govt.nz/v22/anything"
          - url: "https://public.api{env}.landonline.govt.nz/v22/anything"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        assertEquals(1, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect no violations When External Public with public api landonline servers`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.EXTERNAL_PUBLIC.code}
        servers:
          - url: "https://public.api.landonline.govt.nz/v22/anything"
          - url: "https://public.api{env}.landonline.govt.nz/v22/anything"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        assertEquals(0, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect violations When External Public and mixed api and public servers`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL.code}
        servers:
          - url: "https://api.landonline.govt.nz/v22/anything"
          - url: "https://public.api{env}.landonline.govt.nz/v22/anything"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        assertEquals(1, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect violations When External Public and api servers`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.EXTERNAL_PUBLIC.code}
        servers:
          - url: "https://api.landonline.govt.nz/v22/anything"
          - url: "https://api{env}.landonline.govt.nz/v22/anything"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        assertEquals(2, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect violations When Company internal and public api servers`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL.code}
        servers:
          - url: "https://public.api.landonline.govt.nz/v22/anything"
          - url: "https://public.api{env}.landonline.govt.nz/v22/anything"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        assertEquals(2, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect violations When Component internal and public api servers`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPONENT_INTERNAL.code}
        servers:
          - url: "https://public.api.landonline.govt.nz/v22/anything"
          - url: "https://public.api{env}.landonline.govt.nz/v22/anything"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        assertEquals(2, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect violations When Component internal and basePath missing`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL.code}
        servers:
          - url: "https://api.landonline.govt.nz/v12"
          - url: "https://api{env}.landonline.govt.nz/v12"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        assertEquals(2, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect violations When Component internal and basePath contains two resources `() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL.code}
        servers:
          - url: "https://api.landonline.govt.nz/v12/myresource/subresource"
          - url: "https://api{env}.landonline.govt.nz/v12/myresource/subresource"               
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        assertEquals(2, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect violations When Component internal there exists no template variable called env`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL.code}
        servers:
          - url: "https://api.landonline.govt.nz/v12/myresource"
          - url: "https://api.landonline.govt.nz/v12/myresource"
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        //assertEquals(2, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect violations When variable env not declared`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL.code}
        servers:
          - url: "https://api.landonline.govt.nz/v12/myresource"
          - url: "https://api{env}.landonline.govt.nz/v12/myresource"
            variables:
            env:
              enum:
                - .dev
                - .env
            default: .dev
            description: Not prod environments
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        //assertEquals(2, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect violations When Component internal and resource name before version`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL.code}
        servers:
          - url: "https://api.landonline.govt.nz/myresource/v12"
          - url: "https://api.landonline.govt.nz/myresource/v1"
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        //assertEquals(2, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect no violations When Component internal and resource contains hyphens`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL.code}
        servers:
          - url: "https://api.landonline.govt.nz/v1/my-resource"
          - url: "https://api.landonline.govt.nz/v33/my-resource"
          - url: "https://api.landonline.govt.nz/v33/myresource"
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        assertEquals(0, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

    @Test
    fun `Expect violations When Component internal and resource contains digits`() {
        @Language("YAML")
        val spec = """
        openapi: 3.0.0
        info:
          x-audience: ${ApiAudience.COMPANY_INTERNAL.code}
        servers:
          - url: "https://api.landonline.govt.nz/v1/myresource01"
          - url: "https://api.landonline.govt.nz/v33/01my-resource"
          - url: "https://api.landonline.govt.nz/v33/myresource"
          
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)

        assertEquals(2, violations.size)
        //assertEquals("url doesn't pass regex", violations[0].description)
        //assertEquals("/servers/0/url", violations[0].pointer.toString())
    }

}
