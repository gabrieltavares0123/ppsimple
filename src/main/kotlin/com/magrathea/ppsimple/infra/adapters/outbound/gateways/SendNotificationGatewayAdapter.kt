package com.magrathea.ppsimple.infra.adapters.outbound.gateways

import com.magrathea.ppsimple.application.ports.outbound.SendNotificationGateway
import com.magrathea.ppsimple.domain.Notification
import com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients.NotificationClient
import org.slf4j.LoggerFactory
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException
import org.springframework.stereotype.Component

@Component
class SendNotificationGatewayAdapter(
    private val notificationClient: NotificationClient,
) : SendNotificationGateway {
    private val logger = LoggerFactory.getLogger(SendNotificationGatewayAdapter::class.java)

    override fun send(notification: Notification): Boolean {
        logger.info("Start request send notification.")

        return try {
            val result = notificationClient.notify()
            logger.info(
                "Finish request to send notification with success code ${result.statusCode} and body ${result.body}.",
            )
            true
        } catch (fe: NoFallbackAvailableException) {
            logger.info("Failed request to send notification.")
            false
        }
    }
}
