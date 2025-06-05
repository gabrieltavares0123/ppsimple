package com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients.dtos

data class AuthorizationDto(
    val status: String,
    val data: Data,
) {
    data class Data(val authorization: Boolean)
}
