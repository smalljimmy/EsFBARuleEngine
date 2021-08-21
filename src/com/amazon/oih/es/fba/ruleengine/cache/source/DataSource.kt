package com.amazon.oih.es.fba.ruleengine.cache.source

import com.amazon.oih.es.fba.ruleengine.cache.model.Data
import io.reactivex.Observable

/**
 * The DataSource to handle 3 data sources - memory, disk, network
 */
class DataSource(private val memoryDataSource: MemoryDataSource,
                 private val diskDataSource: DiskDataSource,
                 private val networkDataSource: NetworkDataSource) {

    val dataFromMemory: Observable<Data>
        get() = memoryDataSource.getData()

    val dataFromDisk: Observable<Data>
        get() = diskDataSource.getData().doOnNext { data -> memoryDataSource.cacheInMemory(data) }

    val dataFromNetwork: Observable<Data>
        get() = networkDataSource.data.doOnNext { data ->
            diskDataSource.saveToDisk(data)
            memoryDataSource.cacheInMemory(data)
        }
}
