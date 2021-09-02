package com.damonkelley.venbo.payments

import com.damonkelley.venbo.Publish
import com.damonkelley.venbo.accounts.AccountCredited
import com.damonkelley.venbo.accounts.AccountDebited
import com.damonkelley.venbo.accounts.CreditAccount
import com.damonkelley.venbo.accounts.DebitAccount

class PaymentProcessManger(val send: Publish) {
    fun on(event: Any) {
        when (event) {
            is PaymentInitiated -> handle(event)
            is AccountDebited -> handle(event)
            is AccountCredited -> handle(event)
        }
    }

    private fun handle(event: PaymentInitiated) {
        send(DebitAccount(id = event.toAccount, fromAccount = event.fromAccount, amount = event.amount))
    }

    private fun handle(event: AccountDebited) {
        send(CreditAccount(id = event.toAccount, toAccount = event.toAccount, amount = event.amount))
    }

    private fun handle(event: AccountCredited) {
        send(CompletePayment(id = event.id))
    }
}