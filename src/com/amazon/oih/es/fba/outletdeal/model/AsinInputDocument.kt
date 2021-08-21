package com.amazon.oih.es.fba.outletdeal.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class AsinInputDocument (
        @JsonProperty("region_id")
        val regionId: Long,

        @JsonProperty("marketplace_id")
        val marketplaceId: Long,

        @JsonProperty("asin")
        val asin: String,

        @JsonProperty("shipped_units")
        val shippedUnits: Long?,

        @JsonProperty("customer_average_review_rating")
        val customerAverageReviewRating: Double?,

        @JsonProperty("gl_product_group")
        val gl: Int?,

        @JsonFormat
        (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        val timestamp: Date
)
