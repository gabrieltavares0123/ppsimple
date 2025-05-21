package com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients

import com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients.dtos.Data
import com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients.dtos.TransferAuthorizationDto
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class AuthorizationClientFallBack : AuthorizationClient {

    private val logger = LoggerFactory.getLogger(AuthorizationClientFallBack::class.java)

    override fun authorize(): ResponseEntity<TransferAuthorizationDto> {

        logger.info("Failing triggered from TransferAuthorizationClientFallBack.")

        return ResponseEntity.ok(
            TransferAuthorizationDto(
                status = "fail",
                data = Data(
                    authorization = false
                )
            )
        )
    }

}