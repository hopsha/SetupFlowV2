package com.hopsha.setupflowv2.flow

class ComposedFlow<T, R, V>(
    private val firstFlow: Flow<T, V>,
    private val secondFlow: Flow<V, R>
): Flow<T, R> {

    override suspend fun run(input: T): R {
        val result = firstFlow.run(input)
        return secondFlow.run(result)
    }
}