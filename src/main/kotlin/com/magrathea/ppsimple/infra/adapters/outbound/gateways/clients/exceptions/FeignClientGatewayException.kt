package com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients.exceptions

import feign.Response

class FeignClientGatewayException(
    val response: Response,
    val methodKey: String
) : RuntimeException("Feign client failed with method=$methodKey and status=${response.status()} and body=${response.body()}.")