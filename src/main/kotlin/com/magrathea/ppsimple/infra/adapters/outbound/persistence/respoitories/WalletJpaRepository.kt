package com.magrathea.ppsimple.infra.adapters.outbound.persistence.respoitories

import com.magrathea.ppsimple.infra.adapters.outbound.persistence.entities.WalletJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface WalletJpaRepository : JpaRepository<WalletJpaEntity, Int> {
    fun findByExternalId(externalId: String): WalletJpaEntity?

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Wallet u SET u.balance = :newBalance WHERE u.externalId = :externalId")
    fun updateBalance(
        externalId: String,
        newBalance: String,
    )
}
