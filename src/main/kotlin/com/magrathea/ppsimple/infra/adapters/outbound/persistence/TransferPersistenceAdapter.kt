package com.magrathea.ppsimple.infra.adapters.outbound.persistence

import com.magrathea.ppsimple.application.ports.outbound.TransferPersistence
import com.magrathea.ppsimple.domain.Transfer
import com.magrathea.ppsimple.infra.adapters.outbound.persistence.entities.TransferJpaEntity
import com.magrathea.ppsimple.infra.adapters.outbound.persistence.respoitories.TransferJpaRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
class TransferPersistenceAdapter(
    private val transferJpaRepository: TransferJpaRepository,
) : TransferPersistence {
    override fun save(transfer: Transfer): Transfer {
        val entity = transfer.toTransferJpaEntity()
        val created = transferJpaRepository.save(entity)

        return created.toTransfer()
    }

    private fun Transfer.toTransferJpaEntity() =
        TransferJpaEntity(
            id = this.id,
            externalId = this.externalId.toString(),
            payerExternalId = this.payerExternalId.toString(),
            payeeExternalId = this.payeeExternalId.toString(),
            value = this.value.toString(),
            type = this.type,
            createdAt = this.createdAt,
        )

    private fun TransferJpaEntity.toTransfer() =
        Transfer(
            id = this.id,
            externalId = UUID.fromString(this.externalId),
            payerExternalId = UUID.fromString(this.payerExternalId),
            payeeExternalId = UUID.fromString(this.payeeExternalId),
            value = BigDecimal(this.value),
            type = this.type,
            createdAt = this.createdAt,
        )
}
