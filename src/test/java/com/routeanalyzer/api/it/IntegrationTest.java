package com.routeanalyzer.api.it;

import com.routeanalyzer.api.config.RouteAnalyzerConfiguration;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.DockerComposeContainer;

import javax.annotation.PostConstruct;
import java.io.File;

import static java.lang.String.format;

@TestConfiguration
@RunWith(SpringRunner.class)
@TestPropertySource("classpath:test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

    private static final String LOCALHOST_URL = "http://localhost";

    private static final String
            DOCKER_COMPOSE_MONGO_DB = "src/test/resources/mongodb/docker-compose.yml";
    private static final String
            MONGO_CONTAINER_NAME = "mongodb";
    private static final int
            MONGO_PORT = 27017;

    protected TestRestTemplate testRestTemplate;

    public static DockerComposeContainer mongoDbContainer =
            new DockerComposeContainer(new File(DOCKER_COMPOSE_MONGO_DB))
            .withExposedService(MONGO_CONTAINER_NAME, MONGO_PORT);

    static {
        mongoDbContainer.start();
    }

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
