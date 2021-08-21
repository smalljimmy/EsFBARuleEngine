package com.amazon.oih.es.fba.outletdeal.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class SellerRatingDocument (
        @JsonProperty("marketplace_id")
        val marketplaceId: Long?,

        @JsonProperty("customer_id")
        val customerId: Long?,

        @JsonProperty("merchant_customer_id")
        val merchantCustomerId: Long?,

        @JsonProperty("star_rating")
        val starRating: Double?,

        @JsonFormat
        (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        val timestamp: Date
)
