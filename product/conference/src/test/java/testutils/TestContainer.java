package testutils;

import ee.ctob.Application;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ContextConfiguration(classes = Application.class)
@SpringBootTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:/migration",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "junit.jupiter.execution.parallel.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create"
})
@Testcontainers
public class TestContainer {

    @Container
    public static final PostgreSQLContainer PSQLcontainer =
            (PostgreSQLContainer) new PostgreSQLContainer("postgres:latest")
                    .withReuse(true);

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PSQLcontainer::getJdbcUrl);
        registry.add("spring.datasource.username", PSQLcontainer::getUsername);
        registry.add("spring.datasource.password", PSQLcontainer::getPassword);

    }

    @BeforeAll
    public static void containerStart() {
        PSQLcontainer.start();
    }

    @AfterAll
    public static void containerStop() {
        PSQLcontainer.stop();
    }
}
