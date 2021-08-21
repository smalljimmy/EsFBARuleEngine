package com.amazon.oih.es.fba.ruleengine.entity

import com.amazon.oih.es.fba.ruleengine.processor.RulesProcessor

sealed class DroolsMessage {
    abstract val request: Any

    //KIE
    data class GetRecommendation(override val request: FilterLogicContext) : DroolsMessage()
    data class SetRuleProcessor(override val request: RulesProcessor) : DroolsMessage()
    data class Retract(override val request: FilterLogicContext) : DroolsMessage()
    data class Reevaluate(override val request: FilterLogicContext) : DroolsMessage()

    //AmazonApi
    data class GetTotalBuyboxPrice(override val request: FilterLogicContext) : DroolsMessage()

    //OutletDeal
    data class GetOutletEligibility(override val request: FilterLogicContext) : DroolsMessage()
}

