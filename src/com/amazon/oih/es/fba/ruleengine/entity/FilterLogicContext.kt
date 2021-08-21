package com.amazon.oih.es.fba.ruleengine.entity

import com.amazon.oih.es.fba.ruleengine.LOG
import com.amazon.oih.es.fba.ruleengine.constants.ActionType
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CompletableDeferred
import org.kie.api.runtime.rule.FactHandle
import java.math.BigDecimal
import kotlin.random.Random
import kotlin.reflect.KMutableProperty1

class FilterLogicContext private constructor(
        val id: Long = Random.nextLong(),

        val merchantCustomerId: Long = 0,
        val asin: String = "",

        val marketplaceId: Long = 0,

        val msku: String? = "",

        val isLiquidationEligibility: Boolean = false,
        val isAggregatedView: Boolean = false,
        val isActiveListing: Boolean = false,
        val isLowTraffic: Boolean = false,
        val isLowConversion: Boolean = false,
        val isDeadwood: Boolean = false,
        val isQualityAlert: Boolean = false,
        val isOverstock: Boolean = false,
        val isOihLaunched: Boolean = false,

        //current price
        val salesPrice: BigDecimal = 0.toBigDecimal(),

        val totalBuyboxPrice: BigDecimal = 0.toBigDecimal(),

        val markdownPrice: BigDecimal =0.toBigDecimal(),

        val totalQuantity: Long = 0L,
        val markdownQuantity: Long = 0L,
        val healthyQuantity: Long = 0L,
        val returnQuantity: Long = 0L
){
    @JsonIgnore
    val getOutletDealEligibilityAsync = CompletableDeferred<Nothing?>()

    //fields retrieved from dependent services
    var averagePrice: BigDecimal? = null
    @JsonIgnore
    lateinit var averagePriceAsync: CompletableDeferred<BigDecimal?>

    var sellerRating: Double? = null
    @JsonIgnore
    lateinit var sellerRatingAsync: CompletableDeferred<Double?>

    var customerAverageReviewRating: Double? = null
    @JsonIgnore
    lateinit var customerAverageReviewRatingAsync: CompletableDeferred<Double?>

    var gl: Int? = null
    @JsonIgnore
    lateinit var glAsync: CompletableDeferred<Int?>

    var unitsShipped: Long? = null
    @JsonIgnore
    lateinit var unitsShippedAsync: CompletableDeferred<Long?>

    var safe: Boolean = false
    @JsonIgnore
    lateinit var isSafeAsync: CompletableDeferred<Boolean>

    var actionType: ActionType? = null
    @JsonIgnore
    lateinit var actionTypeAsync: CompletableDeferred<ActionType?>

    @JsonIgnore
    var factHandle: FactHandle? = null

    class Builder(
            var id: Long = Random.nextLong(),

            var merchantCustomerId: Long = 0,
            var asin: String = "",

            var marketplaceId: Long = 0,

            var msku: String? = "",

            var isLiquidationEligibility: Boolean = false,
            var isAggregatedView: Boolean = false,
            var isActiveListing: Boolean = false,
            var isLowTraffic: Boolean = false,
            var isLowConversion: Boolean = false,
            var isDeadwood: Boolean = false,
            var isQualityAlert: Boolean = false,
            var isOverstock: Boolean = false,
            var isOihLaunched: Boolean = false,

            //current price
            var salesPrice: BigDecimal = 0.toBigDecimal(),
            var totalBuyboxPrice: BigDecimal = 0.toBigDecimal(),

            var markdownPrice: BigDecimal = 0.toBigDecimal(),

            var totalQuantity: Long = 0L,
            var markdownQuantity: Long = 0L,
            var healthyQuantity: Long = 0L,
            var returnQuantity: Long = 0L,

            //fields dependent on service call
            var sellerRating: Double? = null,
            var averagePrice: BigDecimal? = null,
            var customerAverageReviewRating: Double? = null,
            var gl: Int? = null,
            var unitsShipped: Long? = null,
            var isSafe: Boolean = false,

            var actionType: ActionType? = null
    ) {
        fun id(id: Long) = apply { this.id = id }
        fun merchantCustomerId(merchantCustomerId: Long) = apply { this.merchantCustomerId = merchantCustomerId }
        fun asin(asin: String) = apply { this.asin = asin }
        fun marketplaceId(marketplaceId: Long) = apply { this.marketplaceId = marketplaceId }
        fun msku(msku: String) = apply { this.msku = msku }


        fun isLiquidationEligibility(isLiquidationEligibility: Boolean) = apply { this.isLiquidationEligibility = isLiquidationEligibility }
        fun isAggregatedView(isAggregatedView: Boolean) = apply { this.isAggregatedView = isAggregatedView }
        fun isActiveListing(isActiveListing: Boolean) = apply { this.isActiveListing = isActiveListing }
        fun isLowTraffic(isLowTraffic: Boolean) = apply { this.isLowTraffic = isLowTraffic }
        fun isLowConversion(isLowConversion: Boolean) = apply { this.isLowConversion = isLowConversion }
        fun isDeadwood(isDeadwood: Boolean) = apply { this.isDeadwood = isDeadwood }
        fun isQualityAlert(isQualityAlert: Boolean) = apply { this.isQualityAlert = isQualityAlert }
        fun isOverstock(isOverstock: Boolean) = apply { this.isOverstock = isOverstock }
        fun isOihLaunched(isOihLaunched: Boolean) = apply { this.isOihLaunched = isOihLaunched }


        fun salesPrice(salesPrice: BigDecimal) = apply { this.salesPrice = salesPrice }
        fun totalBuyboxPrice(totalBuyboxPrice: BigDecimal) = apply { this.totalBuyboxPrice = totalBuyboxPrice }
        fun averagePrice(averagePrice: BigDecimal) = apply { this.averagePrice = averagePrice }


        fun markdownPrice(markdownPrice: BigDecimal) = apply { this.markdownPrice = markdownPrice }

        fun totalQuantity(totalQuantity: Long) = apply { this.totalQuantity = totalQuantity }
        fun markdownQuantity(markdownQuantity: Long) = apply { this.markdownQuantity = markdownQuantity }
        fun healthyQuantity(healthyQuantity: Long) = apply { this.healthyQuantity = healthyQuantity }
        fun returnQuantity(returnQuantity: Long) = apply { this.returnQuantity = returnQuantity }

        fun sellerRating(sellerRating: Double) = apply { this.sellerRating = sellerRating }
        fun customerAverageReviewRating(customerAverageReviewRating: Double) = apply { this.customerAverageReviewRating = customerAverageReviewRating }
        fun gl(gl: Int) = apply { this.gl = gl }
        fun unitsShipped(unitsShipped: Long) = apply { this.unitsShipped = unitsShipped }
        fun isSafe(isSafe: Boolean) = apply { this.isSafe = isSafe }


        private inline fun <R : Any, reified T> toAsync(instance: R, prop: KMutableProperty1<R, T>): CompletableDeferred<T> {
            val value = prop.get(instance)

            var result: CompletableDeferred<T> = CompletableDeferred()
            value?.let {
                result = CompletableDeferred(value)
            }

            result.invokeOnCompletion {
                if (result.isCompleted) {
                    prop.set(instance, result.getCompleted())
                }
            }

            return result
        }

        fun build(): FilterLogicContext {
            val filterLogicContext = FilterLogicContext(
                    this.id,
                    this.merchantCustomerId,
                    this.asin,
                    this.marketplaceId,
                    this.msku,
                    this.isLiquidationEligibility,
                    this.isAggregatedView,
                    this.isActiveListing,
                    this.isLowTraffic,
                    this.isLowConversion,
                    this.isDeadwood,
                    this.isQualityAlert,
                    this.isOverstock,
                    this.isOihLaunched,
                    this.salesPrice,
                    this.totalBuyboxPrice,
                    this.markdownPrice,
                    this.totalQuantity,
                    this.markdownQuantity,
                    this.healthyQuantity,
                    this.returnQuantity
            )

            filterLogicContext.let {
                it.averagePrice = this.averagePrice
                it.averagePriceAsync = this.toAsync(filterLogicContext, FilterLogicContext::averagePrice)
                it.sellerRating = this.sellerRating
                it.sellerRatingAsync = this.toAsync(filterLogicContext, FilterLogicContext::sellerRating)
                it.customerAverageReviewRating = this.customerAverageReviewRating
                it.customerAverageReviewRatingAsync = this.toAsync(filterLogicContext, FilterLogicContext::customerAverageReviewRating)
                it.gl = this.gl
                it.glAsync = this.toAsync(filterLogicContext, FilterLogicContext::gl)
                it.unitsShipped = this.unitsShipped
                it.unitsShippedAsync = this.toAsync(filterLogicContext, FilterLogicContext::unitsShipped)
                it.safe = this.isSafe
                it.isSafeAsync = this.toAsync(filterLogicContext, FilterLogicContext::safe)

                it.actionType = this.actionType
                it.actionTypeAsync = this.toAsync(filterLogicContext, FilterLogicContext::actionType)
            }

            return filterLogicContext

        }
    }

    fun toJson(): String {
        return try {
            val mapper = ObjectMapper()
            mapper.writeValueAsString(this)
        } catch (e: Exception) {
            LOG.error("Fail to convert to Json", e)
            ""
        }
    }

    override fun toString(): String = toJson()
}
