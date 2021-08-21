package com.amazon.oih.es.fba.outletdeal.model

enum class SortField(val fieldName: String) {
    MARKETPLACE_ID("marketplace_id"),
    CUSTOMER_ID("customer_id"),
    MERCHANT_CUSTOMER_ID("merchant_customer_id"),
    ASIN("asin"),
    PRICE("out_price"),
    START_RATING("star_rating"),
    TIMESTAMP("timestamp")
}
