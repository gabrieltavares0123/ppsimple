package com.magrathea.ppsimple.infra.configurations

import com.magrathea.ppsimple.infra.adapters.outbound.gateways.clients.FeignErrorDecoder
import feign.Logger
import feign.codec.ErrorDecoder
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignClientConfiguration() {

    @Bean
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

    @Bean
    fun provideLoggerLevel(): Logger.Level = Logger.Level.FULL

    @Bean
    fun provideErrorDecoder(): ErrorDecoder = FeignErrorDecoder()

}