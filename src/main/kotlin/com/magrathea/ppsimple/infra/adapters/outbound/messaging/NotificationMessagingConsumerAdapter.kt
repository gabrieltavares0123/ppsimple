package com.magrathea.ppsimple.infra.adapters.outbound.messaging

import com.magrathea.ppsimple.application.ports.outbound.NotificationMessagingConsumer
import com.magrathea.ppsimple.application.ports.outbound.SendNotificationGateway
import com.magrathea.ppsimple.domain.Notification
import com.magrathea.ppsimple.infra.adapters.outbound.messaging.exceptions.SendNotificationFailedMessagingException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import java.util.concurrent.CountDownLatch

@Component
class NotificationMessagingConsumerAdapter
    @Autowired
    constructor(
        private val sendNotificationGateway: SendNotificationGateway,
        private val kafkaTemplate: KafkaTemplate<String, Notification>,
    ) : NotificationMessagingConsumer {
        private val logger = LoggerFactory.getLogger(NotificationMessagingConsumerAdapter::class.java)

        @Value("\${ppsimple.topic.notification}")
        private lateinit var dltNotificationTopic: String
        private var latch = CountDownLatch(1)
        private var lastPayload = ""

        @KafkaListener(topics = ["\${ppsimple.topic.notification}"], groupId = "\${spring.kafka.consumer.group-id}")
        @Retryable(
            value = [SendNotificationFailedMessagingException::class],
            maxAttempts = 2,
            backoff = Backoff(delay = 1000),
        )
        override fun consume(notification: Notification) {
            lastPayload = notification.toString()
            latch.countDown()

            logger.info("Tentando reenviar notificação do tópico=transaction-notification, mensagem=$notification")
            val result = sendNotificationGateway.send(notification)
            if (!result) throw SendNotificationFailedMessagingException()
        }

        @Recover
        private fun onSendNotificationFail(
            snfme: SendNotificationFailedMessagingException,
            notification: Notification,
        ) {
            logger.info("Retentativas excedidas. Enviando para DLT notificação=$notification")
            kafkaTemplate.send(dltNotificationTopic, notification)
        }

        override fun getLatch(): CountDownLatch = latch

        override fun resetLatch() {
            CountDownLatch(1)
        }

        override fun getLastPayload(): String = lastPayload
    }
