package com.amazon.oih.es.fba.ruleengine.processor

import com.amazon.oih.es.fba.ruleengine.EsFBARuleEngineModule
import com.amazon.oih.es.fba.ruleengine.LOG
import com.amazon.oih.es.fba.ruleengine.constants.ActionType
import com.amazon.oih.es.fba.ruleengine.entity.FilterLogicContext
import com.amazon.oih.es.fba.ruleengine.util.AppConfigTestInitializer
import com.amazon.oih.es.fba.ruleengine.util.MetricExtension
import com.google.inject.Guice
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@ExtendWith(MetricExtension::class)
internal class RulesProcessorIntegrationTest {
    @Inject
    private lateinit var rulesProcessor: RulesProcessor

    private var injector = Guice.createInjector(
            AppConfigTestInitializer(),
            EsFBARuleEngineModule())

    @BeforeEach
    fun setup() {
        AppConfigTestInitializer.initialize()
        injector.injectMembers(this);
    }


    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    fun `test outlet deal`() {
        val timeElapsed = measureTimeMillis {
            runBlocking {
                val request =  FilterLogicContext.Builder(
                        id = 3L,

                        merchantCustomerId = 39873047915L,
                        marketplaceId = 1L,
                        asin = "1452117349",

                        isActiveListing = true,
                        isQualityAlert = false,

                        totalQuantity = 10,
                        healthyQuantity = 5,
                        markdownQuantity = 5,

                        salesPrice = 10.0.toBigDecimal(),
                        totalBuyboxPrice = 10.0.toBigDecimal(),
                        markdownPrice = 4.0.toBigDecimal()
                ).totalBuyboxPrice(10.0.toBigDecimal()).build()

                assertEquals(ActionType.OutletDeal, rulesProcessor.getRecommendation(request))
            }
        }

        LOG.info("total time: $timeElapsed ms")
    }
}
