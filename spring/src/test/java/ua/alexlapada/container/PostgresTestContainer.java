package ua.alexlapada.container;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public interface PostgresTestContainer {
    @Container
    TemplateDatabaseContainer dbContainer = TemplateDatabaseContainer.getInstance();

    final class TemplateDatabaseContainer extends PostgreSQLContainer<TemplateDatabaseContainer> {
        private static final String IMAGE_NAME = "postgres:16.4";
        private static final String PASSWORD = "password";
        private static final String USER = "admin";

        public static TemplateDatabaseContainer container;

        private TemplateDatabaseContainer(final String imageName) {
            super(imageName);
            super.withDatabaseName("test")
                    .withUsername(USER)
                    .withPassword(PASSWORD)
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(new HostConfig()
                            .withPortBindings(new PortBinding(Ports.Binding.bindPort(25432), new ExposedPort(5432)))));
        }

        public static TemplateDatabaseContainer getInstance() {
            if (container != null) {
                return container;
            }
            return container = new TemplateDatabaseContainer(IMAGE_NAME);
        }

        @Override
        public void start() {
            super.start();
            System.setProperty("DB_URL", container.getJdbcUrl());
            System.setProperty("DB_USERNAME", container.getUsername());
            System.setProperty("DB_PASSWORD", container.getPassword());
        }

        @Override
        public void stop() {
            // do nothing, JVM handles shut down
        }
    }
}
