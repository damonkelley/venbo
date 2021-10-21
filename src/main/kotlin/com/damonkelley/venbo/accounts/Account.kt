package com.damonkelley.venbo.accounts

import java.math.BigDecimal

class Account(history: List<Event> = emptyList()) {
    var id: String = ""
    private var balance: BigDecimal = BigDecimal.ZERO

    val changes = mutableListOf<Event>()

    init {
        history.forEach {
            when (it) {
                is AccountOpened -> handle(it)
                is AccountDebited -> handle(it)
                is AccountCredited -> handle(it)
                is AccountDebitRejected -> Unit
            }
        }
    }

    fun open(id: String): Account {
        return AccountOpened(id)
            .apply(::raise)
            .let(::handle)
    }

    // TODO: Refactor to accept a datastructure
    fun credit(fromAccount: String, paymentId: String, amount: BigDecimal): Account {
        return AccountCredited(
            id = id,
            amount = amount,
            paymentId = paymentId,
            fromAccount = fromAccount
        )
            .apply(::raise)
            .let(::handle)
    }

    fun debit(fromAccount: String, paymentId: String, amount: BigDecimal): Account {
        if (balance - amount < BigDecimal.ZERO) {
            return AccountDebitRejected(
                id = id,
                amount = amount,
                paymentId = paymentId,
                toAccount = fromAccount,
                reason = "Insufficient funds"
            ).apply(::raise)
                .let(::handle)

        }
        return AccountDebited(
            id = id,
            amount = amount,
            paymentId = paymentId,
            toAccount = fromAccount
        )
            .apply(::raise)
            .let(::handle)
    }

    private fun raise(event: Event) {
        changes.add(event)
    }

    private fun handle(event: AccountOpened): Account {
        return apply {
            id = event.id
        }
    }

    private fun handle(event: AccountCredited): Account {
        return apply {
            balance += event.amount
        }
    }

    private fun handle(event: AccountDebited): Account {
        return apply {
            balance -= event.amount
        }
    }

    private fun handle(event: AccountDebitRejected): Account {
        return this
    }
}
