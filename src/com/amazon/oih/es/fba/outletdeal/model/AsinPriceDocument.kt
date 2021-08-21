package com.amazon.oih.es.fba.outletdeal.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.*

data class AsinPriceDocument (
        @JsonProperty("marketplace_id")
        val marketplaceId: Long?,

        @JsonProperty("asin")
        val asin: String,

        @JsonProperty("our_price")
        val price: BigDecimal?,

        @JsonFormat
        (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        val timestamp: Date
)
