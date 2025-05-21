package com.magrathea.ppsimple.domain

import com.magrathea.ppsimple.application.exceptions.IllegalArgumentDomainException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class DocumentTest {

    @Test
    fun `should create a CPF instance with success`() {
        assertDoesNotThrow {
            Document.create("000.000.000-00")
        }
    }

    @Test
    fun `should create a CNPJ instance with success`() {
        assertDoesNotThrow {
            Document.create("00.000.000/0000-00")
        }
    }

    @Test
    fun `should throw IllegalArgumentDomainException when document is empty`() {
        val exception = assertThrows<IllegalArgumentDomainException> {
            Document.create("")
        }

        assertInstanceOf<IllegalArgumentDomainException>(exception)
        assertEquals(actual = exception.message, expected = "Invalid document.")
        with(exception.details) {
            assertEquals(actual = this["field"], expected = "document")
            assertEquals(actual = this["invalid_value"], expected = "empty")
            assertEquals(actual = this["expected_format"], expected = "Should not be empty.")
        }
    }

    @Test
    fun `should throw IllegalArgumentDomainException when document length is different from CPF length`() {
        val exception = assertThrows<IllegalArgumentDomainException> {
            Document.create("000.000.000-0")
        }

        assertInstanceOf<IllegalArgumentDomainException>(exception)
        assertEquals(actual = exception.message, expected = "Invalid document.")
        with(exception.details) {
            assertEquals(actual = this["field"], expected = "document")
            assertEquals(actual = this["invalid_value"], expected = "000.000.000-0")
            assertEquals(actual = this["expected_format"], expected = "Field should have length of 11 digits for CPF or 14 digits for CNPJ.")
        }
    }

    @Test
    fun `should throw IllegalArgumentDomainException when document length is different from CNPJ length`() {
        val exception = assertThrows<IllegalArgumentDomainException> {
            Document.create("00.000.000/0000-0")
        }

        assertInstanceOf<IllegalArgumentDomainException>(exception)
        assertEquals(actual = exception.message, expected = "Invalid document.")
        with(exception.details) {
            assertEquals(actual = this["field"], expected = "document")
            assertEquals(actual = this["invalid_value"], expected = "00.000.000/0000-0")
            assertEquals(actual = this["expected_format"], expected = "Field should have length of 11 digits for CPF or 14 digits for CNPJ.")
        }
    }

    @Test
    fun `should return the expected CFP formatted`() {
        assertDoesNotThrow {
            val cpf = Document.create("000.000.000-00")

            assertEquals(actual = cpf.formatted(), expected = "000.000.000-00")
        }
    }

    @Test
    fun `should return the expected CFP unformatted`() {
        assertDoesNotThrow {
            val cpf = Document.create("000.000.000-00")

            assertEquals(actual = cpf.unformatted(), expected = "00000000000")
        }
    }

    @Test
    fun `should return the expected CNPJ formatted`() {
        assertDoesNotThrow {
            val cnpj = Document.create("00.000.000/0000-00")

            assertEquals(actual = cnpj.formatted(), expected = "00.000.000/0000-00")
        }
    }

    @Test
    fun `should return the expected CNPJ unformatted`() {
        assertDoesNotThrow {
            val cnpj = Document.create("00.000.000/0000-00")

            assertEquals(actual = cnpj.unformatted(), expected = "00000000000000")
        }
    }

    @Test
    fun `should return formatted CPF from to string method`() {
        assertDoesNotThrow {
            val cpf = Document.create("000.000.000-00")

            assertEquals(actual = cpf.toString(), expected = "000.000.000-00")
        }
    }

    @Test
    fun `should return formatted CNPJ from to string method`() {
        assertDoesNotThrow {
            val cnpj = Document.create("00.000.000/0000-00")

            assertEquals(actual = cnpj.toString(), expected = "00.000.000/0000-00")
        }
    }

}