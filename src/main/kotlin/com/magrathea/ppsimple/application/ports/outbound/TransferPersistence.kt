package com.magrathea.ppsimple.application.ports.outbound

import com.magrathea.ppsimple.domain.Transfer

interface TransferPersistence {
    fun save(transfer: Transfer): Transfer
}