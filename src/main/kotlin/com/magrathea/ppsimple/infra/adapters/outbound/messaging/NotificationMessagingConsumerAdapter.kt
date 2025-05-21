package com.magrathea.ppsimple.infra.adapters.outbound.messaging

import com.magrathea.ppsimple.application.ports.outbound.NotificationMessagingConsumer
import com.magrathea.ppsimple.domain.Notification
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class NotificationMessagingConsumerAdapter : NotificationMessagingConsumer {

    private val logger = LoggerFactory.getLogger(NotificationMessagingConsumerAdapter::class.java)

    @KafkaListener(topics = ["transaction-notification"], groupId = "ppsimple")
    override fun consume(notification: Notification) {
        logger.info("A notificação foi recebida: $notification")
    }

}