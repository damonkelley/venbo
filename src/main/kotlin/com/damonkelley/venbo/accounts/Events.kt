package com.damonkelley.venbo.accounts

import java.math.BigDecimal

sealed interface Event {
    val id: String
}

data class AccountDebited(
    override val id: String,
    val paymentId: String,
    val toAccount: String,
    val amount: BigDecimal
) : Event

data class AccountDebitRejected(
    override val id: String,
    val paymentId: String,
    val toAccount: String,
    val amount: BigDecimal,
    val reason: String
) : Event

data class AccountCredited(
    override val id: String,
    val paymentId: String,
    val fromAccount: String,
    val amount: BigDecimal
) : Event

data class AccountOpened(override val id: String) : Event