package com.damonkelley.venbo.payments

import com.damonkelley.venbo.Publish
import com.damonkelley.venbo.accounts.CreditAccount
import com.damonkelley.venbo.accounts.DebitAccount

class PaymentProcessManger(val send: Publish) {
    fun on(event: PaymentInitiated) {
        handle(event)
    }

    private fun handle(event: PaymentInitiated) {
        send(DebitAccount(id = event.toAccount, amount = event.amount))
        send(CreditAccount(id = event.fromAccount, amount = event.amount))
        send(CompletePayment(id = event.id))
    }
}