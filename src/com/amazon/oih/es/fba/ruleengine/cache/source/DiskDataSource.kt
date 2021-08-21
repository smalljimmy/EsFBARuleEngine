package com.amazon.oih.es.fba.ruleengine.cache.source

import com.amazon.oih.es.fba.ruleengine.cache.model.Data
import io.reactivex.Observable


/**
 * Class to simulate Disk DataSource
 */
class DiskDataSource {

    private var data: Data? = null

    fun getData(): Observable<Data> {
        return Observable.create { emitter ->
            if (data != null) {
                emitter.onNext(data!!)
            }
            emitter.onComplete()
        }
    }

    fun saveToDisk(data: Data) {
        this.data = data.clone()
        this.data!!.source = "disk"
    }

}
