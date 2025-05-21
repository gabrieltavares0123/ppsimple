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
import com.magrathea.ppsimple.domain.Document
import com.magrathea.ppsimple.domain.Notification
import com.magrathea.ppsimple.domain.Transfer
import com.magrathea.ppsimple.domain.TransferType
import com.magrathea.ppsimple.domain.Wallet
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

class DoTransferServiceTest {

    private val transactionPersistence = mockk<TransactionPersistence>()
    private val transferPersistence = mockk<TransferPersistence>()
    private val walletPersistence = mockk<WalletPersistence>()
    private val verifyAuthorizationGateway = mockk<VerifyAuthorizationGateway>()
    private val sendNotificationGateway = mockk<SendNotificationGateway>()
    private val notificationProducer = mockk<NotificationMessagingProducer>()
    private val externalIdUtils = mockk<ExternalIdUtils>()

    private val doTransferService = DoTransferService(
        transactionPersistence = transactionPersistence,
        transferPersistence = transferPersistence,
        walletPersistence = walletPersistence,
        verifyAuthorizationGateway = verifyAuthorizationGateway,
        sendNotificationGateway = sendNotificationGateway,
        notificationProducer = notificationProducer,
        externalIdUtils = externalIdUtils,
    )

    @BeforeEach
    fun setUp() {
        mockkStatic(LocalDateTime::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(LocalDateTime::class)
    }

    @Test
    fun `should execute a transaction with success`() {
        val payerDocument = Document.create("000.000.000-00")
        val payeeDocument = Document.create("000.000.000-01")

        val payerExternalId = UUID.fromString("8cdd3394-9a78-493a-8f7f-141261103604")
        val payeeExternalId = UUID.fromString("737921f6-5805-431e-9086-c0078ffe5afe")

        val payerWallet = Wallet(
            id = 1,
            externalId = payerExternalId,
            ownerName = "payer",
            document = payerDocument,
            balance = BigDecimal(2000),
            email = "payer@mail.com",
            password = "12345678"
        )

        val payeeWallet = Wallet(
            id = 2,
            externalId = payeeExternalId,
            ownerName = "payee",
            document = payeeDocument,
            balance = BigDecimal(2000),
            email = "payee@mail.com",
            password = "12345678"
        )

        val transferValue = BigDecimal(100)
        val transferExternalId = UUID.fromString("ec23bc86-3f69-4629-942f-37c68bc8b6dd")
        val now = LocalDateTime.of(2025, 5, 21, 11, 37)

        every { externalIdUtils.random() } returns transferExternalId
        every { LocalDateTime.now() } returns now

        val transfer = Transfer(
            id = null,
            externalId = transferExternalId, // Overrides in service when it calls externalIdUtils.random().
            payerExternalId = payerExternalId,
            payeeExternalId = payeeExternalId,
            value = transferValue,
            type = TransferType.NATURAL_TO_NATURAL,
            createdAt = now // Overrides in service when it calls LocalDateTime.now().
        )

        val notification = Notification(
            id = null,
            externalId = transferExternalId, // Overrides inside service when it calls externalIdUtils.random().
            payerExternalId = payerExternalId,
            payeeExternalId = payeeExternalId,
            value = transferValue,
            type = TransferType.NATURAL_TO_NATURAL,
            createdAt = now // Overrides in service when it calls LocalDateTime.now().
        )

        val input = DoTransferUseCase.Input(
            payer = payerExternalId,
            payee = payeeExternalId,
            value = transferValue
        )

        every { verifyAuthorizationGateway.isAuthorized() } returns true
        every { walletPersistence.findBy(externalId = payerExternalId) } returns payerWallet
        every { walletPersistence.findBy(externalId = payeeExternalId) } returns payeeWallet
        every { transactionPersistence.open<Transfer>(any()) } answers { firstArg<() -> Transfer>().invoke() }
        val payerNewBalance = payerWallet.balance - input.value
        justRun {
            walletPersistence.updateBalance(
                externalId = payerWallet.externalId,
                balance = payerNewBalance
            )
        }
        val payeeNewBalance = payeeWallet.balance + input.value
        justRun {
            walletPersistence.updateBalance(
                externalId = payeeWallet.externalId,
                balance = payeeNewBalance
            )
        }
        every { transferPersistence.save(transfer = transfer) } returns transfer
        every { sendNotificationGateway.send(notification) } returns true


        val resultTransferExternalId = doTransferService.execute(input)

        verifyOrder {
            verifyAuthorizationGateway.isAuthorized()
            walletPersistence.findBy(payerExternalId)
            walletPersistence.findBy(payeeExternalId)
            transactionPersistence.open<Transfer>(any())
            walletPersistence.updateBalance(
                externalId = payerWallet.externalId,
                balance = payerNewBalance
            )
            walletPersistence.updateBalance(
                externalId = payeeWallet.externalId,
                balance = payeeNewBalance
            )
            transferPersistence.save(transfer)
            sendNotificationGateway.send(notification)
        }

        assertEquals(actual = resultTransferExternalId, expected = transfer.externalId)
    }

    @Test
    fun `should throw TransactionDomainException when something unexpected happens executing the transaction`() {
        val payerDocument = Document.create("000.000.000-00")
        val payeeDocument = Document.create("000.000.000-01")

        val payerExternalId = UUID.fromString("8cdd3394-9a78-493a-8f7f-141261103604")
        val payeeExternalId = UUID.fromString("737921f6-5805-431e-9086-c0078ffe5afe")

        val payerWallet = Wallet(
            id = 1,
            externalId = payerExternalId,
            ownerName = "payer",
            document = payerDocument,
            balance = BigDecimal(2000),
            email = "payer@mail.com",
            password = "12345678"
        )

        val payeeWallet = Wallet(
            id = 2,
            externalId = payeeExternalId,
            ownerName = "payee",
            document = payeeDocument,
            balance = BigDecimal(2000),
            email = "payee@mail.com",
            password = "12345678"
        )

        val transferValue = BigDecimal(100)
        val transferExternalId = UUID.fromString("ec23bc86-3f69-4629-942f-37c68bc8b6dd")
        val now = LocalDateTime.of(2025, 5, 21, 11, 37)

        every { externalIdUtils.random() } returns transferExternalId
        every { LocalDateTime.now() } returns now

        val input = DoTransferUseCase.Input(
            payer = payerExternalId,
            payee = payeeExternalId,
            value = transferValue
        )

        every { verifyAuthorizationGateway.isAuthorized() } returns true
        every { walletPersistence.findBy(externalId = payerExternalId) } returns payerWallet
        every { walletPersistence.findBy(externalId = payeeExternalId) } returns payeeWallet
        every { transactionPersistence.open<Transfer>(any()) } returns null // Makes it throws the exception.

        val exception = assertThrows<TransactionDomainException> {
            doTransferService.execute(input)
        }

        verifyOrder {
            verifyAuthorizationGateway.isAuthorized()
            walletPersistence.findBy(payerExternalId)
            walletPersistence.findBy(payeeExternalId)
            transactionPersistence.open<Unit>(any())
        }

        with(exception) {
            assertInstanceOf<TransactionDomainException>(exception)
            assertEquals(actual = this.message, expected = "Something unexpected happened in the transaction.")
            assertEquals(actual = this.details["reason"], expected = "Something went wrong with this transaction.")
        }
    }

    @Test
    fun `should send the transfer notification to a queue when notification gateway is unavailable`() {
        val payerDocument = Document.create("000.000.000-00")
        val payeeDocument = Document.create("000.000.000-01")

        val payerExternalId = UUID.fromString("8cdd3394-9a78-493a-8f7f-141261103604")
        val payeeExternalId = UUID.fromString("737921f6-5805-431e-9086-c0078ffe5afe")

        val payerWallet = Wallet(
            id = 1,
            externalId = payerExternalId,
            ownerName = "payer",
            document = payerDocument,
            balance = BigDecimal(2000),
            email = "payer@mail.com",
            password = "12345678"
        )

        val payeeWallet = Wallet(
            id = 2,
            externalId = payeeExternalId,
            ownerName = "payee",
            document = payeeDocument,
            balance = BigDecimal(2000),
            email = "payee@mail.com",
            password = "12345678"
        )

        val transferValue = BigDecimal(100)
        val transferExternalId = UUID.fromString("ec23bc86-3f69-4629-942f-37c68bc8b6dd")
        val now = LocalDateTime.of(2025, 5, 21, 11, 37)

        every { externalIdUtils.random() } returns transferExternalId
        every { LocalDateTime.now() } returns now

        val transfer = Transfer(
            id = null,
            externalId = transferExternalId, // Overrides in service when it calls externalIdUtils.random().
            payerExternalId = payerExternalId,
            payeeExternalId = payeeExternalId,
            value = transferValue,
            type = TransferType.NATURAL_TO_NATURAL,
            createdAt = now // Overrides in service when it calls LocalDateTime.now().
        )

        val notification = Notification(
            id = null,
            externalId = transferExternalId, // Overrides inside service when it calls externalIdUtils.random().
            payerExternalId = payerExternalId,
            payeeExternalId = payeeExternalId,
            value = transferValue,
            type = TransferType.NATURAL_TO_NATURAL,
            createdAt = now // Overrides in service when it calls LocalDateTime.now().
        )

        val input = DoTransferUseCase.Input(
            payer = payerExternalId,
            payee = payeeExternalId,
            value = transferValue
        )

        every { verifyAuthorizationGateway.isAuthorized() } returns true
        every { walletPersistence.findBy(externalId = payerExternalId) } returns payerWallet
        every { walletPersistence.findBy(externalId = payeeExternalId) } returns payeeWallet
        every { transactionPersistence.open<Transfer>(any()) } answers { firstArg<() -> Transfer>().invoke() }
        val payerNewBalance = payerWallet.balance - input.value
        justRun {
            walletPersistence.updateBalance(
                externalId = payerWallet.externalId,
                balance = payerNewBalance
            )
        }
        val payeeNewBalance = payeeWallet.balance + input.value
        justRun {
            walletPersistence.updateBalance(
                externalId = payeeWallet.externalId,
                balance = payeeNewBalance
            )
        }
        every { transferPersistence.save(transfer = transfer) } returns transfer
        every { sendNotificationGateway.send(notification = notification) } returns false // Makes it falls to the producer.
        justRun { notificationProducer.produce(notification = notification) }


        val resultTransferExternalId = doTransferService.execute(input)

        verifyOrder {
            verifyAuthorizationGateway.isAuthorized()
            walletPersistence.findBy(payerExternalId)
            walletPersistence.findBy(payeeExternalId)
            transactionPersistence.open<Transfer>(any())
            walletPersistence.updateBalance(
                externalId = payerWallet.externalId,
                balance = payerNewBalance
            )
            walletPersistence.updateBalance(
                externalId = payeeWallet.externalId,
                balance = payeeNewBalance
            )
            transferPersistence.save(transfer)
            sendNotificationGateway.send(notification)
            notificationProducer.produce(notification)
        }

        assertEquals(actual = resultTransferExternalId, expected = transfer.externalId)
    }

    @Test
    fun `should throw UnauthorizedTransferDomainException when transfer is unauthorized`() {
        val input = DoTransferUseCase.Input(
            payer = UUID.fromString("8cdd3394-9a78-493a-8f7f-141261103604"),
            payee = UUID.fromString("737921f6-5805-431e-9086-c0078ffe5afe"),
            value = BigDecimal(100)
        )

        every { verifyAuthorizationGateway.isAuthorized() } returns false

        val exception = assertThrows<UnauthorizedTransferDomainException> {
            doTransferService.execute(input)
        }

        verify(exactly = 1) { verifyAuthorizationGateway.isAuthorized() }
        with(exception) {
            assertInstanceOf<UnauthorizedTransferDomainException>(exception)
            assertEquals(actual = this.message, expected = "This transfer is unauthorized.")
            assertEquals(actual = this.details["reason"], expected = "This transaction is not authorized.")
        }
    }

    @Test
    fun `should throw PayerNotFoundDomainException when trying to find a non existing payer`() {
        val payerExternalId = UUID.fromString("8cdd3394-9a78-493a-8f7f-141261103604")
        val payeeExternalId = UUID.fromString("737921f6-5805-431e-9086-c0078ffe5afe")

        val input = DoTransferUseCase.Input(
            payer = payerExternalId,
            payee = payeeExternalId,
            value = BigDecimal(100)
        )

        every { verifyAuthorizationGateway.isAuthorized() } returns true
        every { walletPersistence.findBy(payerExternalId) } returns null

        val exception = assertThrows<PayerNotFoundDomainException> {
            doTransferService.execute(input)
        }

        verifyOrder {
            verifyAuthorizationGateway.isAuthorized()
            walletPersistence.findBy(payerExternalId)
        }

        with(exception) {
            assertInstanceOf<PayerNotFoundDomainException>(exception)
            assertEquals(actual = this.message, expected = "Payer not found.")
            assertEquals(
                actual = this.details["reason"],
                expected = "Payee with id $payerExternalId doesn't exists."
            )
        }
    }

    @Test
    fun `should throw PayerNotFoundDomainException when trying to find a non existing payee`() {
        val payerDocument = Document.create("000.000.000-00")

        val payerExternalId = UUID.fromString("8cdd3394-9a78-493a-8f7f-141261103604")
        val payeeExternalId = UUID.fromString("737921f6-5805-431e-9086-c0078ffe5afe")

        val payerWallet = Wallet(
            id = 1,
            externalId = payerExternalId,
            ownerName = "payer",
            document = payerDocument,
            balance = BigDecimal(2000),
            email = "payer@mail.com",
            password = "12345678"
        )

        val input = DoTransferUseCase.Input(
            payer = payerExternalId,
            payee = payeeExternalId,
            value = BigDecimal(100)
        )

        every { verifyAuthorizationGateway.isAuthorized() } returns true
        every { walletPersistence.findBy(payerExternalId) } returns payerWallet
        every { walletPersistence.findBy(payeeExternalId) } returns null

        val exception = assertThrows<PayeeNotFoundDomainException> {
            doTransferService.execute(input)
        }

        verifyOrder {
            verifyAuthorizationGateway.isAuthorized()
            walletPersistence.findBy(payerExternalId)
            walletPersistence.findBy(payeeExternalId)
        }

        with(exception) {
            assertInstanceOf<PayeeNotFoundDomainException>(exception)
            assertEquals(actual = this.message, expected = "Payee not found.")
            assertEquals(
                actual = this.details["reason"],
                expected = "Payee with id $payeeExternalId doesn't exists."
            )
        }
    }

    @Test
    fun `should throw PayerEligibilityDomainException when payer is legal`() {
        val payerDocument = Document.create("00.000.000/0000-00")
        val payeeDocument = Document.create("000.000.000-01")

        val payerExternalId = UUID.fromString("8cdd3394-9a78-493a-8f7f-141261103604")
        val payeeExternalId = UUID.fromString("737921f6-5805-431e-9086-c0078ffe5afe")

        val payerWallet = Wallet(
            id = 1,
            externalId = payerExternalId,
            ownerName = "payer",
            document = payerDocument,
            balance = BigDecimal(2000),
            email = "payer@mail.com",
            password = "12345678"
        )

        val payeeWallet = Wallet(
            id = 2,
            externalId = payeeExternalId,
            ownerName = "payee",
            document = payeeDocument,
            balance = BigDecimal(2000),
            email = "payee@mail.com",
            password = "12345678"
        )

        val input = DoTransferUseCase.Input(
            payer = payerExternalId,
            payee = payeeExternalId,
            value = BigDecimal(100)
        )

        every { verifyAuthorizationGateway.isAuthorized() } returns true
        every { walletPersistence.findBy(payerExternalId) } returns payerWallet
        every { walletPersistence.findBy(payeeExternalId) } returns payeeWallet

        val exception = assertThrows<PayerEligibilityDomainException> {
            doTransferService.execute(input)
        }

        verifyOrder {
            verifyAuthorizationGateway.isAuthorized()
            walletPersistence.findBy(payerExternalId)
            walletPersistence.findBy(payeeExternalId)
        }

        with(exception) {
            assertInstanceOf<PayerEligibilityDomainException>(exception)
            assertEquals(actual = this.message, expected = "Invalid payer.")
            assertEquals(
                actual = this.details["reason"],
                expected = "Payer should not be legal for this kind of transaction."
            )
        }
    }

    @Test
    fun `should throw InsufficientBalanceDomainException when payer don't have enough balance`() {
        val payerDocument = Document.create("000.000.000-00")
        val payeeDocument = Document.create("000.000.000-01")

        val payerExternalId = UUID.fromString("8cdd3394-9a78-493a-8f7f-141261103604")
        val payeeExternalId = UUID.fromString("737921f6-5805-431e-9086-c0078ffe5afe")

        val payerWallet = Wallet(
            id = 1,
            externalId = payerExternalId,
            ownerName = "payer",
            document = payerDocument,
            balance = BigDecimal(0),
            email = "payer@mail.com",
            password = "12345678"
        )

        val payeeWallet = Wallet(
            id = 2,
            externalId = payeeExternalId,
            ownerName = "payee",
            document = payeeDocument,
            balance = BigDecimal(2000),
            email = "payee@mail.com",
            password = "12345678"
        )

        val input = DoTransferUseCase.Input(
            payer = payerExternalId,
            payee = payeeExternalId,
            value = BigDecimal(100)
        )

        every { verifyAuthorizationGateway.isAuthorized() } returns true
        every { walletPersistence.findBy(payerExternalId) } returns payerWallet
        every { walletPersistence.findBy(payeeExternalId) } returns payeeWallet

        val exception = assertThrows<InsufficientBalanceDomainException> {
            doTransferService.execute(input)
        }

        verifyOrder {
            verifyAuthorizationGateway.isAuthorized()
            walletPersistence.findBy(payerExternalId)
            walletPersistence.findBy(payeeExternalId)
        }

        with(exception) {
            assertInstanceOf<InsufficientBalanceDomainException>(exception)
            assertEquals(actual = this.message, expected = "Insufficient balance.")
            assertEquals(
                actual = this.details["reason"],
                expected = "Payer don't have enough balance in the wallet for this transaction."
            )
        }
    }
}