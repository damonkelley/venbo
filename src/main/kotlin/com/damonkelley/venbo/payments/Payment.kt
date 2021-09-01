package com.damonkelley.venbo.payments

import java.math.BigDecimal

class Payment(history: List<Event> = emptyList()) {
    var id: String = ""
    private var toAccount: String = ""
    private var fromAccount: String = ""
    private var amount: BigDecimal = BigDecimal.ZERO
    private var status: String = ""

    val changes = mutableListOf<Event>()

    init {
        history.forEach {
            when (it) {
                is PaymentInitiated -> handle(it)
                is PaymentCompleted -> handle(it)
            }
        }
    }

    fun initiate(id: String, toAccount: String, fromAccount: String, amount: BigDecimal): Payment {
        return PaymentInitiated(id, fromAccount, toAccount, amount)
            .apply(::raise)
            .let(::handle)
    }

    fun complete(): Payment {
        return PaymentCompleted(
            id = id,
            toAccount = toAccount,
            fromAccount = fromAccount,
            amount = amount
        )
            .apply(::raise)
            .let(::handle)
    }

    private fun raise(event: Event) {
        changes.add(event)
    }

    private fun handle(event: PaymentInitiated): Payment {
        return apply {
            id = event.id
            fromAccount = event.fromAccount
            toAccount = event.toAccount
            amount = event.amount
            status = "initiated"
        }
    }

    private fun handle(event: PaymentCompleted): Payment {
        return apply { status = "completed" }
    }
}