package com.amazon.oih.es.fba.ruleengine.metrics

import com.amazon.api.client.ext.google.common.base.CaseFormat
import com.amazon.oih.es.fba.ruleengine.LOG
import com.amazonaws.services.cloudwatch.model.Dimension
import com.amazonaws.services.cloudwatch.model.MetricDatum
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest
import javax.measure.unit.SI
import javax.measure.unit.Unit
import kotlin.system.measureTimeMillis

fun record(metric: String, value: Boolean, operationName: String = "") {
    RequestContext.currentMetrics(operationName).addCount(metric.toCamelCase(), if (value) 1.0 else 0.0, Unit.ONE)
}

fun record(metric: String, value: Number, operationName: String = "") {
    RequestContext.currentMetrics(operationName).addCount(metric.toCamelCase(), value.toDouble(), Unit.ONE)
}

inline fun <T : Any> recordDuration(metric: String, operationName: String = "", crossinline action: () -> T): T {
    lateinit var result: T
    val timeElapsed = measureTimeMillis {
        result = action()
    }

    RequestContext.currentMetrics(operationName).addTime(metric.toCamelCase(), timeElapsed.toDouble(), SI.MILLI(SI.SECOND))
    return result
}

fun recordRaw(applicationName: String, metric: String, value: Number, dimensions: List<Dimension>) {
    val datum = MetricDatum()
            .withMetricName(metric.toCamelCase())
            .withValue(value.toDouble())
            .withDimensions(dimensions)

    val putMetricDataRequest = PutMetricDataRequest()
            .withMetricData(datum)
            .withNamespace(applicationName)

    try {
        RequestContext.currentCloudWatchClient().putMetricData(putMetricDataRequest)
    } catch (e: Exception) {
        LOG.error("Failed to publish a metric to CloudWatch. The metric was: $putMetricDataRequest", e)
    }
}


fun String.toCamelCase(): String = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, this)
