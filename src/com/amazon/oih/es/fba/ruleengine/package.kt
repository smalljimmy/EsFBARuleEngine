package com.amazon.oih.es.fba.ruleengine

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.sql.Timestamp
import kotlin.coroutines.CoroutineContext

//Global
//allow maximum 5 seconds for evaluating each event
const val MAX_EVALUATION_TIME = 50000L


//ES constants
const val ES_THREADS = 100
const val ES_CONNECT_TIMEOUT = 5000
const val ES_SOCKET_TIMEOUT = 30000


//KIE constants
const val KIE_THREADS = 100
const val KIE_ACTOR_CHANNEL_CAPACITY = 10000
const val KIE_ACTOR_MESSAGE_QUEUE_SIZE = 1000000

//max calls in parallel
//actual parallel calls is min (rule_actor_threads, axiom_actor_number)
const val AMAZAON_API_ACTOR_NUMBER = 10000

const val OUTLET_DEAL_ACTOR_NUMBER = 10000

const val ANSI_RED = "\u001B[31m"
const val ANSI_YELLOW = "\u001B[33m"
const val ANSI_WHITE = "\u001B[37m";
const val ANSI_GREEN = "\u001B[32m";
const val ANSI_RESET = "\u001B[0m"

val LOG: Logger = LogManager.getLogger()

class EsDependencyException(message: String = "", exception: Throwable = Throwable()) : Exception(message, exception)

class EsAssertionFailureException(message: String = "", exception: Throwable = Throwable()) : Exception(message, exception)

//extension methods
suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}

fun coroutineContext(actor: Any): CoroutineContext {
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        LOG.error("Caught $exception", exception)
    }

    return (exceptionHandler + Dispatchers.IO + CoroutineName(actor.javaClass.simpleName))
}

//highlighted log messages
fun Logger.debug2(msg: String) = this.debug("[${Thread.currentThread().name}]\n$ANSI_YELLOW$msg$ANSI_RESET")
fun Logger.info2(msg: String) = this.info("[${Thread.currentThread().name}]\n$ANSI_GREEN$msg$ANSI_RESET")
fun Logger.error2(msg: String) = this.error("[${Thread.currentThread().name}]\n$ANSI_RED$msg$ANSI_RESET")
fun Logger.error2(msg: String, exception: Throwable) = this.error("${Timestamp(System.currentTimeMillis())} [${Thread.currentThread().name}] \n$ANSI_RED $msg $ANSI_RESET", exception)
