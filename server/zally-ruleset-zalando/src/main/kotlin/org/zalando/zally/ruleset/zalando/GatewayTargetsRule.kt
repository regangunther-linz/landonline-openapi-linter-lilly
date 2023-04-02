package org.zalando.zally.ruleset.zalando

import com.typesafe.config.Config
import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "251",
    severity = Severity.MUST,
    title = "Provide x-gateway-upstream-targets"
)

class GatewayTargetsRule(rulesConfig: Config) {

    private val gatewayTargets = rulesConfig.getStringList("${javaClass.simpleName}.x-gateway-upstream-targets")
    private val extensionName = "x-gateway-upstream-targets"
    private val extensionPointer = "/$extensionName".toJsonPointer()
    private val extensionMissingDescription = "x-gateway-upstream-targets extension missing"
    private val extensionMissingTargetsDescription = "x-gateway-upstream-targets is missing the expected targets"

    @Check(severity = Severity.MUST)
    fun validateExtension(context: Context): Violation? {
        val extension = context.api.extensions?.get(extensionName) as? Map<*, *>
        if (extension === null) {
            return context.violation(extensionMissingDescription, extensionPointer)
        } else {
            val checkKeys = gatewayTargets.all { extension.containsKey(it) }
            if (checkKeys) {
                return null
            } else {
                return context.violation(extensionMissingTargetsDescription, extensionPointer)
            }
        }
    }
}
