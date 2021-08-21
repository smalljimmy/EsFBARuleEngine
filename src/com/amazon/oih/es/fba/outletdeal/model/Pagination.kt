package com.amazon.oih.es.fba.outletdeal.model

data class Pagination private constructor(val from: Long, val size: Long) {
    companion object {
        fun from(from: Long) = Partial(from)
    }

    data class Partial(private val from: Long) {
        fun size(size: Long) = Pagination(from, size)
        fun to(to: Long) = Pagination(from, to - from)
    }
}
