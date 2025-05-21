package com.magrathea.ppsimple.application.ports.inbound

import java.math.BigDecimal
import java.util.UUID

interface DoTransferUseCase {

    fun execute(input: Input): UUID

    data class Input(
        val payer: UUID,
        val payee: UUID,
        val value: BigDecimal
    )

}