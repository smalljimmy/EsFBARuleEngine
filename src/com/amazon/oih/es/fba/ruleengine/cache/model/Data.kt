package com.amazon.oih.es.fba.ruleengine.cache.model

class Data {

    var source: String? = null

    var value: Any? = null

    fun clone(): Data {
        return Data()
    }
}
