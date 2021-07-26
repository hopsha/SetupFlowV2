package com.hopsha.setupflowv2.flow

class MapStatefulModel: StatefulFlow.StatefulModel {

    private val storage: MutableMap<StatefulFlow.FlowKey<*, *>, StatefulFlow<*, *>> = mutableMapOf()

    override fun <T, R> put(key: StatefulFlow.FlowKey<T, R>, flow: StatefulFlow<T, R>) {
        storage[key] = flow
    }

    override fun putAll(pairs: List<Pair<StatefulFlow.FlowKey<*, *>, StatefulFlow<*, *>>>) {
        pairs.forEach { (key, flow) ->
            storage[key] = flow
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, R> get(key: StatefulFlow.FlowKey<T, R>): StatefulFlow<T, R>? {
        return storage[key]?.let {
            it as StatefulFlow<T, R>
        }
    }

    override fun getAll(): List<Pair<StatefulFlow.FlowKey<*, *>, StatefulFlow<*, *>>> {
        return storage.entries
            .map { it.key to it.value }
            .toList()
    }
}