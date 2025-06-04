package com.magrathea.ppsimple.infra.adapters.inbound.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.forbidden
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.magrathea.ppsimple.infra.BaseIntegrationTest
import com.magrathea.ppsimple.infra.adapters.inbound.rest.data.requests.DoTransferRequest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@EnableWireMock(ConfigureWireMock(port = 60085))
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TransferRestAdapterIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) : BaseIntegrationTest() {

    @BeforeEach
    fun setup() {
        stubFor(
            get(urlEqualTo("/authorize"))
                .willReturn(
                    okJson("{\"status\":\"success\",\"data\":{\"authorization\":true }}")
                )
        ).priority = 2

        stubFor(
            WireMock.post(urlEqualTo("/notify"))
        )
    }

    @Test
    @Sql(scripts = ["/sql/setup_wallet.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should respond with 201 CREATED with an external id when executing a new transfer and it is authorized`() {
        val transfer = DoTransferRequest(
            value = BigDecimal("100"),
            payer = UUID.fromString("d15fd044-fbbd-4fb4-b085-e7245cdac7c1"),
            payee = UUID.fromString("1cd70610-c298-4288-a3ca-a9f85babf3d7")
        )

        mockMvc.post("/api/transfer") {
            content = objectMapper.writeValueAsString(transfer)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isCreated
            jsonPath("externalId", `is`(notNullValue()))
        }
    }

    @Test
    @Sql(scripts = ["/sql/setup_wallet.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should respond with 403 FORBIDEN when executing a new transfer and it is not authorized `() {
        stubFor(
            get(urlEqualTo("/authorize"))
                .willReturn(
                    forbidden()
                        .withBody("{\"status\":\"fail\",\"data\":{\"authorization\":false }}")
                )
        ).priority = 1

        val transfer = DoTransferRequest(
            value = BigDecimal("100"),
            payer = UUID.fromString("d15fd044-fbbd-4fb4-b085-e7245cdac7c1"),
            payee = UUID.fromString("1cd70610-c298-4288-a3ca-a9f85babf3d7")
        )

        mockMvc.post("/api/transfer") {
            content = objectMapper.writeValueAsString(transfer)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isForbidden
            jsonPath("message", `is`("This transfer is unauthorized."))
            jsonPath("details.reason", `is`("This transaction is not authorized."))
        }
    }

    @Test
    @Sql(scripts = ["/sql/setup_wallet_with_missing_payer.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should respond with 404 NOT_FOUND when executing a new transfer and payer doen't exists`() {
        val payerExternalId = UUID.fromString("d15fd044-fbbd-4fb4-b085-e7245cdac7c1")
        val transfer = DoTransferRequest(
            value = BigDecimal("100"),
            payer = payerExternalId,
            payee = UUID.fromString("1cd70610-c298-4288-a3ca-a9f85babf3d7")
        )

        mockMvc.post("/api/transfer") {
            content = objectMapper.writeValueAsString(transfer)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isNotFound
            jsonPath("message", `is`("Payer not found."))
            jsonPath("details.reason", `is`("Payer with id $payerExternalId doesn't exists."))
        }
    }

    @Test
    @Sql(scripts = ["/sql/setup_wallet_with_missing_payee.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should respond with 404 NOT_FOUND when executing a new transafer and payee doen't exists`() {
        val payeeExternalId = UUID.fromString("1cd70610-c298-4288-a3ca-a9f85babf3d7")
        val transfer = DoTransferRequest(
            value = BigDecimal("100"),
            payer = UUID.fromString("d15fd044-fbbd-4fb4-b085-e7245cdac7c1"),
            payee = payeeExternalId
        )

        mockMvc.post("/api/transfer") {
            content = objectMapper.writeValueAsString(transfer)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isNotFound
            jsonPath("message", `is`("Payee not found."))
            jsonPath("details.reason", `is`("Payee with id $payeeExternalId doesn't exists."))
        }
    }

    @Test
    @Sql(scripts = ["/sql/setup_wallet_with_legal_payer.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should respond with http code FORBIDDEN when executing a transfer and payer is legal`() {
        val transfer = DoTransferRequest(
            value = BigDecimal("100"),
            payer = UUID.fromString("1cd70610-c298-4288-a3ca-a9f85babf3d7"),
            payee = UUID.fromString("d15fd044-fbbd-4fb4-b085-e7245cdac7c1")
        )

        mockMvc.post("/api/transfer") {
            content = objectMapper.writeValueAsString(transfer)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isForbidden
            jsonPath("message", `is`("Invalid payer."))
            jsonPath("details.reason", `is`("Payer should not be legal for this kind of transaction."))
        }
    }

    @Test
    @Sql(
        scripts = ["/sql/setup_wallet_with_payer_insufficient_balance.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should respond with http code FORBIDDEN when payer don't have enough balance for the transfer`() {
        val transfer = DoTransferRequest(
            value = BigDecimal("100"),
            payer = UUID.fromString("d15fd044-fbbd-4fb4-b085-e7245cdac7c1"),
            payee = UUID.fromString("1cd70610-c298-4288-a3ca-a9f85babf3d7")
        )

        mockMvc.post("/api/transfer") {
            content = objectMapper.writeValueAsString(transfer)
            contentType = MediaType.APPLICATION_JSON
        }.andDo {
            print()
        }.andExpect {
            status().isForbidden
            jsonPath("message", `is`("Insufficient balance."))
            jsonPath("details.reason", `is`("Payer don't have enough balance in the wallet for this transaction."))
        }
    }

}