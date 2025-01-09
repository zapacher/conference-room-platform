package ee.ctob

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
//@ComponentScan(basePackages = ["ee.ctob"])
open class Application
    fun main(args: Array<String>) {
    runApplication<Application>(*args)
}