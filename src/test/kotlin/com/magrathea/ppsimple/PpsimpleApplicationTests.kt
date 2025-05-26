package com.magrathea.ppsimple

import io.github.cdimascio.dotenv.dotenv
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PpsimpleApplicationTests {

    init {
        dotenv {
            systemProperties = true
            filename = ".env.test"
        }
    }

    @Test
    fun contextLoads() {
    }

}
