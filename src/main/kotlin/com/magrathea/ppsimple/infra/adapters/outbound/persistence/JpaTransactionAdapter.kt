package com.magrathea.ppsimple.infra.adapters.outbound.persistence

import com.magrathea.ppsimple.application.ports.outbound.TransactionPersistence
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class JpaTransactionAdapter(
    private val transactionTemplate: TransactionTemplate
) : TransactionPersistence {

    private val logger = LoggerFactory.getLogger(JpaTransactionAdapter::class.java)


    override fun <T> open(block: () -> T): T? {
        return try {
            transactionTemplate.execute<T> {
                logger.info("Started database transaction.")
                block().also {
                    logger.info("Finished database transaction.")
                }
            }
        } catch (ex: Exception) {
            throw ex.also {
                logger.error("Failed executing database transaction with exception=${it.message}.")
            }
        }
    }
}