package com.amazon.oih.es.fba.outletdeal.dao

import com.amazon.oih.es.fba.outletdeal.model.AsinInputDocument
import com.amazon.oih.es.fba.outletdeal.model.AsinItemSafetyDocument
import com.amazon.oih.es.fba.outletdeal.model.AsinPriceDocument
import com.amazon.oih.es.fba.outletdeal.model.Filters
import com.amazon.oih.es.fba.outletdeal.model.Pagination
import com.amazon.oih.es.fba.outletdeal.model.Predicate
import com.amazon.oih.es.fba.outletdeal.model.QueryResult
import com.amazon.oih.es.fba.outletdeal.model.SellerRatingDocument
import com.amazon.oih.es.fba.outletdeal.model.SortField
import com.amazon.oih.es.fba.ruleengine.EsAssertionFailureException
import com.amazon.oih.es.fba.ruleengine.EsDependencyException
import com.amazon.oih.es.fba.ruleengine.LOG
import com.amazon.oih.es.fba.ruleengine.metrics.recordDuration
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.annotations.VisibleForTesting
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.index.query.TermQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder
import java.io.Closeable
import java.util.*
import javax.inject.Inject

const val SELLER_RATING_INDEX_NAME: String = "seller-rating"
const val ASIN_INPUT_INDEX_NAME: String = "asin-input"
const val ASIN_PRICE_INDEX_NAME: String = "asin-price"
const val ASIN_ITEM_SAFETY_INDEX_NAME: String = "asin-item-safety"


class ESAccessor @Inject constructor(
        private val elasticsearchClient: RestHighLevelClient
) : Closeable {
    val esDependencyException = {e: Exception -> EsDependencyException("Fail to access ES", e)}

    fun getSellerRating(marketplaceId: Long, merchantCustomerId: Long): SellerRatingDocument? {
        try {
            val results = queryES<SellerRatingDocument>(
                    SELLER_RATING_INDEX_NAME,
                    Predicate.And(
                            listOf(Predicate.MarketplaceId(marketplaceId), Predicate.MerchantCustomerId(merchantCustomerId))))

            return when (results.totalResults) {
                0L -> null
                1L -> results.results.first()
                else -> throw EsAssertionFailureException("Found ${results.totalResults} results with the same id: $marketplaceId, $merchantCustomerId")
            }
        } catch (e: Exception) {
            throw esDependencyException(e)
        }
    }

    fun getAsinPrice(marketplaceId: Long, asin: String): AsinPriceDocument? {
        try {
            val results = queryES<AsinPriceDocument>(
                    ASIN_PRICE_INDEX_NAME,
                    Predicate.And(
                            listOf(Predicate.MarketplaceId(marketplaceId), Predicate.Asin(asin))))

            return when (results.totalResults) {
                0L -> null
                1L -> results.results.first()
                else -> throw EsAssertionFailureException("Found ${results.totalResults} results with the same id: $marketplaceId, $asin")
            }
        } catch (e: Exception) {
            throw esDependencyException(e)
        }
    }

    fun getAsinInput(marketplaceId: Long, asin: String): AsinInputDocument? {
        try {
            val results = queryES<AsinInputDocument>(
                    ASIN_INPUT_INDEX_NAME,
                    Predicate.And(
                            listOf(Predicate.MarketplaceId(marketplaceId), Predicate.Asin(asin))))

            return when (results.totalResults) {
                0L -> null
                1L -> results.results.first()
                else -> throw EsAssertionFailureException("Found ${results.totalResults} results with the same id: $marketplaceId, $asin")
            }
        } catch (e: Exception) {
            throw esDependencyException(e)
        }
    }

    fun getAsinItemSafety(marketplaceId: Long, asin: String): AsinItemSafetyDocument? {
        try {
            val results = queryES<AsinItemSafetyDocument>(
                    ASIN_ITEM_SAFETY_INDEX_NAME,
                    Predicate.And(
                            listOf(Predicate.MarketplaceId(marketplaceId), Predicate.Asin(asin))))

            return when (results.totalResults) {
                0L -> null
                1L -> results.results.first()
                else -> throw EsAssertionFailureException("Found ${results.totalResults} results with the same id: $marketplaceId, $asin")
            }
        } catch (e: Exception) {
            throw esDependencyException(e)
        }
    }

    companion object {
        val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

        @VisibleForTesting
        fun SellerRatingDocument.toJson(): String? {
            return objectMapper.writeValueAsString(this)
        }

        @VisibleForTesting
        fun AsinInputDocument.toJson(): String? {
            return objectMapper.writeValueAsString(this)
        }

        @VisibleForTesting
        fun AsinPriceDocument.toJson(): String? {
            return objectMapper.writeValueAsString(this)
        }

        @VisibleForTesting
        fun AsinItemSafetyDocument.toJson(): String? {
            return objectMapper.writeValueAsString(this)
        }

        private val allFiltersOff = Filters(shouldIncludeAll = true, shouldIncludeDeleted = true)
    }


    private fun createQuery(predicate: Predicate): QueryBuilder {
        return when (predicate) {
            is Predicate.And -> BoolQueryBuilder().apply {
                for (p in predicate.predicates) {
                    filter(createQuery(p))
                }
            }
            is Predicate.Or -> BoolQueryBuilder().apply {
                for (p in predicate.predicates) {
                    should(createQuery(p))
                }
            }
            is Predicate.Asin -> TermQueryBuilder("asin.keyword", predicate.asin)
            is Predicate.MarketplaceId -> TermQueryBuilder("marketplace_id", predicate.marketplaceId)
            is Predicate.MerchantCustomerId -> TermQueryBuilder("merchant_customer_id", predicate.merchantCustomerId)
            else -> throw (EsAssertionFailureException("$predicate is not supported"))
        }
    }


    private fun createFilter(filters: Filters): QueryBuilder {
        return BoolQueryBuilder().apply {
            if (!filters.shouldIncludeAll) {
                filter(RangeQueryBuilder("timestamp").lt(Date()))
            }

            if (!filters.shouldIncludeDeleted) {
                filter(TermQueryBuilder("deleted", false))
            }
        }
    }

    private inline fun <reified T> queryES(
            index: String,
            predicate: Predicate,
            pagination: Pagination = Pagination.from(0L).to(2L),
            sortOrders: List<Pair<SortField, SortOrder>>? = null,
            filters: Filters = allFiltersOff
    ): QueryResult<T> {
        val (from, size) = pagination

        val queryBuilder = BoolQueryBuilder().apply {
            filter(createQuery(predicate))
            filter(createFilter(filters))
        }

        val searchRequest = SearchRequest(arrayOf(index), SearchSourceBuilder().apply {
            query(queryBuilder)
            size(size.toInt())
            from(from.toInt())
            sortOrders?.forEach { sort(it.first.fieldName, it.second) }
        })

        LOG.debug("Query $index with searchRequest: ${searchRequest.source()}")

        val searchResponse = recordDuration("Query.${index}.Duration", "queryES") { elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT) }
        val results = searchResponse.hits.hits.map { objectMapper.readValue(it.sourceAsString, T::class.java) }
        val totalResults = searchResponse.hits.totalHits.value
        val tookTime = searchResponse.took.millis

        LOG.debug("Query returned ${results.size} from $totalResults total in $tookTime ms")

        return QueryResult(results, totalResults)
    }

    override fun close() {
        elasticsearchClient.close()
    }

}
