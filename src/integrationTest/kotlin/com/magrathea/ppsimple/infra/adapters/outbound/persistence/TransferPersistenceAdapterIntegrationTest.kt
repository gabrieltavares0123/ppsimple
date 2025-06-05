package com.magrathea.ppsimple.infra.adapters.outbound.persistence

import com.magrathea.ppsimple.application.ports.outbound.TransferPersistence
import com.magrathea.ppsimple.domain.Transfer
import com.magrathea.ppsimple.domain.TransferType
import com.magrathea.ppsimple.infra.BaseIntegrationTest
import com.magrathea.ppsimple.infra.adapters.outbound.persistence.respoitories.TransferJpaRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertInstanceOf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.DirtiesContext
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransferPersistenceAdapterIntegrationTest
    @Autowired
    constructor(
        private val transferJpaRepository: TransferJpaRepository,
    ) : BaseIntegrationTest() {
        private val transferPersistence: TransferPersistence =
            TransferPersistenceAdapter(transferJpaRepository = transferJpaRepository)

        @Test
        fun `should save a new transfer`() {
            val newTransfer =
                Transfer(
                    id = null,
                    externalId = UUID.fromString("a2c416aa-e545-4e3f-863f-94357fe10568"),
                    payerExternalId = UUID.fromString("0682f350-f549-45cc-bfdc-0e9b3235d8c1"),
                    payeeExternalId = UUID.fromString("c1eae3da-7f02-4ffe-a1c5-d13955887487"),
                    value = BigDecimal("100"),
                    type = TransferType.NATURAL_TO_NATURAL,
                    createdAt = LocalDateTime.of(2025, 5, 26, 16, 38),
                )

            val resultTransfer = transferPersistence.save(newTransfer)

            assertNotNull(resultTransfer)
            assertInstanceOf<Transfer>(resultTransfer)
            assertNotNull(resultTransfer.id)
            assertEquals(actual = resultTransfer.externalId, expected = newTransfer.externalId)
            assertEquals(actual = resultTransfer.payerExternalId, expected = newTransfer.payerExternalId)
            assertEquals(actual = resultTransfer.payeeExternalId, expected = newTransfer.payeeExternalId)
            assertEquals(actual = resultTransfer.value, expected = newTransfer.value)
            assertEquals(actual = resultTransfer.type, expected = newTransfer.type)
            assertEquals(actual = resultTransfer.createdAt, expected = newTransfer.createdAt)
        }
    }
