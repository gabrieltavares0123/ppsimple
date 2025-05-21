package com.magrathea.ppsimple.domain

import com.magrathea.ppsimple.application.exceptions.IllegalArgumentDomainException
import javax.swing.text.MaskFormatter

class Document(
    private val rawValue: String,
    val type: DocumentType
) {

    companion object {

        const val CPF_LENGTH = 11
        const val CNPJ_LENGTH = 14

        fun create(value: String): Document {
            val unformattedValue = value.replace(Regex("[^0-9]"), "")

            if (unformattedValue.isEmpty()) throw IllegalArgumentDomainException(
                message = "Invalid document.",
                field = "document",
                invalidValue = "empty",
                expectedFormat = "Should not be empty."
            )

            val type = when (unformattedValue.length) {
                CPF_LENGTH -> DocumentType.CPF
                CNPJ_LENGTH -> DocumentType.CNPJ
                else -> throw IllegalArgumentDomainException(
                    message = "Invalid document.",
                    field = "document",
                    invalidValue = value,
                    expectedFormat = "Field should have length of 11 digits for CPF or 14 digits for CNPJ."
                )
            }

            return Document(unformattedValue, type)
        }
    }

    fun formatted(): String {
        val mask = when (type) {
            DocumentType.CPF -> "###.###.###-##"
            DocumentType.CNPJ -> "##.###.###/####-##"
        }
        return MaskFormatter(mask).apply {
            valueContainsLiteralCharacters = false
        }.valueToString(rawValue)
    }

    fun unformatted() = rawValue

    fun type(): DocumentType = type

    override fun toString(): String = formatted()
}