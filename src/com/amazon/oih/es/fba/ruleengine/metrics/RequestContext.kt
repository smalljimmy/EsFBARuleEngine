package com.amazon.oih.es.fba.ruleengine.metrics

import com.amazon.coral.metrics.Metrics
import com.amazon.coral.metrics.MetricsFactory
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.google.inject.Inject
import org.apache.logging.log4j.ThreadContext
import java.io.Closeable
import java.net.InetAddress
import kotlin.concurrent.getOrSet


/**
 * Used to be able to record metrics without having MetricsContext instance
 */
class RequestContext @Inject constructor(
        metricsFactory: MetricsFactory,
        cloudWatchClient: AmazonCloudWatch
) : Closeable {

    init {
        internalMetricsFactory = metricsFactory
        internalCloudWatchClient = cloudWatchClient
    }

    override fun close() {
        currentMetrics().close()
        METRICS_CONTEXT.remove()

        CLOUD_WATCH_CONTEXT.remove()

        ThreadContext.clearAll()
    }

    companion object {
        private lateinit var internalMetricsFactory: MetricsFactory
        private  lateinit var internalCloudWatchClient: AmazonCloudWatch

        private val METRICS_CONTEXT = ThreadLocal<Metrics>()
        private val CLOUD_WATCH_CONTEXT = ThreadLocal<AmazonCloudWatch>()

        fun currentMetrics(operationName: String=""): Metrics {
            return METRICS_CONTEXT.getOrSet {
                val metrics = internalMetricsFactory.newMetrics()
                metrics.addProperty("Operation", operationName);
                metrics.addDate("StartTime", System.currentTimeMillis().toDouble());
                metrics.addProperty("Host", getHostName())
                metrics
            }
        }

        private fun getHostName(): String {
            return try {
                val inetAddr = InetAddress.getLocalHost()
                inetAddr.hostName
            } catch (ignored: Exception) {
                ""
            }
        }

        fun currentCloudWatchClient(): AmazonCloudWatch {
            return CLOUD_WATCH_CONTEXT.getOrSet {
                internalCloudWatchClient
            }
        }
    }
}
