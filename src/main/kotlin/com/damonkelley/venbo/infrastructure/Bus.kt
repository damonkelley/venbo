package com.damonkelley.venbo.infrastructure

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class Bus<T>(private val scope: CoroutineScope) {
    private val bus = MutableSharedFlow<T>(replay = 10)
    private val events = bus.asSharedFlow()

    fun publish(event: T) : Result<Unit> {
        scope.launch {
            bus.emit(event)
        }

        return Result.success(Unit)
    }

    suspend fun subscribe(predicate: (event: T) -> Boolean = { true }, action: (event: T) -> Unit) {
        events.filter { predicate(it) }.collect { action(it) }
    }
}

