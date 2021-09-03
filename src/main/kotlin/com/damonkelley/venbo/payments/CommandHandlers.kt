package com.damonkelley.venbo.payments

import com.damonkelley.venbo.Repository

class CommandHandlers(private val payments: Repository<Payment>) {
    fun handle(command: InitiatePayment) {
        payments.save(
            Payment().initiate(
                id = command.id,
                fromAccount = command.fromAccount,
                toAccount = command.toAccount,
                amount = command.amount
            )
        )
    }

    fun handle(command: CompletePayment) {
        payments.get(command.id)
            ?.let { it.complete() }
            ?.let { payments.save(it) }
    }

    fun handle(command: RejectPayment) {
        payments.get(command.id)
            ?.let { it.reject(command.reason) }
            ?.let { payments.save(it) }
    }
}