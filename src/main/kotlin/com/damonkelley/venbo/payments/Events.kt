package com.damonkelley.venbo.payments

import java.math.BigDecimal

sealed interface Event
data class PaymentInitiated(
    val id: String,
    val fromAccount: String,
    val toAccount: String,
    val amount: BigDecimal,
) : Event

data class PaymentCompleted(
    val id: String,
    val fromAccount: String,
    val toAccount: String,
    val amount: BigDecimal,
) : Event