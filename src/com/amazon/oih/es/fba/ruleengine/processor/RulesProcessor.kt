package com.amazon.oih.es.fba.ruleengine.processor

import com.amazon.oih.es.fba.ruleengine.EsFBARuleEngineModule
import com.amazon.oih.es.fba.ruleengine.LOG
import com.amazon.oih.es.fba.ruleengine.MAX_EVALUATION_TIME
import com.amazon.oih.es.fba.ruleengine.actor.AbstractKIEActor
import com.amazon.oih.es.fba.ruleengine.actor.OutletDealActor
import com.amazon.oih.es.fba.ruleengine.constants.ActionType
import com.amazon.oih.es.fba.ruleengine.entity.DroolsMessage
import com.amazon.oih.es.fba.ruleengine.entity.FilterLogicContext
import com.amazon.oih.es.fba.ruleengine.info2
import com.amazon.oih.es.fba.ruleengine.metrics.RequestContext
import com.amazon.oih.es.fba.ruleengine.metrics.record
import com.amazon.oih.es.fba.ruleengine.pmap
import com.google.inject.Guice
import com.google.inject.Injector
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

open class RulesProcessor {

    private val injector: Injector = Guice.createInjector(EsFBARuleEngineModule())
    private val outletDealActor: OutletDealActor
    private val ruleActor: AbstractKIEActor

    init {
        //init actors
        outletDealActor = injector.getInstance(OutletDealActor::class.java)
        ruleActor = injector.getInstance(AbstractKIEActor::class.java)

        val ruleProcessor = this
        runBlocking {
            ruleActor.actor.send(DroolsMessage.SetRuleProcessor(ruleProcessor))
        }
    }

    private fun getRecommendationAsync(request : FilterLogicContext): Deferred<ActionType> = GlobalScope.async {
        val requestContext = injector.getInstance(RequestContext::class.java)

        requestContext.use {
            ruleActor.actor.send(DroolsMessage.GetRecommendation(request))

            val result = withTimeoutOrNull(MAX_EVALUATION_TIME) {
                request.actionTypeAsync.await() ?: ActionType.Unknown
            } ?: ActionType.Timeout

            ruleActor.actor.send(DroolsMessage.Retract(request))

            //update recommendation metric
            record("EsFBARuleEngine.GetRecommendation.Count", 1.0, "getRecommendationAsync")
            record("EsFBARuleEngine.${result.name}.Count", 1.0, "getRecommendationAsync")

            LOG.info2("===>${result.name}:  [fact ${request}]")
            result
        }
    }

    fun reevaluateAsync(request : FilterLogicContext ) = GlobalScope.async {
        ruleActor.actor.send(DroolsMessage.Reevaluate(request))
    }

    open fun getRecommendation (request : FilterLogicContext): ActionType = runBlocking {
        getRecommendationAsync(request).await()
    }


    fun getRecommendations (requests : List<FilterLogicContext>): List<ActionType> =  runBlocking {
        requests.pmap {
            getRecommendation(it)
        }
    }

    fun getOutletEligibilityAsync (request : FilterLogicContext)  = GlobalScope.future{
        outletDealActor.send(DroolsMessage.GetOutletEligibility(request))
    }
}

