package com.amazon.oih.es.fba.ruleengine.actor

import com.amazon.oih.es.fba.ruleengine.KIE_ACTOR_CHANNEL_CAPACITY
import com.amazon.oih.es.fba.ruleengine.LOG
import com.amazon.oih.es.fba.ruleengine.entity.DroolsMessage
import com.amazon.oih.es.fba.ruleengine.metrics.record
import com.amazon.oih.es.fba.ruleengine.util.ICPActionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.kie.api.KieServices
import org.kie.api.event.rule.AfterMatchFiredEvent
import org.kie.api.event.rule.DebugAgendaEventListener
import org.kie.api.event.rule.DebugRuleRuntimeEventListener
import org.kie.api.event.rule.DefaultAgendaEventListener
import org.kie.api.runtime.KieSession
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Named

class SingleThreadKIEActor @Inject constructor(@Named("EsFBARuleEngine.kieSessionName") kieSessionName: String): AbstractKIEActor() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override lateinit var actor: SendChannel<DroolsMessage>

    private val kSession : KieSession

    private fun CoroutineScope.createActor() = actor<DroolsMessage> (capacity = KIE_ACTOR_CHANNEL_CAPACITY) {
        consumeEach { msg ->// iterate over incoming messages
            when (msg) {
                is DroolsMessage.SetRuleProcessor -> {
                    kSession.setGlobal("icpActionUtils", ICPActionUtils(msg.request))
                }

                is DroolsMessage.GetRecommendation -> {
                    val factHandle = kSession.insert(msg.request)
                    msg.request.factHandle = factHandle
                    kSession.fireAllRules()
                }

                is DroolsMessage.Retract -> {
                    msg.request.factHandle?.run{
                        msg.request.factHandle = null
                        kSession.delete(this)
                    }
                }

                is DroolsMessage.Reevaluate -> {
                    msg.request.factHandle?.run{
                        msg.request.factHandle = null
                        kSession.delete(this)
                        msg.request.factHandle = kSession.insert(msg.request)
                        kSession.fireAllRules()
                    }
                }

                else -> throw IllegalArgumentException("Can't accept $msg")
            }
        }
    }

    init {
        val ks = KieServices.Factory.get()
        val kc = ks.kieClasspathContainer
        kSession = kc.newKieSession(kieSessionName)

        /**
         * uncomment following lines when debug drools
         */
        if (logger.isTraceEnabled) {
            kSession.addEventListener(DebugAgendaEventListener())
            kSession.addEventListener(DebugRuleRuntimeEventListener())
        }

        kSession.addEventListener(TrackingAgendaEventListener())

        kSession.setGlobal("log", logger)

        actor = GlobalScope.createActor()

        actor.invokeOnClose {
            LOG.error("Rule evaluation failed with exception", it)
            record("EsFBARuleEngine.Failure", 1.0, "SingleThreadKIEActor.init")
        }
    }

    fun finalize(){
        actor.close()
        kSession.dispose()
    }

    private inner class TrackingAgendaEventListener :   DefaultAgendaEventListener() {
        override fun afterMatchFired(event: AfterMatchFiredEvent) {
            val rule = event.match.rule
            logger.debug("Rule fired: $rule")
        }
    }
}
