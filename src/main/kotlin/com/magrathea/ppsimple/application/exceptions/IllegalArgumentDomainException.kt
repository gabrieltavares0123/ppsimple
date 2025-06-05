package com.magrathea.ppsimple.application.exceptions

class IllegalArgumentDomainException(
    message: String,
    field: String,
    invalidValue: Any,
    expectedFormat: String,
) : DomainException(
        message = message,
        details = mapOf("field" to field, "invalid_value" to invalidValue, "expected_format" to expectedFormat),
    )
