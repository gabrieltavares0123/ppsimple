package com.magrathea.ppsimple.application.exceptions

import java.util.UUID

class PayerNotFoundDomainException(
    message: String,
    payerExternalId: UUID
) : DomainException(
    message = message,
    details = mapOf("reason" to "Payer with id $payerExternalId doesn't exists.")
)