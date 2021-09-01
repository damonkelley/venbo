package com.damonkelley.venbo.infrastructure

data class Envelope<T>(val message: T, val trace: Trace)

data class Trace(
    val id: String,
    val correlationId: String,
    val causationId: String,
)