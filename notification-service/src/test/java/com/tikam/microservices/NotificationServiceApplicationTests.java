package com.tikam.microservices;

import com.tikam.microservices.notification_service.NotificationServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = NotificationServiceApplication.class)
class NotificationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
