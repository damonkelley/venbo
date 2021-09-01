package com.damonkelley.venbo.infrastructure

import java.util.UUID

data class Envelope<T>(val message: T, val trace: Trace)

data class Trace(
    val id: String,
    val correlationId: String,
    val causationId: String,
) {
    constructor(id: String = UUID.randomUUID().toString()) : this(id, id, id)
    constructor(trace: Trace) : this(
        UUID.randomUUID().toString(),
        correlationId = trace.correlationId,
        causationId = trace.id
    )
}