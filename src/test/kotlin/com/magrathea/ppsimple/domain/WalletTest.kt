package com.magrathea.ppsimple.domain

import com.magrathea.ppsimple.application.exceptions.IllegalArgumentDomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals

class WalletTest {

    @Test
    fun `should create a Wallet instance with success`() {
        assertDoesNotThrow {
            Wallet(
                id = null,
                externalId = UUID.fromString("10a6f25c-bafd-4c78-b39e-e43dfa9ae658"),
                ownerName = "Gabriel Jorge",
                document = Document.create("000.000.000-00"),
                balance = BigDecimal(1000.00),
                email = "gabriel.jorge@mail.com",
                password = "12345678"
            )
        }
    }

    @Test
    fun `should throw IllegalArgumentDomainException when ownerName is empty`() {
        val exception = assertThrows<IllegalArgumentDomainException> {
            Wallet(
                id = null,
                externalId = UUID.fromString("10a6f25c-bafd-4c78-b39e-e43dfa9ae658"),
                ownerName = "",
                document = Document.create("000.000.000-00"),
                balance = BigDecimal(1000.00),
                email = "gabriel.jorge@mail.com",
                password = "12345678"
            )
        }

        assertInstanceOf<IllegalArgumentDomainException>(exception)
        assertEquals(actual = exception.message, expected = "Invalid owner name.")
        with(exception) {
            assertEquals(actual = this.details["field"], expected = "ownerName")
            assertEquals(actual = this.details["invalid_value"], expected = "empty")
            assertEquals(actual = this.details["expected_format"], expected = "Field should not be empty.")
        }
    }

    @Test
    fun `should throw IllegalArgumentDomainException when ownerName is less than 3 digits length`() {
        val exception = assertThrows<IllegalArgumentDomainException> {
            Wallet(
                id = null,
                externalId = UUID.fromString("10a6f25c-bafd-4c78-b39e-e43dfa9ae658"),
                ownerName = "Ga",
                document = Document.create("000.000.000-00"),
                balance = BigDecimal(1000.00),
                email = "gabriel.jorge@mail.com",
                password = "12345678"
            )
        }

        assertInstanceOf<IllegalArgumentDomainException>(exception)
        assertEquals(actual = exception.message, expected = "Invalid owner name.")
        with(exception) {
            assertEquals(actual = this.details["field"], expected = "ownerName")
            assertEquals(actual = this.details["invalid_value"], expected = "Ga")
            assertEquals(
                actual = this.details["expected_format"],
                expected = "Field should be at last 3 digits length."
            )
        }
    }

    @Test
    fun `should throw IllegalArgumentDomainException when balance is negative`() {
        val exception = assertThrows<IllegalArgumentDomainException> {
            Wallet(
                id = null,
                externalId = UUID.fromString("10a6f25c-bafd-4c78-b39e-e43dfa9ae658"),
                ownerName = "Gabriel Jorge",
                document = Document.create("000.000.000-00"),
                balance = BigDecimal(-1.00),
                email = "gabriel.jorge@mail.com",
                password = "12345678"
            )
        }

        assertInstanceOf<IllegalArgumentDomainException>(exception)
        assertEquals(actual = exception.message, expected = "Invalid balance.")
        with(exception) {
            assertEquals(actual = this.details["field"], expected = "balance")
            assertEquals(actual = this.details["invalid_value"], expected = BigDecimal(-1.00))
            assertEquals(actual = this.details["expected_format"], expected = "Field should not be negative.")
        }
    }

    @Test
    fun `should throw IllegalArgumentDomainException when email is empty`() {
        val exception = assertThrows<IllegalArgumentDomainException> {
            Wallet(
                id = null,
                externalId = UUID.fromString("10a6f25c-bafd-4c78-b39e-e43dfa9ae658"),
                ownerName = "Gabriel Jorge",
                document = Document.create("000.000.000-00"),
                balance = BigDecimal(1.00),
                email = "",
                password = "12345678"
            )
        }

        assertInstanceOf<IllegalArgumentDomainException>(exception)
        assertEquals(actual = exception.message, expected = "Invalid e-mail.")
        with(exception) {
            assertEquals(actual = this.details["field"], expected = "email")
            assertEquals(actual = this.details["invalid_value"], expected = "empty")
            assertEquals(actual = this.details["expected_format"], expected = "Field should not be empty.")
        }
    }

    @Test
    fun `should throw IllegalArgumentDomainException when email is invalid`() {
        val exception = assertThrows<IllegalArgumentDomainException> {
            Wallet(
                id = null,
                externalId = UUID.fromString("10a6f25c-bafd-4c78-b39e-e43dfa9ae658"),
                ownerName = "Gabriel Jorge",
                document = Document.create("000.000.000-00"),
                balance = BigDecimal(1.00),
                email = "gabriel.mail.com",
                password = "12345678"
            )
        }

        assertInstanceOf<IllegalArgumentDomainException>(exception)
        assertEquals(actual = exception.message, expected = "Invalid e-mail.")
        with(exception) {
            assertEquals(actual = this.details["field"], expected = "email")
            assertEquals(actual = this.details["invalid_value"], expected = "gabriel.mail.com")
            assertEquals(
                actual = this.details["expected_format"],
                expected = "Field should be a valid e-mail ex: user@mail.com"
            )
        }
    }

    @Test
    fun `should throw IllegalArgumentDomainException when password is empty`() {
        val exception = assertThrows<IllegalArgumentDomainException> {
            Wallet(
                id = null,
                externalId = UUID.fromString("10a6f25c-bafd-4c78-b39e-e43dfa9ae658"),
                ownerName = "Gabriel Jorge",
                document = Document.create("000.000.000-00"),
                balance = BigDecimal(1.00),
                email = "gabriel@mail.com",
                password = ""
            )
        }

        assertInstanceOf<IllegalArgumentDomainException>(exception)
        assertEquals(actual = exception.message, expected = "Invalid password.")
        with(exception) {
            assertEquals(actual = this.details["field"], expected = "password")
            assertEquals(actual = this.details["invalid_value"], expected = "empty")
            assertEquals(actual = this.details["expected_format"], expected = "Field should not be empty.")
        }
    }

    @Test
    fun `should throw IllegalArgumentDomainException when password is length is less than 8`() {
        val exception = assertThrows<IllegalArgumentDomainException> {
            Wallet(
                id = null,
                externalId = UUID.fromString("10a6f25c-bafd-4c78-b39e-e43dfa9ae658"),
                ownerName = "Gabriel Jorge",
                document = Document.create("000.000.000-00"),
                balance = BigDecimal(1.00),
                email = "gabriel@mail.com",
                password = "1234"
            )
        }

        assertInstanceOf<IllegalArgumentDomainException>(exception)
        assertEquals(actual = exception.message, expected = "Invalid password.")
        with(exception) {
            assertEquals(actual = this.details["field"], expected = "password")
            assertEquals(actual = this.details["invalid_value"], expected = "1234")
            assertEquals(
                actual = this.details["expected_format"],
                expected = "Field should be at last 8 digits length."
            )
        }
    }
}