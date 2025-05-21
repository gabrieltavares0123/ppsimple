package com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class NotificationClientFallBack : NotificationClient {

    private val logger = LoggerFactory.getLogger(NotificationClientFallBack::class.java)

    override fun notify(): ResponseEntity<Unit> {
        logger.info("Failing triggered from SendTransferNotificationClientFallBack.")

        return ResponseEntity.internalServerError().build()
    }
}