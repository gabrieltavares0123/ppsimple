package com.magrathea.ppsimple.infra.adapters.inbound.rest

import com.magrathea.ppsimple.application.ports.inbound.CreateWalletUseCase
import com.magrathea.ppsimple.application.services.CreateWalletService
import com.magrathea.ppsimple.domain.Document
import com.magrathea.ppsimple.infra.adapters.inbound.rest.data.requests.CreateWalletRequest
import com.magrathea.ppsimple.infra.adapters.inbound.rest.data.responses.CreateWalletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class WalletRestAdapter(
    private val createWallet: CreateWalletService,
) {

    @PostMapping(
        "/wallet",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createWallet(
        @RequestBody createWalletRequest: CreateWalletRequest
    ): ResponseEntity<CreateWalletResponse> {

        val externalId = createWallet.execute(createWalletRequest.toCreateWalletUseCaseInput())

        return ResponseEntity.status(HttpStatus.CREATED).body(
            CreateWalletResponse(
                externalId = externalId
            )
        )
    }

    private fun CreateWalletRequest.toCreateWalletUseCaseInput() = CreateWalletUseCase.Input(
        id = null,
        externalId = null,
        ownerName = this.ownerName,
        document = Document.create(this.document),
        balance = this.balance,
        email = this.email,
        password = this.password
    )

}