package com.magrathea.ppsimple.application.ports.outbound

interface TransactionPersistence {
    fun <T> open(block: () -> T): T?
}