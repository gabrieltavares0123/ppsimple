package com.magrathea.ppsimple.application.exceptions

class InsufficientBalanceDomainException(
    message: String,
) : DomainException(
        message = message,
        details = mapOf("reason" to "Payer don't have enough balance in the wallet for this transaction."),
    )
