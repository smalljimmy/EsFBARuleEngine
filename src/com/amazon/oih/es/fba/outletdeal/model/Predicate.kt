package com.amazon.oih.es.fba.outletdeal.model

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
sealed class Predicate {
    data class And(val predicates: List<Predicate>) : Predicate()
    data class Or(val predicates: List<Predicate>) : Predicate()
    data class Not(val predicate: Predicate) : Predicate()
    data class MarketplaceId(val marketplaceId: Long) : Predicate()
    data class Asin(val asin: String) : Predicate()
    data class MerchantCustomerId(val merchantCustomerId: Long) : Predicate()
}
