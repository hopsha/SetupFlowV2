package com.hopsha.setupflowv2.flow

abstract class StatefulFlow<T, R>(
    private val statefulModel: StatefulModel = MapStatefulModel()
) : Flow<T, R> {

    private var cachedInput: T? = null

    final override suspend fun run(input: T): R {
        cachedInput = input
        return runStateful(input)
    }

    abstract suspend fun runStateful(input: T): R

    suspend fun <V, S> goto(key: FlowKey<V, S>) {
        val flow = statefulModel.get(key) ?: throw IllegalArgumentException()
        val input = flow.cachedInput ?: throw IllegalArgumentException()
        flow.run(input)
    }

    suspend fun <V, S> goto(input: V, key: FlowKey<V, S>) {
        val flow = statefulModel.get(key) ?: throw IllegalArgumentException()
        flow.run(input)
    }

    fun <V> then(key: FlowKey<R, V>, runBlock: suspend (input: R) -> V): StatefulFlow<T, V> {
        val flow = statefulFlow(statefulModel, runBlock)
        return then(key, flow)
    }

    fun <V> then(key: FlowKey<R, V>, flow: StatefulFlow<R, V>): StatefulFlow<T, V> {
        statefulModel.put(key, flow)
        return StatefulComposedFlow(statefulModel + flow.statefulModel, this, flow)
    }

    fun <V> thenIf(predicate: suspend (R) -> Boolean): StatefulFlowIf<T, V, R> {
        return StatefulFlowIf(predicate, this)
    }

    class StatefulFlowIf<T, R, V>(
        private val predicate: suspend (V) -> Boolean,
        private val flow: StatefulFlow<T, V>
    ) {

        fun use(ifFlow: StatefulFlow<V, R>): StatefulFlowElse<T, R, V> {
            return StatefulFlowElse(predicate, flow, ifFlow)
        }

        
    }

    class StatefulFlowElse<T, R, V>(
        private val predicate: suspend (V) -> Boolean,
        private val flow: StatefulFlow<T, V>,
        private val ifFlow: StatefulFlow<V, R>
    ) {
        fun orElse(elseFlow: StatefulFlow<V, R>): StatefulFlow<T, R> {
            val secondFlow = flow<V, R> { input ->
                if (predicate(input)) {
                    ifFlow.run(input)
                } else {
                    elseFlow.run(input)
                }
            }
            return StatefulComposedFlow(flow.statefulModel, flow, secondFlow)
        }
    }

    open class FlowKey<T, R>

    interface StatefulModel {
        fun <T, R> put(key: FlowKey<T, R>, flow: StatefulFlow<T, R>)
        fun putAll(pairs: List<Pair<FlowKey<*, *>, StatefulFlow<*, *>>>)
        fun <T, R> get(key: FlowKey<T, R>): StatefulFlow<T, R>?
        fun getAll(): List<Pair<FlowKey<*, *>, StatefulFlow<*, *>>>

        operator fun plus(model: StatefulModel): StatefulModel {
            return this.apply {
                putAll(model.getAll())
            }
        }
    }
}

fun <T, R> statefulFlow(statefulModel: StatefulFlow.StatefulModel = MapStatefulModel(), runBlock: suspend (input: T) -> R): StatefulFlow<T, R> =
    object : StatefulFlow<T, R>(statefulModel) {
        override suspend fun runStateful(input: T): R {
            return runBlock(input)
        }
    }