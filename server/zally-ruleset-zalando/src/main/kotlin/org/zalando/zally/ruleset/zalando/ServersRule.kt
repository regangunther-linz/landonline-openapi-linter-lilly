package org.zalando.zally.ruleset.zalando

import com.typesafe.config.Config
import io.swagger.v3.oas.models.OpenAPI
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.servers.ServerVariable
import io.swagger.v3.oas.models.servers.ServerVariables

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "252",
    severity = Severity.MUST,
    title = "Servers"
)
class ServersRule(rulesConfig: Config) {

    // Assume URLS are public until audience is known
    var regex: Regex = """^https:\/\/public\.api(\.)?(\{env\}\.)?landonline\.govt\.nz\/v\d+\/[a-z]+(-[a-z]+)*s$""".toRegex()

    private val descriptionEnv = "missing env variable for template"
    private val descriptionTemplateError = "servers must contain one templated url containing {env}"
    private val audienceExtension = "x-audience"

    @Check(severity = Severity.MUST)
    fun validate(context: Context): List<Violation> {
        val violations = violatingServers(context)

        return violations
    }

    private fun violatingServers(context: Context): List<Violation> {
        val api = context.api
        val audience = api.info?.extensions?.get(audienceExtension)
        // If audience isn't public change the regex
        if (audience != "external-public") {
            regex = """^https?:\/\/api(\.)?(\{env\}\.)?landonline\.govt\.nz\/v\d+\/[a-z]+(-[a-z]+)*s$""".toRegex()
        }
        val description = "url doesn't pass regex $regex"
        var filteredByUrl = api.servers.orEmpty().filter {
            it.url != null && !regex.matches(it.url)
        }.toMutableList().map {
            context.violation(description, it)
        }
        return if (filteredByUrl.size !== 0) {
            filteredByUrl
        } else {
            var missingEnv = api.servers.orEmpty().filter {
                it.url != null && it.url.contains("{env}")
            }.toMutableList()
            return if (missingEnv.size == 0) {
                listOf(context.violation(descriptionTemplateError, api.servers))
            } else { emptyList() }
        }
    }

    fun validateEnv(context: Context): List<Violation> {
        val envViolations = violatingServersByVariables(context.api)
            .map {
                context.violation(descriptionEnv, it)
            }
        return envViolations
    }

    private fun violatingServersByVariables(api: OpenAPI): Collection<Server> {
        val returnServers = mutableListOf<Server>()
        for (server in api.servers.orEmpty()) {
            if (server.url != null && server.url.contains("{env}")) {
                var expectedVariable = ServerVariables()
                var env = ServerVariable()
                env.enum = listOf(".dev", ".env")
                env.default = ".dev"
                expectedVariable.addServerVariable("env", env)
                if (!expectedVariable.equals(server.variables))
                    returnServers.add(server)
            }
        }
        return returnServers
    }
}
