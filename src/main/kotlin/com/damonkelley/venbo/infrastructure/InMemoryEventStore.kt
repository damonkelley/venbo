package com.damonkelley.venbo.infrastructure

import com.damonkelley.venbo.EventStore
import com.damonkelley.venbo.Publish

data class InMemoryEventStore(val publish: (Envelope<Any>) -> Result<Unit>) : EventStore {
    public val streams: MutableMap<String, List<Envelope<Any>>> = mutableMapOf()

    override fun load(id: String): List<Envelope<Any>> {
        return streams[id] ?: emptyList()
    }

    override fun save(id: String, events: List<Envelope<Any>>) {
        val existingEvents = load(id)

        streams[id] = existingEvents + events

        events.forEach { publish(it) }
    }
}