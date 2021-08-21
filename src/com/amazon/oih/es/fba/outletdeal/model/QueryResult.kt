package com.amazon.oih.es.fba.outletdeal.model

class QueryResult<T>(val results: List<T>, val totalResults: Long, val next: Long? = null) {
    init {
        require(totalResults >= results.size)
    }
}
