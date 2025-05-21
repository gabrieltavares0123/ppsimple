package com.magrathea.ppsimple.infra.adapters.outbound.gateways

import com.magrathea.ppsimple.application.ports.outbound.SendNotificationGateway
import com.magrathea.ppsimple.domain.Notification
import com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients.NotificationClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SendNotificationGatewayAdapter(
    private val notificationClient: NotificationClient
) : SendNotificationGateway {

    private val logger = LoggerFactory.getLogger(SendNotificationGatewayAdapter::class.java)

    override fun send(notification: Notification): Boolean {

        logger.info("Start request transfer authorization.")

        val result = notificationClient.notify()

        return if (result.statusCode.isError) {
            logger.info("Finish request to transfer authorization with success body ${result.body}.")
            false
        } else {
            logger.info("Finish request to transfer authorization with error body ${result.statusCode}.")
            true
        }

    }

}