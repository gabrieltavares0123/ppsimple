package com.magrathea.ppsimple.infra.adapters.outbound.messaging

import com.magrathea.ppsimple.application.ports.outbound.NotificationMessagingConsumer
import com.magrathea.ppsimple.domain.Notification
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.concurrent.CountDownLatch

@Component
class NotificationMessagingConsumerAdapter : NotificationMessagingConsumer {

    private var latch = CountDownLatch(1)
    private var lastPayload = ""

    private val logger = LoggerFactory.getLogger(NotificationMessagingConsumerAdapter::class.java)

    @KafkaListener(topics = ["transaction-notification"], groupId = "ppsimple")
    override fun consume(notification: Notification) {
        logger.info("A notificação foi recebida: $notification")
        lastPayload = notification.toString()
        latch.countDown()
    }

    override fun getLatch(): CountDownLatch = latch

    override fun resetLatch() {
        CountDownLatch(1)
    }

    override fun getLastPayload(): String = lastPayload

}