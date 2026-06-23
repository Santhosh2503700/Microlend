package com.microlend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.jwt.secret=TestSecretKeyForJWTTokenGeneration12345678901234567890",
    "app.jwt.expiration=86400000"
})
@DisplayName("Application Context Loads")
class MicroLendApplicationTests {

    @Test
    @DisplayName("Spring context should load without errors")
    void contextLoads() {
    }
}
