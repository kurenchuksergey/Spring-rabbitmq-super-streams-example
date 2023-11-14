package com.kurenchuksergey.superstreams.declarations

import com.kurenchuksergey.superstreams.PRODUCER
import com.rabbitmq.stream.Environment
import com.rabbitmq.stream.Producer
import kotlinx.coroutines.*
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.rabbit.stream.config.SuperStream


@Configuration
class SuperStreamDeclaration {
    companion object {
        const val NAME = "app-super-stream"
        const val PARTITIONS = 2
    }

    @Bean
    fun superStream(): SuperStream {
        return SuperStream(NAME, PARTITIONS) { _, p ->
            (0..<p).map { it.toString() }.toMutableList()
        }
    }

    @Bean
    @Profile(PRODUCER)
    @SuperStreamTemplate
    fun superStreamTemplate(env: Environment): Producer {
        return env.producerBuilder()
            .superStream(NAME)
            .routing { message ->
                (message.publishingId.mod(PARTITIONS)).toString()
            }
            .key()
            .producerBuilder()
            .build()
    }

    @Bean
    @Profile(PRODUCER)
    fun superStreamProducer(@SuperStreamTemplate producer: Producer, admin: AmqpAdmin): CommandLineRunner {
        return CommandLineRunner {
            GlobalScope.async {
                admin.initialize() //todo find reason and remove from here
                generateSequence(0) { it + 1 }.forEach {
                    producer.send(
                        producer.messageBuilder()
                            .publishingId(it.toLong())
                            .addData("message: $it".toByteArray())
                            .build()
                    ) {
                        //do nothing
                    }
                    println("produced $it message")
                    delay(1000)
                }
            }
        }
    }
}

@Qualifier
annotation class SuperStreamTemplate