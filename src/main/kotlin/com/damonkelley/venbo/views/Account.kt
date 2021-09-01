package com.damonkelley.venbo.views

import com.damonkelley.venbo.Repository
import com.damonkelley.venbo.accounts.AccountCredited
import com.damonkelley.venbo.accounts.AccountDebited
import com.damonkelley.venbo.accounts.AccountOpened
import com.damonkelley.venbo.accounts.Event
import java.math.BigDecimal

data class AccountBalance(val id: String, val balance: BigDecimal)

class ListenForCompletedPayments(private val repository: Repository<AccountBalance>) {
    fun on(event: Event): Result<Unit> {
        return when(event) {
            is AccountDebited -> handle(event)
            is AccountCredited -> handle(event)
            is AccountOpened -> Result.success(Unit)
        }
    }

    private fun handle(event: AccountCredited): Result<Unit> {
        update(event.id, amount = event.amount)

        return Result.success(Unit)
    }

    private fun handle(event: AccountDebited): Result<Unit> {
        update(event.id, amount = -event.amount)

        return Result.success(Unit)
    }

    private fun update(accountId: String, amount: BigDecimal) {
        (repository.get(accountId) ?: AccountBalance(id = accountId, balance = BigDecimal.ZERO))
            .let { repository.save(AccountBalance(id = it.id, balance = it.balance + amount)) }
    }
}

class InMemoryAccountBalanceRepository: Repository<AccountBalance> {
    private val balances = mutableMapOf<String, AccountBalance>()

    fun all(): Collection<AccountBalance> {
        return balances.values
    }

    override fun get(id: String): AccountBalance? {
        return balances[id]
    }

    override fun save(aggregate: AccountBalance) {
        balances[aggregate.id] = aggregate
    }
}