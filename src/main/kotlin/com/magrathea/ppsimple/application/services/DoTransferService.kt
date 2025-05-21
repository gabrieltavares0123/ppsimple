package com.magrathea.ppsimple.application.services

import com.magrathea.ppsimple.application.exceptions.InsufficientBalanceDomainException
import com.magrathea.ppsimple.application.exceptions.PayeeNotFoundDomainException
import com.magrathea.ppsimple.application.exceptions.PayerEligibilityDomainException
import com.magrathea.ppsimple.application.exceptions.PayerNotFoundDomainException
import com.magrathea.ppsimple.application.exceptions.TransactionDomainException
import com.magrathea.ppsimple.application.exceptions.UnauthorizedTransferDomainException
import com.magrathea.ppsimple.application.ports.inbound.DoTransferUseCase
import com.magrathea.ppsimple.application.ports.outbound.ExternalIdUtils
import com.magrathea.ppsimple.application.ports.outbound.NotificationMessagingProducer
import com.magrathea.ppsimple.application.ports.outbound.SendNotificationGateway
import com.magrathea.ppsimple.application.ports.outbound.TransactionPersistence
import com.magrathea.ppsimple.application.ports.outbound.TransferPersistence
import com.magrathea.ppsimple.application.ports.outbound.VerifyAuthorizationGateway
import com.magrathea.ppsimple.application.ports.outbound.WalletPersistence
import com.magrathea.ppsimple.domain.DocumentType
import com.magrathea.ppsimple.domain.Notification
import com.magrathea.ppsimple.domain.Transfer
import com.magrathea.ppsimple.domain.TransferType
import com.magrathea.ppsimple.domain.Wallet
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class DoTransferService(
    private val transactionPersistence: TransactionPersistence,
    private val transferPersistence: TransferPersistence,
    private val walletPersistence: WalletPersistence,
    private val verifyAuthorizationGateway: VerifyAuthorizationGateway,
    private val sendNotificationGateway: SendNotificationGateway,
    private val notificationProducer: NotificationMessagingProducer,
    private val externalIdUtils: ExternalIdUtils
) : DoTransferUseCase {

    private val logger = LoggerFactory.getLogger(DoTransferService::class.java)

    override fun execute(input: DoTransferUseCase.Input): UUID {

        verifyAuthorization()

        val payerWallet = findPayerWalletByExternalId(payerExternalId = input.payer)

        val payeeWallet = findPayeeWalletByExternalId(payeeExternalId = input.payee)

        verifyPayerEligibility(payerWallet)

        verifyPayerWalletBalance(payerWallet = payerWallet, transferValue = input.value)

        val transfer = transactionPersistence.open {
            logger.info("Updating payer balance.")
            val payerNewBalance = payerWallet.balance - input.value
            walletPersistence.updateBalance(
                externalId = payerWallet.externalId,
                balance = payerNewBalance
            )

            logger.info("Updating payee balance.")
            val payeeNewBalance: BigDecimal = payeeWallet.balance + input.value
            walletPersistence.updateBalance(
                externalId = payeeWallet.externalId,
                balance = payeeNewBalance
            )

            logger.info("Persisting transaction in database.")
            val transferExternalId = externalIdUtils.random()
            val newTransfer = Transfer(
                id = null,
                externalId = transferExternalId,
                payerExternalId = payerWallet.externalId,
                payeeExternalId = payeeWallet.externalId,
                value = input.value,
                type = if (payeeWallet.document.type == DocumentType.CPF) TransferType.NATURAL_TO_NATURAL else TransferType.NATURAL_TO_LEGAL,
                createdAt = LocalDateTime.now(),
            )

            transferPersistence.save(newTransfer)
        }

        if (transfer == null)
            throw TransactionDomainException("Something unexpected happened in the transaction.")

        sendTransferNotification(transfer)

        return transfer.externalId!!
    }

    private fun verifyAuthorization() {
        logger.info("Started verifying if transfer is authorized.")

        val authorization = verifyAuthorizationGateway.isAuthorized()
        if (authorization == null || authorization.not()) {
            throw UnauthorizedTransferDomainException(
                message = "This transfer is unauthorized."
            ).also {
                logger.error("Failed to verify if transfer is authorized with ${it.message}")
            }
        }

        logger.info("Finished verifying if is authorized. The result is $authorization.")
    }

    private fun findPayerWalletByExternalId(payerExternalId: UUID): Wallet {
        logger.info("Started retrieving payer with externalId=${payerExternalId}.")
        val foundPayer = walletPersistence.findBy(payerExternalId)
        return if (foundPayer == null) {
            throw PayerNotFoundDomainException(
                message = "Payer not found.",
                payerExternalId = payerExternalId
            ).also {
                logger.error("Failed retrieving payer with message=${it.message}.")
            }
        } else {
            logger.info("Finished retrieving payer with $foundPayer.")
            foundPayer
        }
    }

    private fun findPayeeWalletByExternalId(payeeExternalId: UUID): Wallet {
        logger.info("Started retrieving payee with externalId=${payeeExternalId}.")
        val foundPayee = walletPersistence.findBy(payeeExternalId)
        return if (foundPayee == null) {
            throw PayeeNotFoundDomainException(
                message = "Payee not found.",
                payeeExternalId = payeeExternalId
            ).also {
                logger.error("Failed retrieving payee with message=${it.message}.")
            }
        } else {
            logger.info("Finished retrieving payee with $foundPayee.")
            foundPayee
        }
    }

    private fun verifyPayerEligibility(payer: Wallet) {
        if (payer.document.type != DocumentType.CPF) {
            throw PayerEligibilityDomainException(
                message = "Invalid payer.",
                reason = "Payer should not be legal for this kind of transaction."
            ).also {
                logger.error("Payer should not be Legal for kind of transaction. Message=${it.message}")
            }
        }
    }

    private fun verifyPayerWalletBalance(payerWallet: Wallet, transferValue: BigDecimal) {
        logger.info("Started checking payer balance.")
        if (
            payerWallet.balance.compareTo(BigDecimal(0)) == 0 ||
            payerWallet.balance.compareTo(transferValue) < 0
        ) {
            throw InsufficientBalanceDomainException(
                message = "Insufficient balance."
            ).also {
                logger.error("Failed to check payer balance with message=${it.message}")
            }
        }
        logger.info("Finished checking payer balance with success.")
    }

    private fun sendTransferNotification(transfer: Transfer?) {
        if (transfer != null) {
            val notification = transfer.toNotification()
            val sendResult = sendNotificationGateway.send(notification)

            if (sendResult.not()) {
                notificationProducer.produce(notification)
            }
        }
    }

    private fun Transfer.toNotification() = Notification(
        id = this.id,
        externalId = this.externalId,
        payerExternalId = this.payerExternalId,
        payeeExternalId = this.payeeExternalId,
        value = this.value,
        type = this.type,
        createdAt = this.createdAt
    )

}