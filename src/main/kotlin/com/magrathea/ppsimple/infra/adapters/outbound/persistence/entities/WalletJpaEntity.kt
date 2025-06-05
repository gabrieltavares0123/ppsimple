package com.magrathea.ppsimple.infra.adapters.outbound.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity(name = "Wallet")
@Table(name = "wallet")
data class WalletJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int?,
    @Column(unique = true)
    val externalId: String,
    @Column(unique = true)
    val document: String,
    val balance: BigDecimal,
    val ownerName: String,
    @Column(unique = true)
    val email: String,
    val password: String,
) {
    override fun toString(): String = "id=$id, externalId=$externalId, document=$document, balance=$balance, email=$email"
}
