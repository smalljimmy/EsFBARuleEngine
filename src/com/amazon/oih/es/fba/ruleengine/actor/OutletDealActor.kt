package com.amazon.oih.es.fba.ruleengine.actor

import com.amazon.oih.es.fba.outletdeal.dao.ESAccessor
import com.amazon.oih.es.fba.ruleengine.LOG
import com.amazon.oih.es.fba.ruleengine.OUTLET_DEAL_ACTOR_NUMBER
import com.amazon.oih.es.fba.ruleengine.cache.source.DataSource
import com.amazon.oih.es.fba.ruleengine.debug2
import com.amazon.oih.es.fba.ruleengine.entity.DroolsMessage
import com.amazon.oih.es.fba.ruleengine.error2
import com.amazon.oih.es.fba.ruleengine.metrics.record
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import javax.inject.Inject

class OutletDealActor @Inject constructor(private val ESAccessor: ESAccessor, val dataSource: DataSource) : AbstractActor(OUTLET_DEAL_ACTOR_NUMBER) {

    override suspend fun handleMessage(msg: DroolsMessage) {
        when (msg) {
            is DroolsMessage.GetOutletEligibility -> {
                val request = msg.request

                try {
                    val sellerRatingAsync = async (
                        CoroutineExceptionHandler { _, throwable ->
                            LOG.error2("Catch: $throwable", throwable)
                            record("EsFBARuleEngine.Failure", 1.0, "OutletDealActor.handleMessage")

                            request.sellerRatingAsync.complete(0.0)
                        }
                    )
                    {
                        ESAccessor.getSellerRating(request.marketplaceId, request.merchantCustomerId)
                    }

                    val asinInputAsync = async (
                        CoroutineExceptionHandler { _, throwable ->
                            LOG.error2("Catch: $throwable", throwable)
                            record("EsFBARuleEngine.Failure", 1.0, "OutletDealActor.asinInputAsync")

                            request.customerAverageReviewRatingAsync.complete(0.0)
                            request.glAsync.complete(0)
                            request.unitsShippedAsync.complete(0)

                        }
                    )
                    {
                        ESAccessor.getAsinInput(request.marketplaceId, request.asin)
                    }

                    val asinPriceAsync = async  (
                        CoroutineExceptionHandler { _, throwable ->
                            LOG.error2("Catch: $throwable", throwable)
                            record("EsFBARuleEngine.Failure", 1.0, "OutletDealActor.asinPriceAsync")

                            request.averagePriceAsync.complete(0.toBigDecimal())
                        }
                    )
                    {
                        ESAccessor.getAsinPrice(request.marketplaceId, request.asin)
                    }

                    val asinItemSafetyAsync = async (
                            CoroutineExceptionHandler { _, throwable ->
                                LOG.error2("Catch: $throwable", throwable)
                                record("EsFBARuleEngine.Failure", 1.0, "OutletDealActor.itemSafetyAsync")

                                request.isSafeAsync.complete(false)
                            }
                    )
                    {
                        ESAccessor.getAsinItemSafety(request.marketplaceId, request.asin)
                    }

                    LOG.debug2("""
                    |${sellerRatingAsync.await()}
                    |${asinInputAsync.await()}
                    |${asinPriceAsync.await()}
                    |${asinItemSafetyAsync.await()}
                    """.trimMargin())


                    request.sellerRatingAsync.complete(sellerRatingAsync.await()?.starRating?:0.0)

                    request.customerAverageReviewRatingAsync.complete(asinInputAsync.await()?.customerAverageReviewRating?:0.0)
                    request.glAsync.complete(asinInputAsync.await()?.gl?:0)
                    request.unitsShippedAsync.complete(asinInputAsync.await()?.shippedUnits?:0)

                    request.averagePriceAsync.complete(asinPriceAsync.await()?.price?:0.toBigDecimal())

                    request.isSafeAsync.complete("safe".equals(asinItemSafetyAsync.await()?.status))
                } finally {
                    request.getOutletDealEligibilityAsync.complete(null)
                }
            }

            else -> throw IllegalArgumentException("Can't accept $msg")
        }
    }
}
