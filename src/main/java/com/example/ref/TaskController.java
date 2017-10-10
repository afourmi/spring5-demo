package com.example.ref;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

import com.example.demo.Task;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

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
                    tasks.flatMap(task -> taskRepository.save(task)).map(producer::sendAndReturn);
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

    @Autowired
    private TaskService taskService;

    @RequestMapping(path = "/tasks", method = RequestMethod.GET)
    public Flux<Task> getTasks() {
        return taskRepository.findAll();
    }

    @RequestMapping(path = "/task", method = RequestMethod.POST)
    public Flux<Task> createTask(@RequestBody Flux<Task> tasks) {
        return taskService
                .validateTask(tasks) //
                .zipWith(Flux.interval(Duration.ofMillis(1)), (task1, number) -> {
                    task1.getRecord().put("number", number);
                    return task1;
                })
                .doOnNext(task -> System.out.println("printing task " + task))
                .flatMap(task -> taskRepository.save(task))
                .flatMap(task -> producer.sendAndReturn(task));
    }

    @RequestMapping(path = "/taskBlocking", method = RequestMethod.POST)
    public Iterable<Task> createTaskBlocking(@RequestBody List<Task> tasks) {
        return taskRepository.saveAll(tasks).map(task -> {
            producer.sendAndReturn(task);
            return task;
        }).collectList().block();
    }

    @RequestMapping(path = "events", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Task> listEvents() {
        return consumer.consumeMessages().map(ConsumerRecord::value);
    }
}

@Service
class TaskService {

    private Logger LOG = Logger.getAnonymousLogger();

    public Flux<Task> validateTask(Flux<Task> tasks) {
        return tasks
                .map(task -> {
                    // some business rule here
                    if (task.getRecord() == null) {
                        throw new RuntimeException("invalid data");
                    }
                    return task;
                }) //
                .doOnError(throwable -> LOG.warning("validating tasks failed because of " + throwable.getCause()))
                .onErrorResume(throwable -> Mono.just(new Task("error", null)))
                .filter(task -> !task.getState().equals("error"));
    }
}

