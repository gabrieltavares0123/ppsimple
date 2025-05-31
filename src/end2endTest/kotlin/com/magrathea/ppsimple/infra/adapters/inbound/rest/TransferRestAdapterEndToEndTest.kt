package com.magrathea.ppsimple.infra.adapters.inbound.rest

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.forbidden
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.magrathea.ppsimple.infra.BaseEndToEndTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.emptyOrNullString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.jdbc.Sql
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableWireMock(ConfigureWireMock(port = 60085))
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferRestAdapterEndToEndTest : BaseEndToEndTest() {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.port = port

        stubFor(
            get(urlEqualTo("/authorize"))
                .willReturn(
                    okJson("{\"status\":\"success\",\"data\":{\"authorization\":true }}")
                )
        ).priority = 2

        stubFor(
            post(urlEqualTo("/notify"))
        )
    }

    @Test
    @Sql(scripts = ["/sql/setup_wallet.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should respond with 201 CREATED with an external id when executing a new transfer and it is authorized`() {
        val requestBody = """
            {
                "payer": "d15fd044-fbbd-4fb4-b085-e7245cdac7c1",
                "payee": "1cd70610-c298-4288-a3ca-a9f85babf3d7",
                "value": 100
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/transfer")
        } Then {
            log().all()
            statusCode(HttpStatus.CREATED.value())
            body("externalId", not(emptyOrNullString()))
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

        val requestBody = """
            {
                "payer": "d15fd044-fbbd-4fb4-b085-e7245cdac7c1",
                "payee": "1cd70610-c298-4288-a3ca-a9f85babf3d7",
                "value": 100
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/transfer")
        } Then {
            log().all()
            statusCode(HttpStatus.FORBIDDEN.value())
            body("message", equalTo("This transfer is unauthorized."))
            body("details.reason", equalTo("This transaction is not authorized."))
        }
    }

    @Test
    @Sql(scripts = ["/sql/setup_wallet_with_missing_payer.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should respond with 404 NOT_FOUND when executing a new transfer and payer doen't exists`() {
        val payerExternalId = UUID.fromString("d15fd044-fbbd-4fb4-b085-e7245cdac7c1")
        val requestBody = """
            {
                "payer": "$payerExternalId",
                "payee": "1cd70610-c298-4288-a3ca-a9f85babf3d7",
                "value": 100
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/transfer")
        } Then {
            log().all()
            statusCode(HttpStatus.NOT_FOUND.value())
            body("message", equalTo("Payer not found."))
            body("details.reason", equalTo("Payer with id $payerExternalId doesn't exists."))
        }
    }

    @Test
    @Sql(scripts = ["/sql/setup_wallet_with_missing_payee.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should respond with 404 NOT_FOUND when executing a new transafer and payee doen't exists`() {
        val payeeExternalId = UUID.fromString("1cd70610-c298-4288-a3ca-a9f85babf3d7")
        val requestBody = """
            {
                "payer": "d15fd044-fbbd-4fb4-b085-e7245cdac7c1",
                "payee": "$payeeExternalId",
                "value": 100
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/transfer")
        } Then {
            log().all()
            statusCode(HttpStatus.NOT_FOUND.value())
            body("message", equalTo("Payee not found."))
            body("details.reason", equalTo("Payee with id $payeeExternalId doesn't exists."))
        }
    }

    @Test
    @Sql(scripts = ["/sql/setup_wallet_with_legal_payer.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should respond with http code FORBIDDEN when executing a transfer and payer is legal`() {
        val requestBody = """
            {
                "payer": "1cd70610-c298-4288-a3ca-a9f85babf3d7",
                "payee": "d15fd044-fbbd-4fb4-b085-e7245cdac7c1",
                "value": 100
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/transfer")
        } Then {
            log().all()
            statusCode(HttpStatus.FORBIDDEN.value())
            body("message", equalTo("Invalid payer."))
            body("details.reason", equalTo("Payer should not be legal for this kind of transaction."))
        }
    }

    @Test
    @Sql(
        scripts = ["/sql/setup_wallet_with_payer_insufficient_balance.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should respond with http code FORBIDDEN when payer don't have enough balance for the trasnfer`() {
        val requestBody = """
            {
                "payer": "d15fd044-fbbd-4fb4-b085-e7245cdac7c1",
                "payee": "1cd70610-c298-4288-a3ca-a9f85babf3d7",
                "value": 100
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/transfer")
        } Then {
            log().all()
            statusCode(HttpStatus.FORBIDDEN.value())
            body("message", equalTo("Insufficient balance."))
            body("details.reason", equalTo("Payer don't have enough balance in the wallet for this transaction."))
        }
    }
}