package ua.alexlapada.container;

import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public interface RabbitMqTestContainer {

    @Container
    TemplateRabbitMqContainer rabbitContainer = TemplateRabbitMqContainer.getInstance();

    final class TemplateRabbitMqContainer extends RabbitMQContainer {
        private static final String IMAGE_NAME = "rabbitmq:3.10.7";

        public static TemplateRabbitMqContainer container;

        private TemplateRabbitMqContainer(final String imageName) {
            super(imageName);
        }

        public static TemplateRabbitMqContainer getInstance() {
            if (container != null) {
                return container;
            }
            return container = new TemplateRabbitMqContainer(IMAGE_NAME);
        }

        @Override
        public void start() {
            super.start();
            System.setProperty("RMQ_USERNAME", container.getAdminUsername());
            System.setProperty("RMQ_PASSWORD", container.getAdminPassword());
            System.setProperty("RMQ_HOST", container.getHost());
            System.setProperty("RMQ_PORT", String.valueOf(container.getMappedPort(5672)));
        }

        @Override
        public void stop() {
            // do nothing, JVM handles shut down
        }
    }
}
