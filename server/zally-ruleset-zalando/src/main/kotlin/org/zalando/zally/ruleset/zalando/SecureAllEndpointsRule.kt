package org.zalando.zally.ruleset.zalando

import com.typesafe.config.Config
import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.core.util.allFlows
import org.zalando.zally.core.util.isBearer
import org.zalando.zally.core.util.isOAuth2
import org.zalando.zally.rule.api.*

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "104",
    severity = Severity.MUST,
    title = "Secure Endpoints"
 )
class SecureAllEndpointsRule(rulesConfig: Config) {

    private val protectedAudiences = rulesConfig.getStringList("${javaClass.simpleName}.protectedAudiences").toSet()
    private val audience = "x-audience"
    @Check(severity = Severity.MUST)
    fun checkHasValidSecuritySchemes(context: Context): Violation? {
        val isProtectedAudience = protectedAudiences.contains(context.api.info?.extensions?.get(audience))
        if (!isProtectedAudience) return null

        val valid = context.api.components?.securitySchemes?.values?.filter {
            it.isBearer()
        }.orEmpty()

        return if (valid.isEmpty()) context.violation(
            "API must be secured by Bearer Authentication",
            "/components/securitySchemes".toJsonPointer()
        ) else null
    }

    @Check(severity = Severity.MUST)
    fun checkHasNoInvalidSecuritySchemes(context: Context): List<Violation> =
        context.api.components?.securitySchemes?.values?.filterNot {
            it.isBearer()
        }?.map {
            context.violation("API must be secured by Bearer Authentication", it)
        }.orEmpty()


    @Check(severity = Severity.MUST)
    fun checkUsedScopesAreSpecified(context: Context): List<Violation> {
        if (!context.isOpenAPI3()) return emptyList()

        val specifiedSchemes = context.api.components?.securitySchemes?.entries.orEmpty()
            .map { (group, scheme) -> group to scheme }.toMap()

        val specifiedScopes = context.api.components?.securitySchemes?.entries.orEmpty()
            .filter { (_, scheme) -> scheme.isOAuth2() }
            .flatMap { (group, scheme) ->
                scheme.allFlows().flatMap { flow ->
                    flow.scopes?.keys.orEmpty().map { scope -> group to scope }
                }
            }.toSet()

        val usedScopes = context.api.paths?.values.orEmpty()
            .flatMap {
                it?.readOperations().orEmpty().flatMap { it.security.orEmpty() }
            }
            .flatMap { secReq ->
                secReq.keys.flatMap { group ->
                    secReq[group].orEmpty().map { scope -> group to scope }
                }
            }

        return usedScopes
            .filter { (group, _) -> specifiedSchemes.get(group)?.isOAuth2() ?: false }
            .filterNot { it in specifiedScopes }.map { (group, scope) ->
                context.violation(
                    "The scope '$group/$scope' is not specified in security definition", scope
                )
            }
    }
}
