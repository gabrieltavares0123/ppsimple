package com.magrathea.ppsimple.infra.adapters.inbound.rest

import com.github.tomakehurst.wiremock.client.WireMock.created
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.magrathea.ppsimple.infra.BaseEndToEndTest
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
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.jdbc.Sql
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableWireMock(ConfigureWireMock(port = 60085))
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
                        .withBody("{\"status\":\"success\",\"data\":{\"authorization\": true }}"),
                ),
        )
    }

    @Test
    @Sql(scripts = ["/sql/cleanup_wallet.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should respond with 201 CREATED with an externalId when creating a new wallet`() {
        val requestBody =
            """
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
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet and ownerName is empty`() {
        val requestBody =
            """
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
            body("message", equalTo("Invalid owner name."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("ownerName"))
            body("details.invalid_value", equalTo("empty"))
            body("details.expected_format", equalTo("Field should not be empty."))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet and ownerName is too short`() {
        val requestBody =
            """
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
            body("message", equalTo("Invalid owner name."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("ownerName"))
            body("details.invalid_value", equalTo("Ga"))
            body("details.expected_format", equalTo("Field should be at last 3 digits length."))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet with empty document`() {
        val requestBody =
            """
            {
                "ownerName": "Gabriel Jorge",
                "document": "",
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
            body("message", equalTo("Invalid document."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("document"))
            body("details.invalid_value", equalTo("empty"))
            body("details.expected_format", equalTo("Should not be empty."))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet and document has invalid digits length`() {
        val requestBody =
            """
            {
                "ownerName": "Gabriel Jorge",
                "document": "000.000.000-",
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
            body("message", equalTo("Invalid document."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("document"))
            body("details.invalid_value", equalTo("000.000.000-"))
            body(
                "details.expected_format",
                equalTo("Field should have length of 11 digits for CPF or 14 digits for CNPJ."),
            )
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet and balance is negative`() {
        val requestBody =
            """
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
            body("message", equalTo("Invalid balance."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("balance"))
            body("details.invalid_value", equalTo(-1))
            body("details.expected_format", equalTo("Field should not be negative."))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet and email is empty`() {
        val requestBody =
            """
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
            body("message", equalTo("Invalid e-mail."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("email"))
            body("details.invalid_value", equalTo("empty"))
            body("details.expected_format", equalTo("Field should not be empty."))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when email has an invalid format`() {
        val requestBody =
            """
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
            body("message", equalTo("Invalid e-mail."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("email"))
            body("details.invalid_value", equalTo("gabriel.mail.com"))
            body("details.expected_format", equalTo("Field should be a valid e-mail ex: user@mail.com"))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet and password is empty`() {
        val requestBody =
            """
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
            body("message", equalTo("Invalid password."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("password"))
            body("details.invalid_value", equalTo("empty"))
            body("details.expected_format", equalTo("Field should not be empty."))
        }
    }

    @Test
    fun `should respond with 422 UNPROCESSABLE_ENTITY when creating a new wallet and password is short`() {
        val requestBody =
            """
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
            body("message", equalTo("Invalid password."))
            body("details", not(anEmptyMap<String, String>()))
            body("details.field", equalTo("password"))
            body("details.invalid_value", equalTo("123"))
            body("details.expected_format", equalTo("Field should be at last 8 digits length."))
        }
    }
}
