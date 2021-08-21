package com.amazon.oih.es.fba.ruleengine.actor

import com.amazon.oih.es.fba.ruleengine.LOG
import com.amazon.oih.es.fba.ruleengine.entity.DroolsMessage
import com.amazon.oih.es.fba.ruleengine.metrics.record
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

abstract class AbstractActor(parallelism: Int) : CoroutineScope by GlobalScope {
    private val supervisor = SupervisorJob()

    private var next  = 0

    private val actor: SendChannel<DroolsMessage> = CoroutineScope(supervisor).createParentActor()

    private val children: List<SendChannel<DroolsMessage>> = (1..parallelism).map {
        CoroutineScope(supervisor).handleMessage()
    }

    private fun CoroutineScope.createParentActor() : SendChannel<DroolsMessage> = actor<DroolsMessage>(Dispatchers.IO) {
        consumeEach { msg ->
            children[next++ % children.size].send(msg)
        }
    }

    private fun CoroutineScope.handleMessage(): SendChannel<DroolsMessage> = actor<DroolsMessage> (Dispatchers.IO, capacity = 1 ) {
        consumeEach { msg ->
            launch {
                try {
                    handleMessage(msg)
                } catch (e: Exception) {
                    record("EsFBARuleEngine.Failure", 1.0, "AbstractActor.handleMessage")
                    LOG.error("Failed to handle message $msg", e)
                }
            }
        }
    }

    suspend fun send(message: DroolsMessage) {
        actor.send(message)
    }

    abstract suspend fun handleMessage(msg: DroolsMessage):Unit

    fun finalize(){
        actor.close()
        cancel()
    }

}
