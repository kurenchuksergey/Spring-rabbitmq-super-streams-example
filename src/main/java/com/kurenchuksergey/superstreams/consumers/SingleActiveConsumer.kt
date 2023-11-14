package com.kurenchuksergey.superstreams.consumers

import com.kurenchuksergey.superstreams.CONSUMER
import com.kurenchuksergey.superstreams.declarations.StreamDeclaration
import com.rabbitmq.stream.Environment
import com.rabbitmq.stream.OffsetSpecification
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.rabbit.stream.listener.StreamListenerContainer
import java.util.concurrent.Executors
import kotlin.concurrent.thread

@Configuration
@Profile(CONSUMER)
class SingleActiveConsumer {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(SingleActiveConsumer::class.java)
        private const val GROUP_NAME: String = "single-active-named-consumer"
        private val dispatcherA = Executors.newSingleThreadExecutor { run: Runnable ->
            thread(name = "thread-A", start = false) {
                run.run()
            }
        }.asCoroutineDispatcher()

        private val dispatcherB = Executors.newSingleThreadExecutor { run: Runnable ->
            thread(name = "thread-B", start = false) {
                run.run()
            }
        }.asCoroutineDispatcher()
    }

    @Bean
    fun streamGroupFirstConsumer(env: Environment) = createConsumer(env, dispatcherA)

    @Bean
    fun streamGroupSecondConsumer(env: Environment) = createConsumer(env, dispatcherB)

    private fun createConsumer(env: Environment, dispatcher: CoroutineDispatcher) = StreamListenerContainer(env).apply {
        setQueueNames(StreamDeclaration.NAME)
        setupMessageListener {
            runBlocking(dispatcher) {
                log.info("receive the message:{}", String(it.body))
            }
        }
        setConsumerCustomizer { _, builder ->
            builder.offset(OffsetSpecification.first())
                .singleActiveConsumer()
                .name(GROUP_NAME)
        }
    }
}