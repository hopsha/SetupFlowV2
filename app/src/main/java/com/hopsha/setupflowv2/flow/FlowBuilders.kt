package com.hopsha.setupflowv2.flow

fun <T, R> flow(runBlock: suspend (input: T) -> R): Flow<T, R> = object : Flow<T, R> {
    override suspend fun run(input: T): R {
        return runBlock(input)
    }
}

fun <T, R, V> Flow<T, V>.then(runBlock: suspend (input: V) -> R): Flow<T, R> {
    return ComposedFlow(this, flow(runBlock))
}

fun <T, R, V> Flow<T, V>.then(flow: Flow<V, R>): Flow<T, R> {
    return ComposedFlow(this, flow)
}

operator fun <T, R, V> Flow<T, V>.plus(flow: Flow<V, R>): Flow<T, R> {
    return then(flow)
}

fun <T, R, V> Flow<T, V>.thenIf(predicate: suspend (V) -> Boolean): FlowIf<T, R, V> {
    return FlowIf(predicate, this)
}

class FlowIf<T, R, V>(
    private val predicate: suspend (V) -> Boolean,
    private val flow: Flow<T, V>
) {
    fun use(ifFlow: Flow<V, R>): FlowElse<T, R, V> {
        return FlowElse(predicate, flow, ifFlow)
    }
}

class FlowElse<T, R, V>(
    private val predicate: suspend (V) -> Boolean,
    private val flow: Flow<T, V>,
    private val ifFlow: Flow<V, R>
) {
    fun orElse(elseFlow: Flow<V, R>): Flow<T, R> {
        val secondFlow = flow<V, R> { input ->
            if (predicate(input)) {
                ifFlow.run(input)
            } else {
                elseFlow.run(input)
            }
        }
        return ComposedFlow(flow, secondFlow)
    }
}
