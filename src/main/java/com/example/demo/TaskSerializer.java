package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * Created by aurelien on 10/1/17.
 */
public class TaskSerializer implements Serializer<Task>{
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, Task data) {
        try {
            return new ObjectMapper().writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException();
        }
    }

    @Override
    public void close() {

    }
}
