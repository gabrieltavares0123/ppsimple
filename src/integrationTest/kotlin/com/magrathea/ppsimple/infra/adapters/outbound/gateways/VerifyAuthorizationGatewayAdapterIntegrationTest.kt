package com.magrathea.ppsimple.infra.adapters.outbound.gateways

import com.github.tomakehurst.wiremock.client.WireMock.forbidden
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.magrathea.ppsimple.application.ports.outbound.VerifyAuthorizationGateway
import com.magrathea.ppsimple.infra.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import kotlin.test.assertNotNull

@SpringBootTest
@EnableWireMock(ConfigureWireMock(port = 60085))
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@RunWith(SpringRunner::class)
class VerifyAuthorizationGatewayAdapterIntegrationTest @Autowired constructor(
    private val authorizationGateway: VerifyAuthorizationGateway
) : BaseIntegrationTest() {

    @Test
    fun `should respond with authorization true`() {
        stubFor(
            get(urlEqualTo("/authorize"))
                .willReturn(
                    okJson("{\"status\":\"success\",\"data\":{\"authorization\":true }}")
                )
        )

        val result = authorizationGateway.isAuthorized()

        assertNotNull(result)
        assertTrue(result)
    }

    @Test
    fun `should respond with authorization false`() {
        stubFor(
            get(urlEqualTo("/authorize"))
                .willReturn(
                    forbidden()
                        .withBody("{\"status\":\"fail\",\"data\":{\"authorization\":false }}")
                )
        )

        val result = authorizationGateway.isAuthorized()

        assertNotNull(result)
        assertFalse(result)
    }

}