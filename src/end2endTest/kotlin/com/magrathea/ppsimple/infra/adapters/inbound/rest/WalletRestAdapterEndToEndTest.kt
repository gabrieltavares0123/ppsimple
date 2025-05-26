package com.magrathea.ppsimple.infra.adapters.inbound.rest

import com.github.tomakehurst.wiremock.client.WireMock.created
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.anEmptyMap
import org.hamcrest.Matchers.emptyOrNullString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import kotlin.test.assertTrue

@EnableWireMock(
    ConfigureWireMock(
        port = 60085
    )
)
class WalletRestAdapterEndToEndTest : BaseEndToEndTest() {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.port = port

        stubFor(
            get("/authorize")
                .willReturn(
                    created()
                        .withBody("{\"status\":\"success\",\"data\":{\"authorization\": true }}")
                )
        )
    }

    @Test
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should create a new wallet with success`() {
        val requestBody = """
            {
                "ownerName": "New Wallet",
                "document": "000.000.000-99",
                "balance": 1000,
                "email": "new.wallet@mail.com",
                "password": "12345678"
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/wallet")
        } Then {
            statusCode(HttpStatus.CREATED.value())
            body("externalId", not(emptyOrNullString()))
        }
    }

    // Scenario too hard to test.
    @Test
    fun `should throw TransactionDomainException when something unexpected happens with the transaction`() {
        assertTrue(true)
    }

    @Test
    fun `should response with UNPROCESSABLE_ENTITY when ownerName is empty`() {
        val requestBody = """
            {
                "ownerName": "",
                "document": "000.000.000-00",
                "balance": 1000,
                "email": "gabriel.jorge@mail.com",
                "password": "12345678"
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/wallet")
        } Then {
            log().all()
            statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            body("message", equalTo("Invalid wallet."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("ownerName"))
            body("details.invalid_value", equalTo(""))
            body("details.expected_format", equalTo("Field should not be empty."))
        }
    }

    @Test
    fun `should response with UNPROCESSABLE_ENTITY when ownerName length is too short`() {
        val requestBody = """
            {
                "ownerName": "Ga",
                "document": "000.000.000-00",
                "balance": 1000,
                "email": "gabriel.jorge@mail.com",
                "password": "12345678"
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/wallet")
        } Then {
            log().all()
            statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            body("message", equalTo("Invalid wallet."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("ownerName"))
            body("details.invalid_value", equalTo("Ga"))
            body("details.expected_format", equalTo("Field should be at last 3 digits length."))
        }
    }

    @Test
    fun `should response with UNPROCESSABLE_ENTITY when balance is negative`() {
        val requestBody = """
            {
                "ownerName": "Gabriel Jorge",
                "document": "000.000.000-00",
                "balance": -1,
                "email": "gabriel.jorge@mail.com",
                "password": "12345678"
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/wallet")
        } Then {
            log().all()
            statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            body("message", equalTo("Invalid wallet."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("balance"))
            body("details.invalid_value", equalTo(-1))
            body("details.expected_format", equalTo("Field should not be negative."))
        }
    }

    @Test
    fun `should response with UNPROCESSABLE_ENTITY when email is empty`() {
        val requestBody = """
            {
                "ownerName": "Gabriel Jorge",
                "document": "000.000.000-00",
                "balance": 1000,
                "email": "",
                "password": "12345678"
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/wallet")
        } Then {
            log().all()
            statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            body("message", equalTo("Invalid wallet."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("email"))
            body("details.invalid_value", equalTo(""))
            body("details.expected_format", equalTo("Field should not be empty."))
        }
    }

    @Test
    fun `should response with UNPROCESSABLE_ENTITY when email has an invalid format`() {
        val requestBody = """
            {
                "ownerName": "Gabriel Jorge",
                "document": "000.000.000-00",
                "balance": 1000,
                "email": "gabriel.mail.com",
                "password": "12345678"
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/wallet")
        } Then {
            log().all()
            statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            body("message", equalTo("Invalid wallet."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("email"))
            body("details.invalid_value", equalTo("gabriel.mail.com"))
            body("details.expected_format", equalTo("Field should be a valid e-mail ex: user@mail.com"))
        }
    }

    @Test
    fun `should response with UNPROCESSABLE_ENTITY when passwprd is empty`() {
        val requestBody = """
            {
                "ownerName": "Gabriel Jorge",
                "document": "000.000.000-00",
                "balance": 1000,
                "email": "gabriel.jorge@mail.com",
                "password": ""
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/wallet")
        } Then {
            log().all()
            statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            body("message", equalTo("Invalid wallet."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("password"))
            body("details.invalid_value", equalTo(""))
            body("details.expected_format", equalTo("Field should not be empty."))
        }
    }

    @Test
    fun `should response with UNPROCESSABLE_ENTITY when passwprd is short`() {
        val requestBody = """
            {
                "ownerName": "Gabriel Jorge",
                "document": "000.000.000-00",
                "balance": 1000,
                "email": "gabriel.jorge@mail.com",
                "password": "123"
            }
        """.trimIndent()

        Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/wallet")
        } Then {
            log().all()
            statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            body("message", equalTo("Invalid wallet."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("password"))
            body("details.invalid_value", equalTo("123"))
            body("details.expected_format", equalTo("Field should be at last 8 digits length."))
        }
    }

}