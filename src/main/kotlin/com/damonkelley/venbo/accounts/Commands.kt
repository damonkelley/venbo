package com.damonkelley.venbo.accounts

import java.math.BigDecimal

sealed interface Command
data class OpenAccount(val id: String) : Command
data class DebitAccount(val id: String, val paymentId: String, val toAccount: String, val amount: BigDecimal) : Command
data class CreditAccount(val id: String, val paymentId: String, val fromAccount: String, val amount: BigDecimal) :
    Command