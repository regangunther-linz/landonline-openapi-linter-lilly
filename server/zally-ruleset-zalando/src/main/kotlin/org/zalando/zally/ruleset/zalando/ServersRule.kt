package org.zalando.zally.ruleset.zalando

import com.typesafe.config.Config
import io.swagger.v3.oas.models.OpenAPI
import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.servers.Server


@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "252",
    severity = Severity.MUST,
    title = "Servers"
)
class ServersRule(rulesConfig: Config) {

    //private val gatewayTargets = rulesConfig.getStringList("${javaClass.simpleName}.x-gateway-upstream-targets")
    private val description = "url doesn't pass regex"
    private val audienceExtension = "x-audience"

    //Assume URLS are public until audience is known
    var regex: Regex = """^https:\/\/public\.api(\.)?(\{env\}\.)?landonline\.govt\.nz\/v\d+\/[a-zA-Z]+(-[a-zA-Z]+)*$""".toRegex()

    @Check(severity = Severity.MUST)
    fun validate(context: Context): List<Violation> {
        val audience = context.api.info?.extensions?.get(audienceExtension)
        // If audience isn't public change the regex
        if (audience != "external-public") {
            regex = """^https?:\/\/api(\.)?(\{env\}\.)?landonline\.govt\.nz\/v\d+\/[a-zA-Z]+(-[a-zA-Z]+)*$""".toRegex()
        }
        val violations = violatingServers(context.api)
            .map {
                context.violation(description, it)
            }
        return violations
    }

    private fun violatingServers(api: OpenAPI): Collection<Server> =
        api.servers.orEmpty().filter { !regex.matches(it.url)
        }
}

