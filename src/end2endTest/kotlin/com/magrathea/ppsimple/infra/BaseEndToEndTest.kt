package com.magrathea.ppsimple.infra

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.ConfluentKafkaContainer

@Testcontainers
abstract class BaseEndToEndTest {

    companion object {

        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:16")

        @Container
        @ServiceConnection
        val kafka = ConfluentKafkaContainer("confluentinc/cp-kafka:7.6.1")

    }

}