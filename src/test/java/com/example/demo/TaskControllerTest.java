package com.example.demo;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskControllerTest {

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext applicationContext;

    @Before
    public void setUp() throws Exception {
        this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
    }

//    @Test
    public void testCreateTask() {
        // check no existing tasks
        List<Task> tasks = webTestClient
                .get()
                .uri("/tasks")
                .exchange() //
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Task.class)
                .returnResult()
                .getResponseBody();
        assertEquals(0, tasks.size());

        // create 2 tasks
        webTestClient
                .post()
                .uri("/task")
                .body(BodyInserters.fromObject(Arrays.asList(new Task("New", Maps.newHashMap("firstName", "Matt")),
                        new Task("New", Maps.newHashMap("firstName", "Mike")))))
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        tasks = webTestClient
                .get()
                .uri("/tasks")
                .exchange() //
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Task.class)
                .returnResult()
                .getResponseBody();
        assertEquals(2, tasks.size());
        assertEquals("Matt", tasks.get(0).getRecord().get("firstName"));
        assertEquals("Mike", tasks.get(1).getRecord().get("firstName"));
    }
}