package com.virtualstore.notification_service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:notification;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class EmailTemplateRenderingTest {

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    void rendersOrderPlacedTemplateWithPayloadValues() {
        Context context = new Context();
        context.setVariable("name", "Charan");
        context.setVariable("orderId", "ORD-123");
        context.setVariable("orderDate", "2026-04-13");
        context.setVariable("totalAmount", "1499.00");
        context.setVariable("trackingLink", "https://example.com/track/ORD-123");
        context.setVariable("items", java.util.List.of(
                java.util.Map.of("name", "Keyboard", "quantity", 1, "price", "1499.00")));

        String html = templateEngine.process("order-placed", context);

        assertThat(html).contains("Charan");
        assertThat(html).contains("ORD-123");
        assertThat(html).contains("Keyboard");
        assertThat(html).contains("https://example.com/track/ORD-123");
    }
}
