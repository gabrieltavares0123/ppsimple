package com.magrathea.ppsimple.infra.adapters.outbound.gateways

import com.magrathea.ppsimple.application.ports.outbound.VerifyAuthorizationGateway
import com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients.AuthorizationClient
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class VerifyAuthorizationGatewayAdapter(
    private val authorizationClient: AuthorizationClient
) : VerifyAuthorizationGateway {

    private val logger = LoggerFactory.getLogger(VerifyAuthorizationGatewayAdapter::class.java)

    override fun isAuthorized(): Boolean {

        return try {
            logger.info("Start request transfer authorization.")
            val result = authorizationClient.authorize()
            logger.info("Finish request to verify transfer authorization with success code ${result.statusCode} and body ${result.body}.")
            result.body!!.data.authorization
        } catch (fe: FeignException) {
            logger.info("Failed request to send notification with error code ${fe.status()} and body ${fe.responseBody()}.")
            false
        }

    }

}