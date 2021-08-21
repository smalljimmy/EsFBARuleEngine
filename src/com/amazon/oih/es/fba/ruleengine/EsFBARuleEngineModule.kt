package com.amazon.oih.es.fba.ruleengine

import amazon.odin.awsauth.OdinAWSCredentialsProvider
import com.amazon.api.client.AmazonApiClient
import com.amazon.api.client.ApiEndpoint
import com.amazon.api.client.MAWSCallerLocation
import com.amazon.api.client.SessionAwareApiClient
import com.amazon.coral.metrics.MetricsFactory
import com.amazon.coral.metrics.helper.QuerylogHelper
import com.amazon.guice.brazil.AppConfigBinder
import com.amazon.oih.es.fba.outletdeal.dao.ESAccessor
import com.amazon.oih.es.fba.ruleengine.actor.AbstractKIEActor
import com.amazon.oih.es.fba.ruleengine.actor.SingleThreadKIEActor
import com.amazon.oih.es.fba.ruleengine.cache.source.DataSource
import com.amazon.oih.es.fba.ruleengine.cache.source.DiskDataSource
import com.amazon.oih.es.fba.ruleengine.cache.source.MemoryDataSource
import com.amazon.oih.es.fba.ruleengine.cache.source.NetworkDataSource
import com.amazon.oih.es.fba.ruleengine.metrics.RequestContext
import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.http.AWSSignerInterceptor
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import org.apache.http.HttpHost
import org.apache.http.impl.nio.reactor.IOReactorConfig
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import javax.inject.Named

class EsFBARuleEngineModule() : AbstractModule() {

    override fun configure() {
        bindAppConfig()

        bind(AbstractKIEActor::class.java).to(SingleThreadKIEActor::class.java)

        bind(RequestContext::class.java).asEagerSingleton()
    }

    private fun bindAppConfig() {
        val appConfigBinder = AppConfigBinder(binder())
        appConfigBinder.bindPrefix("*")
        AppConfigBinder.bindAll(binder())
    }


    @Provides
    @Singleton
    fun metricsFactory(
            @Named("root") root: String,
            @Named("domain") domain: String,
            @Named("realm") realm: String
    ): MetricsFactory {
        val metricsFactory = QuerylogHelper()
        metricsFactory.setFilename("$root/var/output/logs/service_log")
        metricsFactory.setProgram("EsFBARuleEngine")
        metricsFactory.setMarketplace(java.lang.String.format("%s:%s:%s", "EsFBARuleEngine", domain, realm))
        return metricsFactory
    }

    @Provides
    fun dataSource(): DataSource {
        return DataSource(MemoryDataSource(), DiskDataSource(), NetworkDataSource())
    }

    @Provides
    fun amazonApi(
            @Named("EsFBARuleEngine.awsStage")  stage: String,
            @Named("EsFBARuleEngine.awsRealm") realm: String
    ): SessionAwareApiClient {
        return AmazonApiClient.builder()
                .withEndpoint(ApiEndpoint.builder()
                        .withStage(ApiEndpoint.Stage.valueOf(stage))
                        .withRegion(ApiEndpoint.Region.valueOf(realm))
                        .withLocation(MAWSCallerLocation.current())
                        .build())
                .buildAAAClient()
    }


    @Provides
    @Singleton
    fun elasticsearchClient(
            @Named("EsFBARuleEngine.awsRegion") region: String,
            @Named("EsFBARuleEngine.esHost")  hostname: String,
            credentialsProvider: AWSCredentialsProvider): RestHighLevelClient {
        val serviceName = "es"

        val signer = AWS4Signer().apply {
            this.serviceName = serviceName
            this.regionName = getRegion(region).getName()
        }

        val httpHost = HttpHost(hostname, 443, "https")
        LOG.debug("Using Elasticsearch endpoint: $httpHost")

        val interceptor = AWSSignerInterceptor(serviceName, signer, credentialsProvider)
        val restClientBuilder = RestClient
                .builder(httpHost)
                .setRequestConfigCallback {
                    it.setConnectTimeout(ES_CONNECT_TIMEOUT)
                    it.setSocketTimeout(ES_SOCKET_TIMEOUT)
                }
                .setHttpClientConfigCallback {
                    it.addInterceptorLast(interceptor)
                    it.setDefaultIOReactorConfig(
                        IOReactorConfig.custom()
                                .setIoThreadCount(ES_THREADS)
                                .build()
                    )
                }

        return RestHighLevelClient(restClientBuilder)
    }


    @Provides
    @Singleton
    fun getAWSCredentialsProvider(
            @Named("EsFBARuleEngine.odin") odin: String): AWSCredentialsProvider {
        return AWSCredentialsProviderChain(
                EnvironmentVariableCredentialsProvider(),
                OdinAWSCredentialsProvider(odin, false))
    }


    @Provides
    @Singleton
    fun amazonCloudWatchClient(
            @Named("EsFBARuleEngine.awsRegion") region: String,
            credentialsProvider: AWSCredentialsProvider): AmazonCloudWatch {
        return AmazonCloudWatchClient.builder()
                .withRegion(getRegion(region).getName())
                .withCredentials(credentialsProvider)
                .build()
    }


    @Provides
    @Singleton
    fun esAccessor(
            restHighLevelClient: RestHighLevelClient
    ): ESAccessor = ESAccessor(restHighLevelClient)


    private fun getRegion(region: String): Regions {
        return Regions.fromName(region)
    }
}
