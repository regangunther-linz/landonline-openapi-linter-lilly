package org.zalando.zally.ruleset.zalando

import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "135",
    severity = Severity.SHOULD,
    title = "Should not contain API in url"
)
class URLShouldNotContainAPI {

    @Check(severity = Severity.MUST)
    fun validate(context: Context): List<Violation>? =
        context.api.servers.orEmpty().filter { it.url != null && it.url.matches("^(http|https)://.*/api.*$".toRegex()) }
            .map { context.violation("URL shouldn't contain /api", it) }
}
