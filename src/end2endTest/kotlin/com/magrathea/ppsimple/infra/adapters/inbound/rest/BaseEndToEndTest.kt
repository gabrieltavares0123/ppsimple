package com.magrathea.ppsimple.infra.adapters.inbound.rest

import io.github.cdimascio.dotenv.dotenv
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class BaseEndToEndTest {

    init {
        dotenv {
            systemProperties = true
            filename = ".env.e2e"
        }

        postgres.start()
        kafka.start()
    }

    companion object {

        @JvmStatic
        @Container
        val postgres = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("ppsimple")
            withUsername("ppsimple")
            withPassword("ppsimple")
            waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)))
            withReuse(true)
        }

        @JvmStatic
        @Container
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1")).apply {
            waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)))
            withReuse(true)
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerPgProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }

            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
        }
    }

}