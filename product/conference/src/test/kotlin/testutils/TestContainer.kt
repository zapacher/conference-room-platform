package testutils

import ee.ctob.Application
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.AfterAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.junit.jupiter.Container

@ContextConfiguration(classes = [Application::class])
@SpringBootTest
@TestPropertySource(properties = [
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:/migration",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "junit.jupiter.execution.parallel.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create"
])
@Testcontainers
open class TestContainer {

    companion object {
        @Container
        @JvmStatic
        val PSQLcontainer: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:latest")
                .withReuse(true)

        @DynamicPropertySource
        @JvmStatic
        fun overrideProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { PSQLcontainer.jdbcUrl }
            registry.add("spring.datasource.username") { PSQLcontainer.username }
            registry.add("spring.datasource.password") { PSQLcontainer.password }
        }

        @BeforeAll
        @JvmStatic
        fun containerStart() {
            PSQLcontainer.start()
        }

        @AfterAll
        @JvmStatic
        fun containerStop() {
            PSQLcontainer.stop()
        }
    }
}