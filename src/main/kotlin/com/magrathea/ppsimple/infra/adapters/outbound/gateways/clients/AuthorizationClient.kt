package com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients

import com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients.dtos.AuthorizationDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@FeignClient(
    name = "transfer-authorization-client",
    url = "\${gateway.transfer-authorization-gateway-url}",
    fallback = AuthorizationClientFallBack::class
)
interface AuthorizationClient {

    @GetMapping("/authorize")
    @ResponseBody
    fun authorize(): ResponseEntity<AuthorizationDto>

}