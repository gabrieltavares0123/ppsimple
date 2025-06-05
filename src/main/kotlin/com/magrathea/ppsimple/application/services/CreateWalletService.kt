package com.magrathea.ppsimple.application.services

import com.magrathea.ppsimple.application.exceptions.TransactionDomainException
import com.magrathea.ppsimple.application.ports.inbound.CreateWalletUseCase
import com.magrathea.ppsimple.application.ports.outbound.ExternalIdUtils
import com.magrathea.ppsimple.application.ports.outbound.TransactionPersistence
import com.magrathea.ppsimple.application.ports.outbound.WalletPersistence
import com.magrathea.ppsimple.domain.Wallet
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CreateWalletService(
    private val transactionPersistence: TransactionPersistence,
    private val walletPersistence: WalletPersistence,
    private val externalIdUtils: ExternalIdUtils,
) : CreateWalletUseCase {
    private val logger = LoggerFactory.getLogger(CreateWalletService::class.java)

    override fun execute(input: CreateWalletUseCase.Input): UUID {
        logger.info("Start creating a new wallet with $input")
        val externalId = externalIdUtils.random()
        val newWallet = input.toWallet(externalId)
        logger.info("New wallet id created $externalId")

        logger.info("Starting transaction to save new wallet")
        val createdWallet = transactionPersistence.open { walletPersistence.save(wallet = newWallet) }

        if (createdWallet == null) {
            val transactionDomainException =
                TransactionDomainException("Something unexpected happened in the transaction.")
            logger.error(
                "Finish new wallet creation with error: message=${transactionDomainException.message}, " +
                    "details=${transactionDomainException.details}",
            )
            throw transactionDomainException
        } else {
            logger.info("Finish new wallet creation with success $externalId")
        }

        return createdWallet.externalId
    }

    private fun CreateWalletUseCase.Input.toWallet(externalId: UUID) =
        Wallet(
            id = null,
            externalId = externalId,
            ownerName = this.ownerName,
            document = this.document,
            balance = this.balance,
            email = this.email,
            password = this.password,
        )
}
