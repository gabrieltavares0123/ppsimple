package com.magrathea.ppsimple.application.exceptions

open class DomainException(
    override val message: String,
    open val details: Map<String, Any> = emptyMap()
) : RuntimeException(message)