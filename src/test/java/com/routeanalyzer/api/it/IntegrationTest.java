package com.routeanalyzer.api.it;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;

import javax.annotation.PostConstruct;

import static java.lang.String.format;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    private static final String LOCALHOST_URL = "http://localhost";

    protected static final String DOCKER_COMPOSE_MONGO_DB = "src/test/resources/mongodb/docker-compose.yml";
    protected static final String MONGO_CONTAINER_NAME = "mongodb";
    protected static final int MONGO_PORT = 27017;

    protected TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int localPort;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @PostConstruct
    public void initialize() {
        this.testRestTemplate = new TestRestTemplate(restTemplateBuilder
                .rootUri(format("%s:%d",LOCALHOST_URL,localPort)));
    }

}
