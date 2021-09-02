package com.damonkelley.venbo.accounts

import com.damonkelley.venbo.Repository
import java.math.BigDecimal

class CommandHandlers(private val accounts: Repository<Account>) {
    fun on(command: Command): Result<Unit> = when (command) {
        is CreditAccount -> handle(command)
        is DebitAccount -> handle(command)
        is OpenAccount -> handle(command)
    }

    private fun handle(command: OpenAccount): Result<Unit> {
        accounts.save(Account().open(command.id))
        return Result.success(Unit)
    }

    private fun handle(command: CreditAccount): Result<Unit> {
        accounts.get(command.id)
            ?.credit(command.toAccount, command.amount)
            ?.let(accounts::save)

        return Result.success(Unit)
    }

    private fun handle(command: DebitAccount): Result<Unit> {
        accounts.get(command.id)
            ?.debit(command.fromAccount, command.amount)
            ?.let(accounts::save)

        return Result.success(Unit)
    }
}

class Account(history: List<Event> = emptyList()) {
    var id: String = ""
    private var balance: BigDecimal = BigDecimal.ZERO

    val changes = mutableListOf<Event>()

    init {
        history.forEach {
            when (it) {
                is AccountOpened -> handle(it)
                is AccountDebited -> handle(it)
                is AccountCredited -> handle(it)
            }
        }
    }

    fun open(id: String): Account {
        return AccountOpened(id)
            .apply(::raise)
            .let(::handle)
    }

    fun credit(fromAccount: String, amount: BigDecimal): Account {
        return AccountCredited(
            id = id,
            amount = amount,
            fromAccount = fromAccount
        )
            .apply(::raise)
            .let(::handle)
    }

    fun debit(fromAccount: String, amount: BigDecimal): Account {
        return AccountDebited(
            id = id,
            amount = amount,
            toAccount = fromAccount
        )
            .apply(::raise)
            .let(::handle)
    }

    private fun raise(event: Event) {
        changes.add(event)
    }

    private fun handle(event: AccountOpened): Account {
        return apply {
            id = event.id
        }
    }

    private fun handle(event: AccountCredited): Account {
        return apply {
            balance += event.amount
        }
    }

    private fun handle(event: AccountDebited): Account {
        return apply {
            balance -= event.amount
        }
    }
}

sealed interface Event {
    val id: String
}

data class AccountDebited(override val id: String, val toAccount: String, val amount: BigDecimal) : Event
data class AccountCredited(override val id: String, val fromAccount: String, val amount: BigDecimal) : Event
data class AccountOpened(override val id: String) : Event

sealed interface Command
data class OpenAccount(val id: String) : Command
data class DebitAccount(val id: String, val fromAccount: String, val amount: BigDecimal) : Command
data class CreditAccount(val id: String, val toAccount: String, val amount: BigDecimal) : Command
