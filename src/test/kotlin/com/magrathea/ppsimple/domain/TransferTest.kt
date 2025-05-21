package com.magrathea.ppsimple.domain

import com.magrathea.ppsimple.application.exceptions.IllegalArgumentDomainException
import com.magrathea.ppsimple.application.exceptions.PayerEligibilityDomainException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

class TransferTest {

    @Test
    fun `should create a transfer instance with success`() {
        assertDoesNotThrow {
            Transfer(
                id = null,
                externalId = UUID.fromString("f612d1d9-7a4b-4466-b60d-d18078ed9dea"),
                payerExternalId = UUID.fromString("9596f15e-acd3-42ef-800d-c92cdbcb48a9"),
                payeeExternalId = UUID.fromString("20dd8bd5-f413-44e5-95e9-745131fefbc5"),
                value = BigDecimal(100.00),
                type = TransferType.NATURAL_TO_LEGAL,
                createdAt = LocalDateTime.of(2025, 5, 20, 15, 7)
            )
        }
    }

    @Test
    fun `should throw IllegalArgumentDomainException when transfer value is below the minimum`() {
        val exception = assertThrows<IllegalArgumentDomainException>() {
            Transfer(
                id = null,
                externalId = UUID.fromString("f612d1d9-7a4b-4466-b60d-d18078ed9dea"),
                payerExternalId = UUID.fromString("9596f15e-acd3-42ef-800d-c92cdbcb48a9"),
                payeeExternalId = UUID.fromString("20dd8bd5-f413-44e5-95e9-745131fefbc5"),
                value = BigDecimal(0.00),
                type = TransferType.NATURAL_TO_LEGAL,
                createdAt = LocalDateTime.of(2025, 5, 20, 15, 7)
            )
        }

        assertInstanceOf<IllegalArgumentDomainException>(exception)
        with(exception) {
            assertEquals(actual = this.message, expected = "Invalid transfer value.")
            assertEquals(actual = this.details["field"], expected = "value")
            assertEquals(actual = this.details["invalid_value"], expected = BigDecimal(0))
            assertEquals(actual = this.details["expected_format"], expected = "At least 0.01")
        }
    }

    @Test
    fun `should throw PayerEligibilityDomainException when payer and payee are the same`() {
        val exception = assertThrows<PayerEligibilityDomainException>() {
            Transfer(
                id = null,
                externalId = UUID.fromString("f612d1d9-7a4b-4466-b60d-d18078ed9dea"),
                payerExternalId = UUID.fromString("9596f15e-acd3-42ef-800d-c92cdbcb48a9"),
                payeeExternalId = UUID.fromString("9596f15e-acd3-42ef-800d-c92cdbcb48a9"),
                value = BigDecimal(10.00),
                type = TransferType.NATURAL_TO_LEGAL,
                createdAt = LocalDateTime.of(2025, 5, 20, 15, 7)
            )
        }

        assertInstanceOf<PayerEligibilityDomainException>(exception)
        with(exception) {
            assertEquals(actual = this.message, expected = "Invalid payer.")
            assertEquals(actual = this.details["reason"], expected = "Transfers to same person are no permitted.")
        }
    }

    @Test
    fun `should return the expected string from to string method`() {
        assertDoesNotThrow {
            val transfer = Transfer(
                id = null,
                externalId = UUID.fromString("f612d1d9-7a4b-4466-b60d-d18078ed9dea"),
                payerExternalId = UUID.fromString("9596f15e-acd3-42ef-800d-c92cdbcb48a9"),
                payeeExternalId = UUID.fromString("20dd8bd5-f413-44e5-95e9-745131fefbc5"),
                value = BigDecimal(100.00),
                type = TransferType.NATURAL_TO_LEGAL,
                createdAt = LocalDateTime.of(2025, 5, 20, 15, 7)
            )

            assertEquals(
                actual = transfer.toString(),
                expected = "[externalId=f612d1d9-7a4b-4466-b60d-d18078ed9dea, payerExternalId=9596f15e-acd3-42ef-800d-c92cdbcb48a9, payeeExternalId=20dd8bd5-f413-44e5-95e9-745131fefbc5, value=100, type=NATURAL_TO_LEGAL, createdAt=2025-05-20T15:07]"
            )
        }
    }

}