package com.magrathea.ppsimple.application.exceptions

class UnauthorizedTransferDomainException(
    message: String,
) : DomainException(message = message, details = mapOf("reason" to "This transaction is not authorized."))
