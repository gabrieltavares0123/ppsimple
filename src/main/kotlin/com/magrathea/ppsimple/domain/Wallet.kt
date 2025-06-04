package com.magrathea.ppsimple.domain

import com.magrathea.ppsimple.application.exceptions.IllegalArgumentDomainException
import java.math.BigDecimal
import java.util.UUID

data class Wallet(
    val id: Int?,
    val externalId: UUID,
    val ownerName: String,
    val document: Document,
    val balance: BigDecimal,
    val email: String,
    val password: String
) {

    init {
        if (ownerName.isEmpty()) throw IllegalArgumentDomainException(
            message = "Invalid owner name.",
            field = "ownerName",
            invalidValue = "empty",
            expectedFormat = "Field should not be empty."
        )

        if (ownerName.length < OWNER_NAME_MINIMUM_LENGTH) throw IllegalArgumentDomainException(
            message = "Invalid owner name.",
            field = "ownerName",
            invalidValue = ownerName,
            expectedFormat = "Field should be at last $OWNER_NAME_MINIMUM_LENGTH digits length."
        )

        if (balance.compareTo(BigDecimal("0")) < 0) throw IllegalArgumentDomainException(
            message = "Invalid balance.",
            field = "balance",
            invalidValue = balance,
            expectedFormat = "Field should not be negative."
        )

        if (email.isBlank()) throw IllegalArgumentDomainException(
            message = "Invalid e-mail.",
            field = "email",
            invalidValue = "empty",
            expectedFormat = "Field should not be empty."
        )

        val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        if (emailRegex.matches(email).not()) throw IllegalArgumentDomainException(
            message = "Invalid e-mail.",
            field = "email",
            invalidValue = email,
            expectedFormat = "Field should be a valid e-mail ex: user@mail.com"
        )

        if (password.isEmpty()) throw IllegalArgumentDomainException(
            message = "Invalid password.",
            field = "password",
            invalidValue = "empty",
            expectedFormat = "Field should not be empty."
        )

        if (password.length < PASSWORD_MINIMUM_LENGTH) throw IllegalArgumentDomainException(
            message = "Invalid password.",
            field = "password",
            invalidValue = password,
            expectedFormat = "Field should be at last $PASSWORD_MINIMUM_LENGTH digits length."
        )
    }

    private companion object {
        const val OWNER_NAME_MINIMUM_LENGTH = 3
        const val PASSWORD_MINIMUM_LENGTH = 8
    }

}
