package com.magrathea.ppsimple.application.ports.outbound

interface VerifyAuthorizationGateway {

    fun isAuthorized(): Boolean

}