package com.magrathea.ppsimple.infra.adapters.inbound.rest.data.requests

import java.math.BigDecimal
import java.util.UUID

data class DoTransferRequest(
    val value: BigDecimal,
    val payer: UUID,
    val payee: UUID,
)
