package com.kurenchuksergey.superstreams.consumers

import com.kurenchuksergey.superstreams.CONSUMER
import com.kurenchuksergey.superstreams.declarations.StreamDeclaration
import com.rabbitmq.stream.Environment
import com.rabbitmq.stream.OffsetSpecification
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.rabbit.stream.listener.StreamListenerContainer

@Configuration
@Profile(CONSUMER)
class UnnamedConsumers {

    @Bean
    fun unnamed1(env: Environment) = StreamListenerContainer(env).apply {
        setQueueNames(StreamDeclaration.NAME)
        setupMessageListener {
            LoggerFactory.getLogger("Unnamed1").info("receive the message:{}", String(it.body))
        }
        setConsumerCustomizer { _, builder ->
            builder.offset(OffsetSpecification.first())
        }
    }

    @Bean
    fun unnamed2(env: Environment) = StreamListenerContainer(env).apply {
        setQueueNames(StreamDeclaration.NAME)
        setupMessageListener {
            LoggerFactory.getLogger("Unnamed2").info("receive the message:{}", String(it.body))
        }
        setConsumerCustomizer { _, builder ->
            builder.offset(OffsetSpecification.first())
        }
    }
}