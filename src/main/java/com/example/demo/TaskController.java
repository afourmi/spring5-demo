package com.example.demo;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
interface TaskRepository extends ReactiveMongoRepository<Task, String> {

    Flux<Task> findAll();

    Mono<Task> findById(String id);
}

@Configuration
class RoutingConfiguration {

    @Autowired
    private TaskRepository taskRepository;

    @Bean
    RouterFunction<?> routingFunction(SampleProducer producer) {
        return route(GET("/tasksFunctional"), req -> ServerResponse.ok().body(taskRepository.findAll(), Task.class))
                .andRoute(POST("/taskFunctional"), req -> {
                    Flux<Task> tasks = req.bodyToFlux(Task.class);
                    tasks.map(task -> taskRepository.save(task)).map(producer::send).subscribe();
                    return ServerResponse.ok().build();
                });
    }
}

@RestController
public class TaskController {

    @Autowired
    private SampleProducer producer;

    @Autowired
    private SampleConsumer consumer;

    @Autowired
    private TaskRepository taskRepository;

    @RequestMapping(path = "/tasks", method = RequestMethod.GET)
    public Flux<Task> getTasks() {
        return taskRepository.findAll();
    }

    @RequestMapping(path = "/task", method = RequestMethod.POST)
    public Flux<Disposable> createTask(@RequestBody Flux<Task> tasks) {
        // return taskRepository.saveAll(task).map(task1 -> {
        // producer.sendMessages(task1);
        // return task1;
        // });
        return tasks.map(task -> taskRepository.save(task)).map(task -> producer.send(task));
    }

    @RequestMapping(path = "/taskBlocking", method = RequestMethod.POST)
    public Iterable<Task> createTaskBlocking(@RequestBody List<Task> tasks) {
        return taskRepository.saveAll(tasks).map(task -> {
            producer.send(Mono.just(task));
            return task;
        }).collectList().block();
    }

    @RequestMapping(path = "events", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Task> listEvents() {
        return consumer.consumeMessages().map(ConsumerRecord::value);
    }
}

@Document
class Task {

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
