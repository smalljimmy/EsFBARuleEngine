package com.amazon.oih.es.fba.ruleengine.util

import com.amazon.oih.es.fba.ruleengine.constants.ActionType
import com.amazon.oih.es.fba.ruleengine.constants.BUYBOX_LOWER_THRESHOLD
import com.amazon.oih.es.fba.ruleengine.constants.BUYBOX_UPPER_THRESHOLD
import com.amazon.oih.es.fba.ruleengine.entity.FilterLogicContext
import com.amazon.oih.es.fba.ruleengine.processor.RulesProcessor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal

class ICPActionUtils(private val ruleProcessor: RulesProcessor) {
    private fun getCurrentPrice(context: FilterLogicContext) =
            context.salesPrice


    fun isWithinPriceInBuyboxThreshold(context: FilterLogicContext): Boolean = runBlocking {
        val buyboxPrice = context.totalBuyboxPrice
        val price = getCurrentPrice(context)
        return@runBlocking (price > buyboxPrice.times(BigDecimal(BUYBOX_LOWER_THRESHOLD))
                && price < buyboxPrice.times(BigDecimal(BUYBOX_UPPER_THRESHOLD)))

    }

    fun shouldShowCreateSaleAction(context: FilterLogicContext): Boolean {
        fun isOIHMarkdownQuantityGT0(context: FilterLogicContext) =
                context.markdownQuantity > 0


        fun isOIHRecommendedPriceLTCurrentPrice(context: FilterLogicContext) =
                context.markdownPrice < getCurrentPrice(context)


        return isOIHMarkdownQuantityGT0(context) && isOIHRecommendedPriceLTCurrentPrice(context)
    }

    fun isUnhealthyQuantityLE0(context: FilterLogicContext): Boolean {
        fun getUnhealthyQuantity(context: FilterLogicContext): Long {
            val unhealthy = (context.totalQuantity) - (context.healthyQuantity)
            return if (unhealthy > 0) unhealthy else 0
        }

        return getUnhealthyQuantity(context) <= 0
    }

    fun shouldShowCreateRemovalActionOIH(context: FilterLogicContext): Boolean {
        fun getReturnQuantity(context: FilterLogicContext) = context.returnQuantity

        fun shouldShowCreateRemovalAction(context: FilterLogicContext) = context.totalQuantity > 0

        return shouldShowCreateRemovalAction(context) && getReturnQuantity(context) > 0
    }

    fun setActionType(context: FilterLogicContext, actionType: ActionType) {
        context.actionTypeAsync.complete(actionType)
    }

    fun shouldCheckOutletEligibility(context: FilterLogicContext): Boolean {
        return context.getOutletDealEligibilityAsync.isActive
    }

    fun doOutletEligibleCheckAsync(context: FilterLogicContext) =  GlobalScope.async {
        ruleProcessor.getOutletEligibilityAsync(context)
        context.getOutletDealEligibilityAsync.await()
        ruleProcessor.reevaluateAsync(context)
    }
}
