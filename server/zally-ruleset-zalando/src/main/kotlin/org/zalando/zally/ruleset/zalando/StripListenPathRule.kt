package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "250",
    severity = Severity.MAY,
    title = "Strip Gateway listen Path"
)
class StripListenPathRule() {

    private val invalidType = "Strip listen path must be a boolean"
    private val extensionName = "x-gateway-strip-listen-path"
    private val extensionPointer = "/$extensionName".toJsonPointer()

    @Check(severity = Severity.MUST)
    fun validate(context: Context): Violation? {
        val stripValue = context.api.extensions?.get(extensionName)

        return when (stripValue) {
            is Boolean , null -> null
            else -> context.violation(invalidType, extensionPointer)
        }
    }
}

