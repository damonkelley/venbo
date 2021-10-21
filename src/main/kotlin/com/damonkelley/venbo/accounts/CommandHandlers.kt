package com.damonkelley.venbo.accounts

import com.damonkelley.venbo.Repository

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
            ?.credit(
                fromAccount = command.fromAccount,
                paymentId = command.paymentId,
                amount = command.amount
            )
            ?.let(accounts::save)

        return Result.success(Unit)
    }

    private fun handle(command: DebitAccount): Result<Unit> {
        accounts.get(command.id)
            ?.debit(
                fromAccount = command.toAccount,
                paymentId = command.paymentId,
                amount = command.amount
            )
            ?.let(accounts::save)

        return Result.success(Unit)
    }
}