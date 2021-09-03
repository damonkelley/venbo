package com.damonkelley.venbo.payments

import java.math.BigDecimal

sealed interface Command
data class InitiatePayment(
    val id: String,
    val fromAccount: String,
    val toAccount: String,
    val amount: BigDecimal,
) : Command

data class CompletePayment(
    val id: String,
) : Command

data class RejectPayment(
    val id: String,
    val reason: String,
) : Command
