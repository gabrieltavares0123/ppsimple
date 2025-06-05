package com.magrathea.ppsimple.application.exceptions

import java.util.UUID

class PayeeNotFoundDomainException(
    message: String,
    payeeExternalId: UUID,
) : DomainException(message = message, details = mapOf("reason" to "Payee with id $payeeExternalId doesn't exists."))
