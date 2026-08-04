package com.olek.banking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifies that the application context starts with infrastructure disabled.
 */
@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.jdbc.="
                        + "DataSourceAutoConfiguration,="
                        + "org.springframework.boot.autoconfigure.orm.jpa.="
                        + "HibernateJpaAutoConfiguration,="
                        + "org.springframework.boot.autoconfigure.flyway.="
                        + "FlywayAutoConfiguration="
        }
)
class BankingPlatformApplicationTests {

    @Test
    void contextLoads() {
    }

}
