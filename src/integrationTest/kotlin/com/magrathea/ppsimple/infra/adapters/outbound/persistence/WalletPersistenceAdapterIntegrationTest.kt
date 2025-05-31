package com.magrathea.ppsimple.infra.adapters.outbound.persistence

import com.magrathea.ppsimple.application.ports.outbound.WalletPersistence
import com.magrathea.ppsimple.domain.Document
import com.magrathea.ppsimple.domain.Wallet
import com.magrathea.ppsimple.infra.BaseIntegrationTest
import com.magrathea.ppsimple.infra.adapters.outbound.persistence.respoitories.WalletJpaRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertInstanceOf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.jdbc.Sql
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WalletPersistenceAdapterIntegrationTest @Autowired constructor(
    private val walletJpaRepository: WalletJpaRepository
) : BaseIntegrationTest() {

    private val walletPersistence: WalletPersistence = WalletPersistenceAdapter(
        walletJpaRepository = walletJpaRepository
    )

    @Test
    fun `should save a new wallet`() {
        val newWallet = Wallet(
            id = null,
            externalId = UUID.fromString("d15fd044-fbbd-4fb4-b085-e7245cdac7c1"),
            ownerName = "Test owner",
            document = Document.create("000.000.000-00"),
            balance = BigDecimal(10000),
            email = "test@mail.com",
            password = "12345678"
        )

        val resultWallet = walletPersistence.save(
            wallet = newWallet
        )

        assertNotNull(resultWallet)
        assertInstanceOf<Wallet>(resultWallet)
        assertNotNull(resultWallet.id)
        assertEquals(actual = resultWallet.externalId, expected = newWallet.externalId)
        assertEquals(actual = resultWallet.ownerName, expected = newWallet.ownerName)
        assertEquals(actual = resultWallet.document.toString(), expected = newWallet.document.toString())
        assertEquals(actual = resultWallet.balance, expected = newWallet.balance)
        assertEquals(actual = resultWallet.email, expected = newWallet.email)
        assertEquals(actual = resultWallet.password, expected = newWallet.password)
    }

    @Test
    @Sql(scripts = ["/sql/setup_wallet.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should find wallet by externalId when wallet exists in database`() {
        val externalId = UUID.fromString("d15fd044-fbbd-4fb4-b085-e7245cdac7c1")
        val resultWallet = walletPersistence.findBy(externalId)

        assertNotNull(resultWallet)
        assertInstanceOf<Wallet>(resultWallet)
    }


    @Test
    fun `should not find wallet by externalId when wallet doesn't exists in database`() {
        val externalId = UUID.fromString("d15fd044-fbbd-4fb4-b085-e7245cdac7c1")
        val resultWallet = walletPersistence.findBy(externalId)

        assertNull(resultWallet)
    }

    @Test
    @Sql(scripts = ["/sql/setup_wallet.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should update wallet balance`() {
        val externalID = UUID.fromString("d15fd044-fbbd-4fb4-b085-e7245cdac7c1")

        val oldWallet = walletJpaRepository.findByExternalId(externalId = externalID.toString())
        val newBalance = oldWallet!!.balance.plus(BigDecimal("100"))

        walletPersistence.updateBalance(
            externalId = externalID,
            newBalance = newBalance
        )

        val newWallet = walletJpaRepository.findByExternalId(externalId = externalID.toString())

        assertTrue { oldWallet.balance.compareTo(BigDecimal("1000")) == 0 }
        assertTrue { newWallet!!.balance.compareTo(BigDecimal("1100")) == 0 }
        assertTrue { newWallet!!.balance > oldWallet.balance }
    }
}