package com.magrathea.ppsimple.infra.adapters.outbound.messaging

import com.magrathea.ppsimple.application.ports.outbound.NotificationMessagingProducer
import com.magrathea.ppsimple.domain.Notification
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class NotificationMessagingProducerAdapter(
    private val kafkaTemplate: KafkaTemplate<String, Notification>,
) : NotificationMessagingProducer {
    private val logger = LoggerFactory.getLogger(NotificationMessagingProducerAdapter::class.java)

    override fun produce(notification: Notification) {
        kafkaTemplate.send("transaction-notification", notification)
        logger.info("A notificação foi enviada: $notification")
    }
}
