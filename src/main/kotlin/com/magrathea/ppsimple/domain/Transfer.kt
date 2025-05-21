package com.magrathea.ppsimple.domain

import com.magrathea.ppsimple.application.exceptions.IllegalArgumentDomainException
import com.magrathea.ppsimple.application.exceptions.PayerEligibilityDomainException
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Transfer(
    val id: Int?,
    val externalId: UUID?,
    val payerExternalId: UUID,
    val payeeExternalId: UUID,
    val value: BigDecimal,
    val type: TransferType,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {

    init {
        if (value.compareTo(BigDecimal(MINIMUM_TRANSFER_VALUE)) < 0) throw IllegalArgumentDomainException(
            message = "Invalid transfer value.",
            field = "value",
            invalidValue = value,
            expectedFormat = "At least 0.01"
        )

        if (payerExternalId == payeeExternalId) throw PayerEligibilityDomainException(
            message = "Invalid payer.",
            reason = "Transfers to same person are no permitted."
        )
    }

    override fun toString(): String {
        return "[externalId=$externalId, payerExternalId=$payerExternalId, payeeExternalId=$payeeExternalId, " +
                "value=$value, type=${type.name}, createdAt=$createdAt]"
    }

    private companion object {
        const val MINIMUM_TRANSFER_VALUE = "0.01"
    }
}