package com.magrathea.ppsimple.infra.configurations

import feign.Logger
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignClientConfiguration() {

    @Bean
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

    @Bean
    fun provideLoggerLevel(): Logger.Level = Logger.Level.FULL

}