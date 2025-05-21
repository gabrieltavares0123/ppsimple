package com.magrathea.ppsimple.application.exceptions

class TransactionDomainException(
    message: String
) : DomainException(
    message = message,
    details = mapOf("reason" to "Something went wrong with this transaction.")
)