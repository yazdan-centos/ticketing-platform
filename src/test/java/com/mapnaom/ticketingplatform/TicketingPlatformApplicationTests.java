package com.mapnaom.ticketingplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // run against in-memory H2 so the context loads without an external Postgres
class TicketingPlatformApplicationTests {

    @Test
    void contextLoads() {
    }

}
