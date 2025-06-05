package com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients

import com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients.dtos.AuthorizationDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class AuthorizationClientFallBack : AuthorizationClient {
    private val logger = LoggerFactory.getLogger(AuthorizationClientFallBack::class.java)

    override fun authorize(): ResponseEntity<AuthorizationDto> {
        logger.info("Triggering AuthorizationClientFallBack for authorization request.")

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            AuthorizationDto(
                status = HttpStatus.FORBIDDEN.name,
                data = AuthorizationDto.Data(authorization = false),
            ),
        )
    }
}
