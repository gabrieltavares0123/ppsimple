package com.magrathea.ppsimple.application.exceptions

class PayerEligibilityDomainException(
    message: String,
    reason: String,
) : DomainException(message = message, details = mapOf("reason" to reason))
