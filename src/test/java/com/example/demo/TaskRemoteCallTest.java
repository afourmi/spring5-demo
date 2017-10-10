package com.example.demo;

import org.assertj.core.util.Maps;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

public class TaskRemoteCallTest {

    WebClient taskEndPoint = WebClient
            .builder() //
            .baseUrl("http://localhost:8080")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    // @Test
    public void testCreateTaskBlockingcall() {
        Flux<Task> tasksFlux = Flux.just(new Task("New", Maps.newHashMap("firstName", "Sean")));
        WebClient.RequestHeadersSpec<?> requestPreparation =
                taskEndPoint.post().uri("/taskBlocking").body(BodyInserters.fromPublisher(tasksFlux, Task.class));

        WebClient.ResponseSpec retrieve = requestPreparation.retrieve();
        Flux<Task> taskFlux = retrieve.bodyToFlux(Task.class);

        taskFlux.toIterable().forEach(System.out::println);
    }

    // @Test
    public void testCreateTaskNonBlockingcall() {
        Flux<Task> tasksFlux = Flux.just(new Task("New", Maps.newHashMap("firstName", "Robert")),
                new Task("New", Maps.newHashMap("firstName", "Alice")));
        WebClient.RequestHeadersSpec<?> requestPreparation =
                taskEndPoint.post().uri("/task").body(BodyInserters.fromPublisher(tasksFlux, Task.class));
        Flux<Task> resultFlux = requestPreparation.retrieve().bodyToFlux(Task.class);

        Flux<Task> map = resultFlux.map(task -> {
            System.out.println(task);
            return task;
        });

        Disposable subscribe = map.subscribe();
        System.out.println(subscribe);
    }
}