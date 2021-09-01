package com.damonkelley.venbo

import com.damonkelley.venbo.accounts.AccountCredited
import com.damonkelley.venbo.accounts.AccountDebited
import com.damonkelley.venbo.accounts.CreditAccount
import com.damonkelley.venbo.accounts.DebitAccount
import com.damonkelley.venbo.accounts.OpenAccount
import com.damonkelley.venbo.infrastructure.Bus
import com.damonkelley.venbo.infrastructure.Envelope
import com.damonkelley.venbo.infrastructure.InMemoryEventStore
import com.damonkelley.venbo.payments.CommandHandlers
import com.damonkelley.venbo.payments.CompletePayment
import com.damonkelley.venbo.payments.InitiatePayment
import com.damonkelley.venbo.payments.PaymentProcessManger
import com.damonkelley.venbo.accounts.adapters.AccountRepository
import com.damonkelley.venbo.infrastructure.Trace
import com.damonkelley.venbo.payments.PaymentInitiated
import com.damonkelley.venbo.payments.adapters.PaymentRepository
import com.damonkelley.venbo.views.AccountBalance
import com.damonkelley.venbo.views.InMemoryAccountBalanceRepository
import com.damonkelley.venbo.views.ListenForCompletedPayments
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import com.damonkelley.venbo.accounts.CommandHandlers as AccountCommandHandlers

interface Repository<T> {
    fun get(id: String): T?
    fun save(aggregate: T)
}

typealias Publish = (Any) -> Result<Unit>

interface EventStore {
    fun load(id: String): List<Envelope<Any>>
    fun save(id: String, events: List<Envelope<Any>>)
}


suspend fun main() {
    coroutineScope {
        val bus = Bus<Envelope<Any>>(this)
        val store = InMemoryEventStore(bus::publish)

        val send = { event: Any ->
            bus.publish(Envelope(event, Trace("", "", "")))
        }

        launch {
            val handlers = CommandHandlers(PaymentRepository(store))
            bus.subscribe { event ->
                when (event.message) {
                    is InitiatePayment -> handlers.handle(event.message)
                    is CompletePayment -> handlers.handle(event.message)
                }
            }
        }

//        launch { bus.subscribe(action = ::println) }

        launch {
            val handlers = AccountCommandHandlers(AccountRepository(store))

            bus.subscribe { command ->
                when (command.message) {
                    is OpenAccount -> handlers.handle(command.message)
                    is CreditAccount -> handlers.handle(command.message)
                    is DebitAccount -> handlers.handle(command.message)
                }
            }
        }

        launch {
            bus.subscribe {
                when (it.message) {
                    is PaymentInitiated -> PaymentProcessManger(send).on(it.message)
                }
            }
        }

        launch {
            val id = UUID.randomUUID().toString()
            val accountA = UUID.randomUUID().toString()
            val accountB = UUID.randomUUID().toString()
            val accountC = UUID.randomUUID().toString()
            val accountD = UUID.randomUUID().toString()

            listOf(accountA, accountB, accountC, accountD).forEach {
                send(OpenAccount(id = it))
            }

            send(
                InitiatePayment(
                    id = id,
                    fromAccount = accountA,
                    toAccount = accountB,
                    BigDecimal.TEN
                )
            )

            send(
                InitiatePayment(
                    id = UUID.randomUUID().toString(),
                    fromAccount = accountA,
                    toAccount = accountC,
                    BigDecimal("500.30")
                )
            )

            send(
                InitiatePayment(
                    id = UUID.randomUUID().toString(),
                    fromAccount = accountB,
                    toAccount = accountC,
                    BigDecimal("20.50")
                )
            )

            send(
                InitiatePayment(
                    id = UUID.randomUUID().toString(),
                    fromAccount = accountC,
                    toAccount = accountD,
                    BigDecimal("34.76")
                )
            )
        }

        launch {
            val repository = InMemoryAccountBalanceRepository()
            bus.subscribe { event ->
                when (event.message) {
                    is AccountDebited -> ListenForCompletedPayments(repository).handle(event.message).also {
                        repository.get(event.message.id)?.let(::showBalance)
                    }
                    is AccountCredited -> ListenForCompletedPayments(repository).handle(event.message).also {
                        repository.get(event.message.id)?.let(::showBalance)
                    }
                }
            }

            println(store.streams)
        }
    }
}

fun showBalance(balance: AccountBalance) {
    println("-".repeat(80) )
    println("${balance.id} | ${balance.balance}")
    println("-".repeat(80) )
    println()
}