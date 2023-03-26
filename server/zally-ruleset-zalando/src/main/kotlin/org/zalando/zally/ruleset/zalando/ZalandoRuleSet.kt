package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.AbstractRuleSet
import java.net.URI

class ZalandoRuleSet : AbstractRuleSet() {

    override val url: URI = URI.create("https://linz.github.io/landonline-api-restful-guidelines/")
}
