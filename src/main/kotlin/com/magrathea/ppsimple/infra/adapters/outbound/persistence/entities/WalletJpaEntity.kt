package com.magrathea.ppsimple.infra.adapters.outbound.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

@Entity(name = "Wallet")
@Table(name = "wallet")
data class WalletJpaEntity(

    @Id
    @SequenceGenerator(
        name = PRIMARY_KEY_GENERATOR_NAME,
        sequenceName = DATABASE_SEQUENCE_NAME,
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = PRIMARY_KEY_GENERATOR_NAME)
    val id: Int?,

    @Column(unique = true)
    val externalId: String,

    @Column(unique = true)
    val document: String,

    val balance: String,
    val ownerName: String,

    @Column(unique = true)
    val email: String,

    val password: String
) {

    private companion object {
        const val PRIMARY_KEY_GENERATOR_NAME = "wallet_id_sequence"
        const val DATABASE_SEQUENCE_NAME = "wallet_id_sequence"
    }

    override fun toString(): String {
        return "id=$id, email=$email"
    }
}