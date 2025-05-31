package com.magrathea.ppsimple.application.services

import com.magrathea.ppsimple.application.exceptions.TransactionDomainException
import com.magrathea.ppsimple.application.ports.inbound.CreateWalletUseCase
import com.magrathea.ppsimple.application.ports.outbound.ExternalIdUtils
import com.magrathea.ppsimple.application.ports.outbound.TransactionPersistence
import com.magrathea.ppsimple.application.ports.outbound.WalletPersistence
import com.magrathea.ppsimple.domain.Wallet
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CreateWalletService(
    private val transactionPersistence: TransactionPersistence,
    private val walletPersistence: WalletPersistence,
    private val externalIdUtils: ExternalIdUtils
) : CreateWalletUseCase {

    override fun execute(input: CreateWalletUseCase.Input): UUID {
        val externalId = externalIdUtils.random()
        val newWallet = input.toWallet(externalId)

        val createdWallet = transactionPersistence.open {
            walletPersistence.save(wallet = newWallet)
        }

        if (createdWallet == null)
            throw TransactionDomainException("Something unexpected happened in the transaction.")

        return createdWallet.externalId
    }

    private fun CreateWalletUseCase.Input.toWallet(externalId: UUID) = Wallet(
        id = null,
        externalId = externalId,
        ownerName = this.ownerName,
        document = this.document,
        balance = this.balance,
        email = this.email,
        password = this.password
    )

}