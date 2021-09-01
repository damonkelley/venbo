package com.damonkelley.venbo.payments.adapters

import com.damonkelley.venbo.EventStore
import com.damonkelley.venbo.Repository
import com.damonkelley.venbo.infrastructure.Envelope
import com.damonkelley.venbo.infrastructure.Trace
import com.damonkelley.venbo.payments.Event
import com.damonkelley.venbo.payments.Payment

class PaymentRepository(private val store: EventStore) : Repository<Payment> {
    override fun get(id: String): Payment? {
        val events = store.load(id).map {
            when (it.message) {
                is Event -> it.message
                else -> throw Exception("incompatible event")
            }
        }

        if (events.isEmpty()) return null

        return Payment(events)
    }

    override fun save(aggregate: Payment) {
        store.save(aggregate.id, aggregate.changes.map {
            Envelope(message = it, trace = Trace("", "", ""))
        })
    }
}