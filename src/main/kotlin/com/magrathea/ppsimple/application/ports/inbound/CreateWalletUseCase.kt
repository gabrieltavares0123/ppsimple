package com.magrathea.ppsimple.application.ports.inbound

import com.magrathea.ppsimple.domain.Document
import java.math.BigDecimal
import java.util.UUID

interface CreateWalletUseCase {
    fun execute(input: Input): UUID

    data class Input(
        val externalId: UUID?,
        val ownerName: String,
        val document: Document,
        val balance: BigDecimal,
        val email: String,
        val password: String,
    )
}
