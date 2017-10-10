package com.example.ref;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.Task;
import com.example.demo.TaskSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

/**
 * Sample producer application using Reactive API for Kafka.
 * To run sample producer
 * <ol>
 * <li>Start Zookeeper and Kafka server
 * <li>Update {@link #BOOTSTRAP_SERVERS} and {@link #TOPIC} if required
 * <li>Create Kafka topic {@link #TOPIC}
 * <li>Run {@link SampleProducer} as Java application with all dependent jars in the CLASSPATH (eg. from IDE).
 * <li>Shutdown Kafka server and Zookeeper when no longer required
 * </ol>
 */
@Component
public class SampleProducer {

    private static final Logger log = LoggerFactory.getLogger(SampleProducer.class.getName());

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    private static final String TOPIC = "tasks";

    private final KafkaSender<Task, Task> sender;

    public SampleProducer() {

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "sample-producer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, TaskSerializer.class);
        SenderOptions<Task, Task> senderOptions = SenderOptions.create(props);

        sender = KafkaSender.create(senderOptions);
    }

    public Mono<Task> sendAndReturn(Task paramTask) {
        return sender
                .send(Mono.just(SenderRecord.create(new ProducerRecord<>(TOPIC, paramTask), paramTask)))
                .doOnError(throwable -> log.error("Send failed", throwable))
                .doOnNext(result -> log.info("message send " + result.correlationMetadata()))
                .map(SenderResult::correlationMetadata)
                .next()
        ;
    }

    public void close() {
        sender.close();
    }
}
