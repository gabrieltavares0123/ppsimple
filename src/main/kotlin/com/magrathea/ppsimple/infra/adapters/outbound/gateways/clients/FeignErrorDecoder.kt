package com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients

import com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients.exceptions.FeignClientGatewayException
import feign.Response
import feign.codec.ErrorDecoder
import org.slf4j.LoggerFactory

class FeignErrorDecoder : ErrorDecoder {

    private val logger = LoggerFactory.getLogger(FeignErrorDecoder::class.java)

    override fun decode(methodKey: String, response: Response): Exception? {
        logger.info("Feign call failed with method: $methodKey and responseStatus: ${response.status()} and body: ${response.body()}.")

        return FeignClientGatewayException(
            response = response,
            methodKey = methodKey
        )
    }

}