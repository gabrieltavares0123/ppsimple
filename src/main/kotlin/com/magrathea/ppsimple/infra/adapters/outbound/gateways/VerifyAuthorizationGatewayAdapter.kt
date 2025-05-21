package com.magrathea.ppsimple.infra.adapters.outbound.gateways

import com.magrathea.ppsimple.application.ports.outbound.VerifyAuthorizationGateway
import com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients.AuthorizationClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class VerifyAuthorizationGatewayAdapter(
    private val authorizationClient: AuthorizationClient
) : VerifyAuthorizationGateway {

    private val logger = LoggerFactory.getLogger(VerifyAuthorizationGatewayAdapter::class.java)

    override fun isAuthorized(): Boolean {

        logger.info("Start request transfer authorization.")

        val result = authorizationClient.authorize()

        logger.info("Finish request to transfer authorization with body ${result.body}.")

        val isAuthorized = result.body?.data?.authorization ?: false

        return isAuthorized
    }

}