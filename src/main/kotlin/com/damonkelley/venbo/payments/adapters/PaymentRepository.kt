package com.damonkelley.venbo.payments.adapters

import com.damonkelley.venbo.EventStore
import com.damonkelley.venbo.Repository
import com.damonkelley.venbo.infrastructure.Envelope
import com.damonkelley.venbo.infrastructure.Trace
import com.damonkelley.venbo.payments.Event
import com.damonkelley.venbo.payments.Payment

class PaymentRepository(private val store: EventStore, val trace: Trace) : Repository<Payment> {
    override fun get(id: String): Payment? {
        val events = store.load(id).map { envelope ->
            when (envelope.message) {
                is Event -> envelope.message
                else -> println("===> ${envelope.message}").let { throw Exception("incompatible payment event $envelope") }
            }
        }

        if (events.isEmpty()) return null

        return Payment(events)
    }

    override fun save(aggregate: Payment) {
        store.save(aggregate.id, aggregate.changes.map {
            Envelope(message = it, trace = Trace(trace))
        })
    }
}