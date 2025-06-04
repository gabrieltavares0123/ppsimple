package com.magrathea.ppsimple.infra.adapters.inbound.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.magrathea.ppsimple.infra.BaseIntegrationTest
import com.magrathea.ppsimple.infra.adapters.inbound.rest.data.requests.CreateWalletRequest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
class WalletRestAdapterIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) : BaseIntegrationTest() {

    @Test
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should respond with 201 CREATED when creating a new wallet`() {
        val wallet = CreateWalletRequest(
            ownerName = "Owner name",
            document = "000.000.000-00",
            balance = BigDecimal("1000"),
            email = "owner@mail.com",
            password = "12345678"
        )

        mockMvc.post("/api/wallet") {
            content = objectMapper.writeValueAsString(wallet)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isCreated
            jsonPath("externalId", `is`(notNullValue()))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet with empty owner name`() {
        val wallet = CreateWalletRequest(
            ownerName = "",
            document = "000.000.000-00",
            balance = BigDecimal("1000"),
            email = "owner@mail.com",
            password = "12345678"
        )

        mockMvc.post("/api/wallet") {
            content = objectMapper.writeValueAsString(wallet)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isUnprocessableEntity
            jsonPath("status", `is`("UNPROCESSABLE_ENTITY"))
            jsonPath("message", `is`("Invalid owner name."))
            jsonPath("details.field", `is`("ownerName"))
            jsonPath("details.invalid_value", `is`("empty"))
            jsonPath("details.expected_format", `is`("Field should not be empty."))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet with ownerName is too short`() {
        val wallet = CreateWalletRequest(
            ownerName = "aa",
            document = "000.000.000-00",
            balance = BigDecimal("1000"),
            email = "owner@mail.com",
            password = "12345678"
        )

        mockMvc.post("/api/wallet") {
            content = objectMapper.writeValueAsString(wallet)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isUnprocessableEntity
            jsonPath("status", `is`("UNPROCESSABLE_ENTITY"))
            jsonPath("message", `is`("Invalid owner name."))
            jsonPath("details.field", `is`("ownerName"))
            jsonPath("details.invalid_value", `is`("aa"))
            jsonPath("details.expected_format", `is`("Field should be at last 3 digits length."))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet with empty document`() {
        val wallet = CreateWalletRequest(
            ownerName = "Owner name",
            document = "",
            balance = BigDecimal("1000"),
            email = "owner@mail.com",
            password = "12345678"
        )

        mockMvc.post("/api/wallet") {
            content = objectMapper.writeValueAsString(wallet)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isUnprocessableEntity
            jsonPath("status", `is`("UNPROCESSABLE_ENTITY"))
            jsonPath("message", `is`("Invalid document."))
            jsonPath("details.field", `is`("document"))
            jsonPath("details.invalid_value", `is`("empty"))
            jsonPath("details.expected_format", `is`("Should not be empty."))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet and document has invalid digits length`() {
        val wallet = CreateWalletRequest(
            ownerName = "Owner name",
            document = "000.000.000-",
            balance = BigDecimal("1000"),
            email = "owner@mail.com",
            password = "12345678"
        )

        mockMvc.post("/api/wallet") {
            content = objectMapper.writeValueAsString(wallet)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isUnprocessableEntity
            jsonPath("status", `is`("UNPROCESSABLE_ENTITY"))
            jsonPath("message", `is`("Invalid document."))
            jsonPath("details.field", `is`("document"))
            jsonPath("details.invalid_value", `is`("000.000.000-"))
            jsonPath(
                "details.expected_format",
                `is`("Field should have length of 11 digits for CPF or 14 digits for CNPJ.")
            )
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet and balance is negative`() {
        val wallet = CreateWalletRequest(
            ownerName = "Owner name",
            document = "000.000.000-00",
            balance = BigDecimal("-1"),
            email = "owner@mail.com",
            password = "12345678"
        )

        mockMvc.post("/api/wallet") {
            content = objectMapper.writeValueAsString(wallet)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isUnprocessableEntity
            jsonPath("status", `is`("UNPROCESSABLE_ENTITY"))
            jsonPath("message", `is`("Invalid balance."))
            jsonPath("details.field", `is`("balance"))
            jsonPath("details.invalid_value", `is`(-1))
            jsonPath("details.expected_format", `is`("Field should not be negative."))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet and email is empty`() {
        val wallet = CreateWalletRequest(
            ownerName = "Owner name",
            document = "000.000.000-00",
            balance = BigDecimal("1"),
            email = "",
            password = "12345678"
        )

        mockMvc.post("/api/wallet") {
            content = objectMapper.writeValueAsString(wallet)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isUnprocessableEntity
            jsonPath("status", `is`("UNPROCESSABLE_ENTITY"))
            jsonPath("message", `is`("Invalid e-mail."))
            jsonPath("details.field", `is`("email"))
            jsonPath("details.invalid_value", `is`("empty"))
            jsonPath("details.expected_format", `is`("Field should not be empty."))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet and email has an invalid format`() {
        val wallet = CreateWalletRequest(
            ownerName = "Owner name",
            document = "000.000.000-00",
            balance = BigDecimal("1000"),
            email = "gabriel @mail.com",
            password = "12345678"
        )

        mockMvc.post("/api/wallet") {
            content = objectMapper.writeValueAsString(wallet)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isUnprocessableEntity
            jsonPath("status", `is`("UNPROCESSABLE_ENTITY"))
            jsonPath("message", `is`("Invalid e-mail."))
            jsonPath("details.field", `is`("email"))
            jsonPath("details.invalid_value", `is`("gabriel @mail.com"))
            jsonPath("details.expected_format", `is`("Field should be a valid e-mail ex: user@mail.com"))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet and password is empty`() {
        val wallet = CreateWalletRequest(
            ownerName = "Owner name",
            document = "000.000.000-00",
            balance = BigDecimal("1000"),
            email = "gabriel.jorge@mail.com",
            password = ""
        )

        mockMvc.post("/api/wallet") {
            content = objectMapper.writeValueAsString(wallet)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isUnprocessableEntity
            jsonPath("status", `is`("UNPROCESSABLE_ENTITY"))
            jsonPath("message", `is`("Invalid password."))
            jsonPath("details.field", `is`("password"))
            jsonPath("details.invalid_value", `is`("empty"))
            jsonPath("details.expected_format", `is`("Field should not be empty."))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet and password password is short`() {
        val wallet = CreateWalletRequest(
            ownerName = "Owner name",
            document = "000.000.000-00",
            balance = BigDecimal("1000"),
            email = "gabriel.jorge@mail.com",
            password = "123"
        )

        mockMvc.post("/api/wallet") {
            content = objectMapper.writeValueAsString(wallet)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isUnprocessableEntity
            jsonPath("status", `is`("UNPROCESSABLE_ENTITY"))
            jsonPath("message", `is`("Invalid password."))
            jsonPath("details.field", `is`("password"))
            jsonPath("details.invalid_value", `is`("123"))
            jsonPath("details.expected_format", `is`("Field should be at last 8 digits length."))
        }
    }

}