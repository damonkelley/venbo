package com.damonkelley.venbo

import com.damonkelley.venbo.accounts.Command
import com.damonkelley.venbo.accounts.Event
import com.damonkelley.venbo.accounts.OpenAccount
import com.damonkelley.venbo.accounts.adapters.AccountRepository
import com.damonkelley.venbo.infrastructure.Bus
import com.damonkelley.venbo.infrastructure.Envelope
import com.damonkelley.venbo.infrastructure.InMemoryEventStore
import com.damonkelley.venbo.infrastructure.Trace
import com.damonkelley.venbo.payments.CommandHandlers
import com.damonkelley.venbo.payments.CompletePayment
import com.damonkelley.venbo.payments.InitiatePayment
import com.damonkelley.venbo.payments.PaymentInitiated
import com.damonkelley.venbo.payments.PaymentProcessManger
import com.damonkelley.venbo.payments.adapters.PaymentRepository
import com.damonkelley.venbo.views.InMemoryAccountBalanceRepository
import com.damonkelley.venbo.views.ListenForCompletedPayments
import io.ktor.application.install
import io.ktor.http.cio.websocket.send
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.cancel
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

        fun send(trace: () -> Trace = { Trace() }): Publish {
            return { event: Any ->
                bus.publish(Envelope(event, trace()))
            }
        }

        launch {
            bus.subscribe { event ->
                val handlers = CommandHandlers(PaymentRepository(store, event.trace))
                when (event.message) {
                    is InitiatePayment -> handlers.handle(event.message)
                    is CompletePayment -> handlers.handle(event.message)
                }
            }
        }

        launch { bus.subscribe(action = ::println) }

        launch {

            bus.subscribe { command ->
                when (command.message) {
                    is Command -> AccountCommandHandlers(AccountRepository(store, command.trace)).on(command.message)
                }
            }
        }

        launch {
            bus.subscribe {
                when (it.message) {
                    is PaymentInitiated -> PaymentProcessManger(send { Trace(it.trace) }).on(it.message)
                }
            }
        }

        launch {
            UUID.randomUUID().toString()

            val accounts = (1..4).map { UUID.randomUUID().toString() }

            accounts.forEach {
                send()(OpenAccount(id = it))
            }

            while (true) {
                delay((200..2000L).random())

                send()(
                    InitiatePayment(
                        id = UUID.randomUUID().toString(),
                        fromAccount = accounts.random(),
                        toAccount = accounts.random(),
                        amount = BigDecimal("${(1..100).random()}")
                    )
                )

            }
        }

        val repository = InMemoryAccountBalanceRepository()

        launch {
            bus.subscribe { event ->
                when (event.message) {
                    is Event -> ListenForCompletedPayments(repository).on(event.message)
                }
            }
        }

        embeddedServer(Netty, port = 8080) {
            install(WebSockets)

            routing {
                webSocket("/payments") {
                    bus.subscribe {
                        when (val message = it.message) {
                            is PaymentInitiated -> launch {
                                send("${message.fromAccount.slice(0..7)} paid ${message.toAccount.slice(0..7)} $${message.amount}")
                            }
                        }
                    }
                }

                webSocket("/accounts/{id}") {
                    val accountId = call.parameters["id"] ?: cancel("Not found")

                    bus.subscribe({ it.message is Event && it.message.id == accountId}) {
                        when (it.message) {
                            is Event -> launch {
                                val balance = call.parameters["id"]
                                    ?.let { id -> repository.get(id) }
                                    ?.balance
                                    ?: BigDecimal.ZERO

                                send("Balance: $balance")
                            }
                        }
                    }
                }
            }
        }.start(wait = false)
    }
}