package com.magrathea.ppsimple.infra.adapters.outbound.persistence

import com.magrathea.ppsimple.application.ports.outbound.WalletPersistence
import com.magrathea.ppsimple.domain.Document
import com.magrathea.ppsimple.domain.Wallet
import com.magrathea.ppsimple.infra.adapters.outbound.persistence.entities.WalletJpaEntity
import com.magrathea.ppsimple.infra.adapters.outbound.persistence.respoitories.WalletJpaRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
class WalletPersistenceAdapter(
    private val walletJpaRepository: WalletJpaRepository,
) : WalletPersistence {

    override fun findBy(externalId: UUID): Wallet? =
        walletJpaRepository.findByExternalId(externalId.toString())?.toWallet()

    override fun save(wallet: Wallet): Wallet {
        val userCredentialEntity = wallet.toWalletJpaEntity()

        val createdWallet = walletJpaRepository.save(userCredentialEntity)

        return createdWallet.toWallet()
    }

    @Transactional
    override fun updateBalance(externalId: UUID, newBalance: BigDecimal) {
        walletJpaRepository.updateBalance(
            externalId = externalId.toString(),
            newBalance = newBalance.toPlainString()
        )
    }

    private fun WalletJpaEntity.toWallet() = Wallet(
        id = this.id,
        externalId = UUID.fromString(this.externalId),
        ownerName = this.ownerName,
        document = Document.create(this.document),
        balance = this.balance,
        email = this.email,
        password = this.password
    )

    private fun Wallet.toWalletJpaEntity() = WalletJpaEntity(
        id = this.id,
        externalId = this.externalId.toString(),
        ownerName = this.ownerName,
        document = this.document.unformatted(),
        balance = this.balance,
        email = this.email,
        password = this.password
    )
}