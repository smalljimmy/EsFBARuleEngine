package com.amazon.oih.es.fba.ruleengine.cache.source

import com.amazon.oih.es.fba.ruleengine.cache.model.Data
import io.reactivex.Observable


/**
 * Class to simulate Network DataSource
 */
class NetworkDataSource {
    val data: Observable<Data>
        get() = Observable.create { observer ->
            val data = Data()
            data.source = "network"
            observer.onNext(data)
            observer.onComplete()
        }

    fun get(configure: (Any?) -> Data) : Observable<Data> =
    Observable.create { observer ->
        val data = configure(null)
        data.source = "network"
        observer.onNext(data)
        observer.onComplete()
    }

}
