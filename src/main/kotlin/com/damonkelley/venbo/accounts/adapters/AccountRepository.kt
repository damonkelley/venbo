package com.damonkelley.venbo.accounts.adapters

import com.damonkelley.venbo.EventStore
import com.damonkelley.venbo.Repository
import com.damonkelley.venbo.accounts.Account
import com.damonkelley.venbo.accounts.Event
import com.damonkelley.venbo.infrastructure.Envelope
import com.damonkelley.venbo.infrastructure.Trace

class AccountRepository(private val store: EventStore, val trace: Trace) : Repository<Account> {
    override fun get(id: String): Account? {
        val events = store.load(id).map {
            when (it.message) {
                is Event -> it.message
                else -> throw Exception("incompatible event")
            }
        }

        if (events.isEmpty()) return null

        return Account(events)
    }

    override fun save(aggregate: Account) {
        store.save(aggregate.id, aggregate.changes.map {
            Envelope(message = it, trace = Trace(trace))
        })
    }
}