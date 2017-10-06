package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

/**
 * Created by aurelien on 10/1/17.
 */
public class TaskDeserializer implements Deserializer<Task>{
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public Task deserialize(String topic, byte[] data) {
        try {
            return new ObjectMapper().readValue(data, Task.class);
        } catch (IOException e) {
            throw new SerializationException();
        }
    }

    @Override
    public void close() {

    }
}
