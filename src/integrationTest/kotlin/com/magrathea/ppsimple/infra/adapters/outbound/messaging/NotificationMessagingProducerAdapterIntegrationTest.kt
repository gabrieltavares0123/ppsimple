package com.magrathea.ppsimple.infra.adapters.outbound.messaging

import com.magrathea.ppsimple.application.ports.outbound.NotificationMessagingConsumer
import com.magrathea.ppsimple.domain.Notification
import com.magrathea.ppsimple.domain.TransferType
import com.magrathea.ppsimple.infra.BaseIntegrationTest
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.test.Test

@SpringBootTest
@RunWith(SpringRunner::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NotificationMessagingProducerAdapterIntegrationTest
    @Autowired
    constructor(
        private val kafkaTemplate: KafkaTemplate<String, Notification>,
        private val consumer: NotificationMessagingConsumer,
    ) : BaseIntegrationTest() {
        private val producer = NotificationMessagingProducerAdapter(kafkaTemplate = kafkaTemplate)

        @Before
        fun setup() {
            consumer.resetLatch()
        }

        @Test
        fun `should produce and receive a notification`() {
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

            producer.produce(notification)

            val consumed = consumer.getLatch().await(10, TimeUnit.SECONDS)
            assertTrue(consumed)
            assertThat(consumer.getLastPayload(), containsString(notification.toString()))
        }
    }
