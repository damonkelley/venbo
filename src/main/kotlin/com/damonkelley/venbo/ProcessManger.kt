package com.damonkelley.venbo

import com.damonkelley.venbo.accounts.AccountCredited
import com.damonkelley.venbo.accounts.AccountDebitRejected
import com.damonkelley.venbo.accounts.AccountDebited
import com.damonkelley.venbo.accounts.CreditAccount
import com.damonkelley.venbo.accounts.DebitAccount
import com.damonkelley.venbo.payments.CompletePayment
import com.damonkelley.venbo.payments.PaymentInitiated
import com.damonkelley.venbo.payments.RejectPayment

class PaymentProcessManger(val send: Publish) {
    fun on(event: Any) {
        when (event) {
            is PaymentInitiated -> handle(event)
            is AccountDebited -> handle(event)
            is AccountDebitRejected -> handle(event)
            is AccountCredited -> handle(event)
        }
    }

    private fun handle(event: PaymentInitiated) {
        send(DebitAccount(id = event.toAccount, paymentId = event.id, toAccount = event.fromAccount, amount = event.amount))
    }

    private fun handle(event: AccountDebited) {
        send(CreditAccount(id = event.toAccount, paymentId = event.paymentId, fromAccount = event.toAccount, amount = event.amount))
    }

    private fun handle(event: AccountCredited) {
        send(CompletePayment(id = event.paymentId))
    }

    private fun handle(event: AccountDebitRejected) {
        send(RejectPayment(id = event.paymentId, reason = event.reason))
    }
}