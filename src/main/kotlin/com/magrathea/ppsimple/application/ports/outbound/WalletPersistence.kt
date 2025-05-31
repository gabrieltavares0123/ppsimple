package com.magrathea.ppsimple.application.ports.outbound

import com.magrathea.ppsimple.domain.Wallet
import java.math.BigDecimal
import java.util.UUID

interface WalletPersistence {

    fun findBy(externalId: UUID): Wallet?

    fun save(wallet: Wallet): Wallet

    fun updateBalance(externalId: UUID, newBalance: BigDecimal)

}