package com.magrathea.ppsimple.domain

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Notification(
    val id: Int?,
    val externalId: UUID?,
    val payerExternalId: UUID,
    val payeeExternalId: UUID,
    val value: BigDecimal,
    val type: TransferType,
    val createdAt: LocalDateTime,
) {
    override fun toString(): String =
        "id=$id, externalId=$externalId, payerExternalId=$payerExternalId, payeeExternalId=$payeeExternalId, " +
            "value=$value, type=$type, createdAt=$createdAt"
}
