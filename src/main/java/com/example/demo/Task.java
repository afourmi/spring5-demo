package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document
public class Task {

    @Id
    private String id;

    private String state;

    private Map<String, Object> record;

    public Task() {

    }

    public Task(String state, Map<String, Object> record) {
        this.state = state;
        this.record = record;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Map<String, Object> getRecord() {
        return record;
    }

    public void setRecord(Map<String, Object> record) {
        this.record = record;
    }

    public String toString() {
        return "Task[id=" + id + " , state=" + state + ", record=" + record + "]";
    }
}
