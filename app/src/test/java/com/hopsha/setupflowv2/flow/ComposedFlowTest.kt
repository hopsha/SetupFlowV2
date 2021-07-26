package com.hopsha.setupflowv2.flow

import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ComposedFlowTest {

    @Test
    fun `Composed flow works`() = runBlocking {
        val flow = IntParser()
            .then { input -> input + 1 }
            .then(IntAdderAndToDoubleConverter())
            .then(DoubleStringifier())
        val result = flow.run("1")
        assertEquals("6.0", result)
    }

    @Test
    fun `Composed flow if-else works`() = runBlocking {
        val flow = IntParser()
            .then { input -> input + 1 }
            .thenIf<String, Double, Int> { input -> input > 5 }
                .use(IntSubstitutorAndToDoubleConverter())
                .orElse(IntAdderAndToDoubleConverter())
            .then(DoubleStringifier())
        val resultBelow5 = flow.run("1")
        assertEquals("5.0", resultBelow5)
        val resultAbove5 = flow.run("6")
        assertEquals("2.0", resultAbove5)
    }

    /*@Test
    fun `Stateful flow works`() = runBlocking {
        val flow = StatefulIntParser()
            .then(Add, StatefulIntAdder())
            .thenIf<Int, Int, Int> { input -> input > 5 }
                .use(statefulFlow {  })
                .orElse(statefulFlow {

                })
        val resultBelow5 = flow.run("1")
        assertEquals("5.0", resultBelow5)
        val resultAbove5 = flow.run("6")
        assertEquals("2.0", resultAbove5)
    }*/

    private object Add : StatefulFlow.FlowKey<Int, Int>()

    private class IntParser : Flow<String, Int> {
        override suspend fun run(input: String): Int {
            return input.toInt()
        }
    }

    private class StatefulIntParser : StatefulFlow<String, Int>() {
        override suspend fun runStateful(input: String): Int {
            return input.toInt()
        }
    }

    private class StatefulIntAdder : StatefulFlow<Int, Int>() {
        override suspend fun runStateful(input: Int): Int {
            return input + 1
        }
    }

    private class IntAdderAndToDoubleConverter : Flow<Int, Double> {
        override suspend fun run(input: Int): Double {
            return input.toDouble() + 4
        }
    }

    private class IntSubstitutorAndToDoubleConverter : Flow<Int, Double> {
        override suspend fun run(input: Int): Double {
            return input.toDouble() - 4
        }
    }

    private class DoubleStringifier : Flow<Double, String> {
        override suspend fun run(input: Double): String {
            return input.toString()
        }
    }

}