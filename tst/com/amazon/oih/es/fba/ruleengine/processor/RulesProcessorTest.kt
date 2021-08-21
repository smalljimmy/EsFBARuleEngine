package com.amazon.oih.es.fba.ruleengine.processor

import com.amazon.oih.es.fba.ruleengine.EsFBARuleEngineModule
import com.amazon.oih.es.fba.ruleengine.constants.ActionType
import com.amazon.oih.es.fba.ruleengine.entity.FilterLogicContext
import com.amazon.oih.es.fba.ruleengine.util.AppConfigTestInitializer
import com.amazon.oih.es.fba.ruleengine.util.MetricExtension
import com.google.inject.Guice
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@ExtendWith(MetricExtension::class)
class RulesProcessorTest {
    private fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

    @Inject
    private lateinit var rulesProcessor: RulesProcessor

    private var injector = Guice.createInjector(
            AppConfigTestInitializer(),
            EsFBARuleEngineModule())

    @BeforeEach
    fun setup() {
        AppConfigTestInitializer.initialize()
        injector.injectMembers(this);
    }

    @TestFactory
    @Tag("UnitTests")
    fun `test fba recommendations automatically`() = TestCases.fbaTests
            .map { (input, expected) ->
                DynamicTest.dynamicTest("when I calculate $input then I get $expected") {
                    log("-----------------------------------------------")
                    val result = rulesProcessor.getRecommendation(input)
                    assertEquals(expected, result)
                }
            }

    @Test
    @Tag("PerformanceTests")
    fun `test X times as one batch`() {
        var size = 10
        var testCase = 2

        val timeElapsed = measureTimeMillis {
            runBlocking {
                val requests = (1..size).map {
                    TestCases.fbaTests[testCase].first
                }

                val results = rulesProcessor.getRecommendations(requests)

                results.forEach {
                    log("result: $it")
                    assertEquals( TestCases.fbaTests[testCase].second, it)
                }
            }
        }

        log("#req: $size / total time: $timeElapsed ms / avg latency: ${timeElapsed / size} ms")
    }

    @Test
    @Tag("PerformanceTests")
    fun `do massive mixed tests`() {
        val requests = mutableListOf<FilterLogicContext>()
        val expectations = mutableListOf<ActionType>()

        val timeElapsed = measureTimeMillis {
            runBlocking {
                massiveRun {
                    requests.addAll(TestCases.fbaTests.map { it.first }.toList())
                    expectations.addAll(TestCases.fbaTests.map { it.second }.toList())

                    val results = rulesProcessor.getRecommendations(requests)

                    results.forEachIndexed { index, recommendation ->
                        assertEquals(expectations[index], recommendation)
                    }
                }
            }
        }

        log("#req: ${requests.size} / total time: $timeElapsed ms / avg latency: ${timeElapsed / requests.size} ms")
    }

    private suspend fun massiveRun(action: suspend () -> Unit) {
        val n = 5  // number of coroutines to launch
        val k = 5 // times an action is repeated by each coroutine
        coroutineScope {
            // scope for coroutines
            repeat(n) {
                launch {
                    repeat(k) { action() }
                }
            }
        }
    }
}
