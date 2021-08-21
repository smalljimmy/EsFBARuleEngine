package com.amazon.oih.es.fba.outletdeal.dao

import com.amazon.oih.es.fba.outletdeal.dao.ESAccessor.Companion.toJson
import com.amazon.oih.es.fba.outletdeal.model.AsinInputDocument
import com.amazon.oih.es.fba.outletdeal.model.AsinPriceDocument
import com.amazon.oih.es.fba.outletdeal.model.SellerRatingDocument
import com.amazon.oih.es.fba.ruleengine.util.MetricExtension
import io.mockk.every
import io.mockk.mockk
import org.apache.lucene.search.TotalHits
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchResponseSections
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.SearchHits
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MetricExtension::class)
internal class ESAccessorTest {
    private val elasticSearchClient: RestHighLevelClient  = mockk()
    private val esAccessor = ESAccessor(elasticSearchClient)

    @Test
    fun `test getSellerRating`() {
        val response = getSearchResponseFromString(SellerRatingDocument(1L, 1234L, 1234L, 1.toDouble(), Date()).toJson()!!, 1)
        every { elasticSearchClient.search(any(), any())} returns response
        esAccessor.getSellerRating(1L, 1245L)
    }

    @Test
    fun `test getAsinInput`() {
        val response = getSearchResponseFromString(AsinInputDocument(1L,1234L, "ASIN", 1, 2.toDouble(), 0, Date()).toJson()!!, 1)
        every {elasticSearchClient.search(any(), any())} returns response
        esAccessor.getAsinInput(1L, "ASIN")
    }

    @Test
    fun `test getAsinPrice`() {
        val response = getSearchResponseFromString(AsinPriceDocument(1L, "ASIN", 1.0.toBigDecimal(), Date()).toJson()!!, 1)
        every {elasticSearchClient.search(any(), any())} returns response
        esAccessor.getAsinPrice(1L, "ASIN")
    }

    private fun getSearchResponseFromString(json:String?, total:Long):SearchResponse {
        val hits: SearchHits
        hits = if (json == null)
        {
            SearchHits(arrayOf<SearchHit>(), TotalHits(total, TotalHits.Relation.EQUAL_TO), 10f)
        }
        else
        {
            val source = BytesArray(json)
            val hit = SearchHit(1)
            hit.sourceRef(source)
            SearchHits(arrayOf<SearchHit>(hit), TotalHits(total, TotalHits.Relation.EQUAL_TO), 10f)
        }
        val searchResponseSections = SearchResponseSections(hits, null, null, false, null, null, 5)
        return SearchResponse(searchResponseSections, null, 1, 1, 0, 1, arrayOf(),
                SearchResponse.Clusters(1, 1, 0))
    }
}
