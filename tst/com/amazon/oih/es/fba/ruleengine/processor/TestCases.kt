package com.amazon.oih.es.fba.ruleengine.processor

import com.amazon.oih.es.fba.ruleengine.constants.ActionType
import com.amazon.oih.es.fba.ruleengine.entity.FilterLogicContext

object TestCases {
    val fbaTests = listOf(

            //active listing + no quality alert + healthyQty > availableQty
            FilterLogicContext.Builder(
                    id = 2L,
                    isActiveListing = true,
                    isQualityAlert = false,

                    totalQuantity = 5,
                    healthyQuantity = 6
            ).build() to ActionType.ImproveEconomicInputs,


            //active listing + no quality alert + healthyQty <= availableQty + markdownQty > 0
            //outlet eligible
            FilterLogicContext.Builder(
                    id = 3L,

                    merchantCustomerId = 39873047915L,
                    marketplaceId = 1L,
                    asin = "1452117349",

                    isActiveListing = true,
                    isQualityAlert = false,

                    totalQuantity = 10,
                    healthyQuantity = 5,
                    markdownQuantity = 5,

                    totalBuyboxPrice = 10.0.toBigDecimal(),
                    salesPrice = 10.0.toBigDecimal(),

                    averagePrice = 4.1.toBigDecimal(),
                    sellerRating = 4.0,
                    customerAverageReviewRating = 5.0,
                    unitsShipped = 2,
                    gl = 12,
                    isSafe = true,

                    markdownPrice = 4.0.toBigDecimal()
            ).build() to ActionType.OutletDeal,


            //active listing + no quality alert + healthyQty <= availableQty + markdownQty > 0
            //not outlet eligible  +  deadwood
            FilterLogicContext.Builder(
                    id = 4L,
                    isActiveListing = true,
                    isQualityAlert = false,

                    totalQuantity = 10,
                    healthyQuantity = 5,
                    markdownQuantity = 5,

                    salesPrice = 10.0.toBigDecimal(),
                    markdownPrice = 8.0.toBigDecimal(),

                    isDeadwood = true
            ).build() to ActionType.CreateASale,


            //active listing + no quality alert + healthyQty <= availableQty + markdownQty > 0
            //not deadwood + not low traffic
            FilterLogicContext.Builder(
                    id = 5L,
                    isActiveListing = true,
                    isQualityAlert = false,

                    totalQuantity = 10,
                    healthyQuantity = 5,
                    markdownQuantity = 5,

                    salesPrice = 10.0.toBigDecimal(),
                    markdownPrice = 8.0.toBigDecimal(),

                    isDeadwood = false,
                    isLowTraffic = false
            ).build() to ActionType.CreateASale,


            //active listing + no quality alert + healthyQty <= availableQty + markdownQty > 0
            //not deadwood + low traffic + over buybox eligible
            FilterLogicContext.Builder(
                    id = 6L,
                    isActiveListing = true,
                    isQualityAlert = false,

                    totalQuantity = 10,
                    healthyQuantity = 5,
                    markdownQuantity = 5,

                    markdownPrice = 8.0.toBigDecimal(),
                    totalBuyboxPrice = 10.0.toBigDecimal(),
                    salesPrice = 20.0.toBigDecimal(),

                    isDeadwood =  false,
                    isLowTraffic = true
            ).build() to ActionType.CreateASale,

            //active listing + no quality alert + healthyQty <= availableQty + markdownQty > 0
            //not outlet eligible + not deadwood + low traffic + over buybox  + not ads eligible
            FilterLogicContext.Builder(
                    id = 7L,
                    isActiveListing = true,
                    isQualityAlert = false,

                    totalQuantity = 10,
                    markdownPrice = 8.0.toBigDecimal(),
                    healthyQuantity = 5,
                    markdownQuantity = 5,
                    returnQuantity = 0,

                    totalBuyboxPrice = 10.0.toBigDecimal(),
                    salesPrice = 10.0.toBigDecimal(),

                    isDeadwood = false,
                    isLowTraffic = true
            ).build() to ActionType.ImproveKeywords,


            //active listing + no quality alert + healthyQty <= availableQty
            //markdown quantity = 0 + return quantity > 0
            FilterLogicContext.Builder(
                    id = 8L,
                    isActiveListing = true,
                    isQualityAlert = false,

                    totalQuantity = 10,
                    markdownQuantity = 0,
                    returnQuantity = 5
                    ).build() to ActionType.RemovalOrder
            )
}
