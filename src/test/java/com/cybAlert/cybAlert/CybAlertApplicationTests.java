package com.cybAlert.cybAlert;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.docker.compose.enabled=false",
		"spring.datasource.url=jdbc:h2:mem:cyberwatch;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class CybAlertApplicationTests {

	@Test
	void contextLoads() {
	}

}
