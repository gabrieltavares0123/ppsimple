package com.magrathea.ppsimple.infra.adapters.inbound.rest

import com.magrathea.ppsimple.application.exceptions.DomainException
import com.magrathea.ppsimple.application.exceptions.IllegalArgumentDomainException
import com.magrathea.ppsimple.application.exceptions.InsufficientBalanceDomainException
import com.magrathea.ppsimple.application.exceptions.PayeeNotFoundDomainException
import com.magrathea.ppsimple.application.exceptions.PayerEligibilityDomainException
import com.magrathea.ppsimple.application.exceptions.PayerNotFoundDomainException
import com.magrathea.ppsimple.application.exceptions.TransactionDomainException
import com.magrathea.ppsimple.application.exceptions.UnauthorizedTransferDomainException
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@Hidden
@RestControllerAdvice
class ExceptionHandlerRestAdapter {

    @ExceptionHandler(DomainException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleDomainException(de: DomainException): ResponseEntity<RestError> {
        val restError = RestError(
            status = HttpStatus.INTERNAL_SERVER_ERROR.name,
            message = de.message ?: "Failed.",
            details = de.details
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restError)
    }

    @ExceptionHandler(IllegalArgumentDomainException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleIllegalArgumentDomainException(de: IllegalArgumentDomainException): ResponseEntity<RestError> {
        val restError = RestError(
            status = HttpStatus.UNPROCESSABLE_ENTITY.name,
            message = de.message ?: "Failed.",
            details = de.details
        )

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(restError)
    }

    @ExceptionHandler(InsufficientBalanceDomainException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleInsufficientBalanceDomainException(de: InsufficientBalanceDomainException): ResponseEntity<RestError> {
        val restError = RestError(
            status = HttpStatus.FORBIDDEN.name,
            message = de.message ?: "Failed.",
            details = de.details
        )

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(restError)
    }

    @ExceptionHandler(PayerEligibilityDomainException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handlePayerEligibilityDomainException(de: PayerEligibilityDomainException): ResponseEntity<RestError> {
        val restError = RestError(
            status = HttpStatus.FORBIDDEN.name,
            message = de.message ?: "Failed.",
            details = de.details
        )

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(restError)
    }

    @ExceptionHandler(PayerNotFoundDomainException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handlePayerNotFoundDomainException(de: PayerNotFoundDomainException): ResponseEntity<RestError> {
        val restError = RestError(
            status = HttpStatus.NOT_FOUND.name,
            message = de.message ?: "Failed.",
            details = de.details
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(restError)
    }

    @ExceptionHandler(PayeeNotFoundDomainException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handlePayeeNotFoundDomainException(de: PayeeNotFoundDomainException): ResponseEntity<RestError> {
        val restError = RestError(
            status = HttpStatus.NOT_FOUND.name,
            message = de.message ?: "Failed.",
            details = de.details
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(restError)
    }

    @ExceptionHandler(TransactionDomainException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleTransactionDomainException(de: TransactionDomainException): ResponseEntity<RestError> {
        val restError = RestError(
            status = HttpStatus.INTERNAL_SERVER_ERROR.name,
            message = de.message ?: "Failed.",
            details = de.details
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restError)
    }

    @ExceptionHandler(UnauthorizedTransferDomainException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleUnauthorizedTransferDomainException(de: UnauthorizedTransferDomainException): ResponseEntity<RestError> {
        val restError = RestError(
            status = HttpStatus.FORBIDDEN.name,
            message = de.message ?: "Failed.",
            details = de.details
        )

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(restError)
    }

    data class RestError(
        val status: String,
        val message: String,
        val details: Map<String, Any> = emptyMap()
    )

}