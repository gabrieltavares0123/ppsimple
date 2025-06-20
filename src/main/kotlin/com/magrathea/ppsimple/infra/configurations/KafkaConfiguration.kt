package com.magrathea.ppsimple.infra.configurations

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfiguration {
    @Bean
    fun createTransactionNotificationTopic(): NewTopic = TopicBuilder.name("transaction-notification").build()

    @Bean
    fun createTransactionNotificationDltTopic(): NewTopic = TopicBuilder.name("transaction-notification-dlt").build()
}
