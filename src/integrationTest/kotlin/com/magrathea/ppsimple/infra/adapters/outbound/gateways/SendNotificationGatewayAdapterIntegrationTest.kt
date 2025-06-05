package com.magrathea.ppsimple.infra.adapters.outbound.gateways

import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.magrathea.ppsimple.application.ports.outbound.SendNotificationGateway
import com.magrathea.ppsimple.domain.Notification
import com.magrathea.ppsimple.domain.TransferType
import com.magrathea.ppsimple.infra.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertNotNull

@SpringBootTest
@RunWith(SpringRunner::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EnableWireMock(ConfigureWireMock(port = 60085))
class SendNotificationGatewayAdapterIntegrationTest
    @Autowired
    constructor(
        private val sendNotificationGateway: SendNotificationGateway,
    ) : BaseIntegrationTest() {
        @Test
        fun `should respond with notify true`() {
            stubFor(
                post(urlEqualTo("/notify")),
            )

            val notification =
                Notification(
                    id = 1,
                    externalId = UUID.fromString("ac4ac6a9-9465-4397-a694-5e1556573da2"),
                    payerExternalId = UUID.fromString("e8a37903-5b5b-47ec-89a9-5301ac73df30"),
                    payeeExternalId = UUID.fromString("72cdf8fb-ce36-479d-8590-16585bc2dd79"),
                    value = BigDecimal("100"),
                    type = TransferType.NATURAL_TO_NATURAL,
                    createdAt = LocalDateTime.of(2025, 5, 30, 12, 46),
                )

            val result = sendNotificationGateway.send(notification)

            assertNotNull(result)
            assertTrue(result)
        }

        @Test
        fun `should respond with notify false`() {
            stubFor(
                post(urlEqualTo("/notify")),
            )

            val notification =
                Notification(
                    id = 1,
                    externalId = UUID.fromString("ac4ac6a9-9465-4397-a694-5e1556573da2"),
                    payerExternalId = UUID.fromString("e8a37903-5b5b-47ec-89a9-5301ac73df30"),
                    payeeExternalId = UUID.fromString("72cdf8fb-ce36-479d-8590-16585bc2dd79"),
                    value = BigDecimal("100"),
                    type = TransferType.NATURAL_TO_NATURAL,
                    createdAt = LocalDateTime.of(2025, 5, 30, 12, 46),
                )

            val result = sendNotificationGateway.send(notification)

            assertNotNull(result)
            assertTrue(result)
        }
    }
