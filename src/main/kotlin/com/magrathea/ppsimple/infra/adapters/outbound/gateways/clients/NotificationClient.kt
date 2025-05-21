package com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody

@FeignClient(
    name = "send-transfer-notification",
    url = "\${gateway.send-transfer-notification-gateway-url}",
    fallback = NotificationClientFallBack::class
)
interface NotificationClient {

    @PostMapping("/notify")
    @ResponseBody
    fun notify(): ResponseEntity<Unit>

}