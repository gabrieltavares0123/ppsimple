package com.magrathea.ppsimple.infra.adapters.inbound.rest

import com.magrathea.ppsimple.application.ports.inbound.DoTransferUseCase
import com.magrathea.ppsimple.application.services.DoTransferService
import com.magrathea.ppsimple.infra.adapters.inbound.rest.data.requests.DoTransferRequest
import com.magrathea.ppsimple.infra.adapters.inbound.rest.data.responses.TransferResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class TransferRestAdapter(
    private val doTransferService: DoTransferService
) {
    @PostMapping(
        "/transfer",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun doTransfer(
        @RequestBody doTransferRequest: DoTransferRequest
    ): ResponseEntity<TransferResponse> {

        val result = doTransferService.execute(doTransferRequest.toDoTransferUseCaseInput())
        val response = TransferResponse(externalId = result)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    private fun DoTransferRequest.toDoTransferUseCaseInput() = DoTransferUseCase.Input(
        payer = this.payer,
        payee = this.payee,
        value = this.value
    )

}