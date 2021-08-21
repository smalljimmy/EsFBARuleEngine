package com.amazon.oih.es.fba.ruleengine.actor

import com.amazon.oih.es.fba.ruleengine.*
import com.amazon.oih.es.fba.ruleengine.entity.DroolsMessage
import com.amazon.oih.es.fba.ruleengine.processor.RulesProcessor
import com.amazon.oih.es.fba.ruleengine.util.ICPActionUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.kie.api.KieServices
import org.kie.api.event.rule.AfterMatchFiredEvent
import org.kie.api.event.rule.DefaultAgendaEventListener
import org.kie.api.runtime.KieContainer
import org.kie.api.runtime.KieSession
import org.kie.api.runtime.StatelessKieSession
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.util.concurrent.ArrayBlockingQueue
import javax.inject.Inject
import javax.inject.Named

class MultiThreadKIEActor @Inject constructor(@Named("kieSessionName") kieSessionName: String) : AbstractKIEActor() {
    var next  = 0

    private val lock = Object()

    private var messages = ArrayBlockingQueue<DroolsMessage.GetRecommendation>(KIE_ACTOR_MESSAGE_QUEUE_SIZE)

    private var children = listOf<KieSessionRunnable>()

    private  val kieContainer: KieContainer

    private lateinit var ruleProcessor: RulesProcessor

    override lateinit var actor : SendChannel<DroolsMessage>

    init {
        val ks = KieServices.Factory.get()
        kieContainer = ks.kieClasspathContainer

        actor = GlobalScope.actor(capacity = KIE_ACTOR_CHANNEL_CAPACITY) {
            consumeEach { msg ->
                when (msg) {
                    is DroolsMessage.SetRuleProcessor -> {
                        ruleProcessor = msg.request
                        children = (1..KIE_THREADS).map { KieSessionRunnable(lock, kieSessionName, kieContainer, ruleProcessor) }
                        children.forEach{
                            Thread(it).start()
                        }
                    }

                    is DroolsMessage.GetRecommendation -> {
                        synchronized(lock) {
                            messages.add(msg)
                            lock.notifyAll()
                        }
                    }

                    is DroolsMessage.Retract -> {
                        //do nothing
                    }

                    else -> throw IllegalArgumentException("Can't accept $msg")
                }
            }
        }
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    inner class KieSessionRunnable (private val lock: Object, private val kieSessionName: String, private val kieContainer: KieContainer, private val ruleProcessor: RulesProcessor): Runnable {
        private lateinit var kieSession: KieSession

        override fun run() {
            kieSession = kieContainer.newKieSession(kieSessionName)

            kieSession.addEventListener(TrackingAgendaEventListener())

            kieSession.setGlobal("log", LoggerFactory.getLogger(RulesProcessor::class.java))
            kieSession.setGlobal("icpActionUtils", ICPActionUtils(ruleProcessor))

            while (true) {
                synchronized(lock) {
                    while (messages.isEmpty()) {
                        lock.wait()
                    }
                }

                while (!messages.isEmpty()) {
                    var batch = mutableListOf<DroolsMessage.GetRecommendation>()
                    synchronized(messages) {
                        LOG.debug("remaining ${messages.size} messages")

                        if (messages.isNotEmpty()) {
                            val batchSize = if (messages.size/KIE_THREADS > 0) messages.size/KIE_THREADS else messages.size
                            repeat(batchSize) {
                                batch.add(messages.take())
                            }
                        }
                    }

                    //evaluate batch requests from rule engine
                    kieSession.insert(batch.map{it.request})
                    kieSession.fireAllRules()

                    //resolve batch results with recommendations
                    batch.map{it.request.actionTypeAsync}
                }
            }//while
        }//run

        private inner class TrackingAgendaEventListener :   DefaultAgendaEventListener() {
            override fun afterMatchFired(event: AfterMatchFiredEvent) {
                val rule = event.match.rule
                LOG.debug("Rule fired: $rule")
            }
        }
    }//inner-class


    fun finalize(){
        actor.close()
    }

}
