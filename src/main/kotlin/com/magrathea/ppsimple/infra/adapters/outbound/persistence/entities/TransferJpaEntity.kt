package com.magrathea.ppsimple.infra.adapters.outbound.persistence.entities

import com.magrathea.ppsimple.domain.TransferType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity(name = "Transfer")
@Table(name = "transfer")
data class TransferJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int?,
    val externalId: String,
    val payerExternalId: String,
    val payeeExternalId: String,
    @Column(name = "transfer_value")
    val value: String,
    @Column(name = "transfer_type")
    @Enumerated(EnumType.STRING)
    val type: TransferType,
    @CreatedDate
    val createdAt: LocalDateTime,
) {
    override fun toString(): String =
        "id=$id, externalId=$externalId, payerExternalId=$payerExternalId, payeeExternalId=$payeeExternalId, " +
            "value=$value, type=$type, createdAt=$createdAt"
}
