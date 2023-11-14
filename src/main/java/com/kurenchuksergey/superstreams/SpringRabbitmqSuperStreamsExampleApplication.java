package com.kurenchuksergey.superstreams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication(scanBasePackages = "com.kurenchuksergey.superstreams")
public class SpringRabbitmqSuperStreamsExampleApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringRabbitmqSuperStreamsExampleApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringRabbitmqSuperStreamsExampleApplication.class, args);
    }

    @Bean
    public CachingConnectionFactory connectionFactory(
            @Value("${applications.rabbitmq.prefetch.count:100}") int prefetchCount
    ) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setChannelListeners(
                List.of((c, t) -> {
                            try {
                                c.basicQos(prefetchCount);
                            } catch (Exception e) {
                                log.error("channel error", e);
                            }
                        }
                )
        );
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin amqpAdmin(CachingConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
