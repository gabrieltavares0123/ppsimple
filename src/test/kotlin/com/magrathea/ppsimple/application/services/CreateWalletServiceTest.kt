package com.magrathea.ppsimple.application.services

import com.magrathea.ppsimple.application.exceptions.TransactionDomainException
import com.magrathea.ppsimple.application.ports.inbound.CreateWalletUseCase
import com.magrathea.ppsimple.application.ports.outbound.ExternalIdUtils
import com.magrathea.ppsimple.application.ports.outbound.TransactionPersistence
import com.magrathea.ppsimple.application.ports.outbound.WalletPersistence
import com.magrathea.ppsimple.domain.Document
import com.magrathea.ppsimple.domain.Wallet
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals

class CreateWalletServiceTest {
    private val transactionPersistence = mockk<TransactionPersistence>()
    private val walletPersistence = mockk<WalletPersistence>()
    private val externalIdUtils = mockk<ExternalIdUtils>()

    private val createWalletService =
        CreateWalletService(
            transactionPersistence = transactionPersistence,
            walletPersistence = walletPersistence,
            externalIdUtils = externalIdUtils,
        )

    @Test
    fun `should save a new wallet with success`() {
        val externalId = UUID.fromString("8cdd3394-9a78-493a-8f7f-141261103604")
        val document = Document.create("000.000.000-00")
        val balance = BigDecimal(2000)

        val input =
            CreateWalletUseCase.Input(
                // Overrides in service when it calls externalIdUtils.random().
                externalId = null,
                ownerName = "payer",
                document = document,
                balance = balance,
                email = "payer@mail.com",
                password = "12345678",
            )

        val wallet =
            Wallet(
                id = null,
                // Overrides in service when it calls externalIdUtils.random().
                externalId = externalId,
                ownerName = "payer",
                document = document,
                balance = balance,
                email = "payer@mail.com",
                password = "12345678",
            )

        every { externalIdUtils.random() } returns externalId
        every { transactionPersistence.open<Wallet>(any()) } answers { firstArg<() -> Wallet>().invoke() }
        every { walletPersistence.save(wallet) } returns wallet

        val resultExternalId = createWalletService.execute(input)

        verifyOrder {
            externalIdUtils.random()
            transactionPersistence.open<Wallet>(any())
            walletPersistence.save(wallet)
        }

        assertEquals(actual = resultExternalId, expected = externalId)
    }

    @Test
    fun `should throw TransactionDomainException when something unexpected happens in the transaction `() {
        val externalId = UUID.fromString("8cdd3394-9a78-493a-8f7f-141261103604")
        val document = Document.create("000.000.000-00")
        val balance = BigDecimal(2000)

        val input =
            CreateWalletUseCase.Input(
                // Overrides in service when it calls externalIdUtils.random().
                externalId = null,
                ownerName = "payer",
                document = document,
                balance = balance,
                email = "payer@mail.com",
                password = "12345678",
            )

        every { externalIdUtils.random() } returns externalId
        every { transactionPersistence.open<Wallet>(any()) } returns null

        val exception =
            assertThrows<TransactionDomainException> {
                createWalletService.execute(input)
            }

        verifyOrder {
            externalIdUtils.random()
            transactionPersistence.open<Wallet>(any())
        }

        with(exception) {
            assertInstanceOf<TransactionDomainException>(exception)
            assertEquals(actual = this.message, expected = "Something unexpected happened in the transaction.")
            assertEquals(actual = this.details["reason"], expected = "Something went wrong with this transaction.")
        }
    }
}
