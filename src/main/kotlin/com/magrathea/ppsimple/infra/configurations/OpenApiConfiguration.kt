package com.magrathea.ppsimple.infra.configurations

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration {

    @Bean
    fun provideApiKeyScheme(): SecurityScheme =
        SecurityScheme().type(SecurityScheme.Type.HTTP).bearerFormat("JWT").scheme("bearer")

    @Bean
    fun provideOpenApi(
        securityScheme: SecurityScheme
    ): OpenAPI {
        val devServer = Server()
        devServer.url = "http://localhost:8080"
        devServer.description = "Development environment"

        val contact = Contact()
        contact.email = "gabriel.tavaresramos@gmail.com"
        contact.name = "Gabriel Jorge"

        val mitLicense = License().name("MIT License").url("https://choosealicense.com/licenses/mit/")

        val info = Info()
            .title("Desafio PicPay")
            .version("1.0")
            .contact(contact)
            .description("Essa API expõe endpoints para o desafio backend PicPay")
            .license(mitLicense)

        return OpenAPI()
            .addSecurityItem(
                SecurityRequirement()
                    .addList("Bearer Authentication")
            ).components(
                Components()
                    .addSecuritySchemes(
                        "Bearer Authentication",
                        securityScheme
                    )
            ).info(
                info
            ).servers(
                listOf(
                    devServer
                )
            )
    }

}