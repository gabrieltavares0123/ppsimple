package com.magrathea.ppsimple.infra.adapters.inbound.rest.data.requests

import java.math.BigDecimal

data class CreateWalletRequest(
    val ownerName: String,
    val document: String,
    val balance: BigDecimal,
    val email: String,
    val password: String
)