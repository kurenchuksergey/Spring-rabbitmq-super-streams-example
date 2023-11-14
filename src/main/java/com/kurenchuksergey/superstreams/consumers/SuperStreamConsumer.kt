package com.kurenchuksergey.superstreams.consumers

import com.kurenchuksergey.superstreams.CONSUMER
import com.kurenchuksergey.superstreams.declarations.SuperStreamDeclaration
import com.rabbitmq.stream.Environment
import com.rabbitmq.stream.OffsetSpecification
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.rabbit.stream.listener.StreamListenerContainer
import org.springframework.rabbit.stream.support.StreamMessageProperties
import java.util.concurrent.Executors
import kotlin.concurrent.thread

@Configuration
@Profile(CONSUMER)
class SuperStreamConsumer {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(SuperStreamConsumer::class.java)
        private const val GROUP_NAME: String = "super-stream-consumer"

        private val handler = CoroutineExceptionHandler { _, exception ->
            println("CoroutineExceptionHandler got $exception")
        }

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
    fun superStreamGroupFirstConsumer(env: Environment) =
        createConsumer(env, dispatcherA)

    @Bean
    fun superStreamGroupSecondConsumer(env: Environment) =
        createConsumer(env, dispatcherB)

    private fun createConsumer(env: Environment, dispatcher: CoroutineDispatcher) =
        StreamListenerContainer(env).apply {
            setupMessageListener {
                runBlocking(dispatcher + handler) {
                    val streamMessageProperties = it.messageProperties as StreamMessageProperties
                    log.info(
                        "stream[{}]: {}",
                        streamMessageProperties.context.stream(),
                        String(it.body),
                    )
                }
            }
            superStream(SuperStreamDeclaration.NAME, GROUP_NAME)
            setConsumerCustomizer { _, builder ->
                builder.offset(OffsetSpecification.first())
            }
        }
}