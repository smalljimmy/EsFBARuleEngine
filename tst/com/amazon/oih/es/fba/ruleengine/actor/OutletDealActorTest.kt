package com.amazon.oih.es.fba.ruleengine.actor

import com.amazon.oih.es.fba.outletdeal.dao.ESAccessor
import com.amazon.oih.es.fba.ruleengine.cache.source.DataSource
import com.amazon.oih.es.fba.ruleengine.entity.DroolsMessage
import com.amazon.oih.es.fba.ruleengine.entity.FilterLogicContext
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.mockk.mockk

internal class OutletDealActorTest: StringSpec({
    val esAccessor: ESAccessor = mockk(relaxed = true)
    val dataSource: DataSource = mockk()

    val validMessage: DroolsMessage.GetOutletEligibility = DroolsMessage.GetOutletEligibility(FilterLogicContext.Builder().build())

    val invalidMessage: DroolsMessage.GetTotalBuyboxPrice = mockk()

    val outletDealActor = OutletDealActor(esAccessor, dataSource)

    "handle outlet message should work" {
        outletDealActor.handleMessage(validMessage)
    }

    "handle other messages should throw exception" {
        shouldThrow<IllegalArgumentException> {
            outletDealActor.handleMessage(invalidMessage)
        }
    }

})
