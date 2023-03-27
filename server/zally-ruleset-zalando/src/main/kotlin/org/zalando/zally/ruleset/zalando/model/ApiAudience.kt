package org.zalando.zally.ruleset.zalando.model

enum class ApiAudience(val code: String) {
    EXTERNAL_PUBLIC("external-public"),
    COMPANY_INTERNAL("company-internal"),
    COMPONENT_INTERNAL("component-internal");

    companion object {
        fun parse(code: String): ApiAudience =
            values().find { it.code == code }
                ?: throw UnsupportedAudienceException(code)
    }
}

class UnsupportedAudienceException(val code: String) : Exception("API audience $code is not supported.")
