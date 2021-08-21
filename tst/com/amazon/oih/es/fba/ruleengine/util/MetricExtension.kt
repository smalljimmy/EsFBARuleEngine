package com.amazon.oih.es.fba.ruleengine.util

import com.amazon.coral.metrics.Metrics
import com.amazon.coral.metrics.MetricsFactory
import com.amazon.oih.es.fba.ruleengine.metrics.RequestContext
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class MetricExtension : AfterEachCallback, BeforeEachCallback {

    lateinit var metrics: Metrics
    private lateinit var metricsFactory: MetricsFactory
    lateinit var cloudWatchClient: AmazonCloudWatch
    private lateinit var requestContext: RequestContext

    override fun beforeEach(context: ExtensionContext) {
        metricsFactory = mockkClass(MetricsFactory::class)
        requestContext = mockk()
        metrics = mockk(relaxed = true)
        cloudWatchClient = mockk()
        every{metricsFactory.newMetrics()} returns metrics
        requestContext = RequestContext(metricsFactory, cloudWatchClient)
    }

    override fun afterEach(context: ExtensionContext) {
        requestContext.close()
    }
}
