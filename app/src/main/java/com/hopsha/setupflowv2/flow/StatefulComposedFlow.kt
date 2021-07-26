package com.hopsha.setupflowv2.flow

class StatefulComposedFlow<T, R, V>(
    statefulModel: StatefulModel,
    private val firstFlow: Flow<T, V>,
    private val secondFlow: Flow<V, R>
): StatefulFlow<T, R>(statefulModel) {

    override suspend fun runStateful(input: T): R {
        val result = firstFlow.run(input)
        return secondFlow.run(result)
    }
}