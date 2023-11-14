package com.kurenchuksergey.superstreams.declarations

import com.kurenchuksergey.superstreams.PRODUCER
import com.rabbitmq.stream.Environment
import com.rabbitmq.stream.ProducerBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate
import java.lang.Thread.sleep


@Configuration
class StreamDeclaration {
    companion object {
        const val NAME = "app-single-stream"
    }

    @Bean
    fun stream(): Queue = QueueBuilder.durable(NAME)
        .stream()
        .build()

    @Bean
    @StreamTemplate
    @Profile(PRODUCER)
    fun streamTemplate(env: Environment): RabbitStreamTemplate {
        val template = RabbitStreamTemplate(env, NAME)
        template.setProducerCustomizer { _, builder: ProducerBuilder -> builder.name("stream-producer") }
        return template
    }

    @Bean
    @Profile(PRODUCER)
    fun streamProducer(@StreamTemplate template: RabbitStreamTemplate, admin: AmqpAdmin) =
        CommandLineRunner {
            GlobalScope.launch {
                admin.initialize()  //todo find reason and remove from here
                generateSequence(0) { it + 1 }.forEach {
                    template.convertAndSend("message.stream:$it")
                    println("published:$it")
                    delay(1000)
                }
            }
        }
}

    @Qualifier
    annotation class StreamTemplate
