package ua.alexlapada;


import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ua.alexlapada.container.PostgresTestContainer;
import ua.alexlapada.helper.DatabaseAssert;

import javax.persistence.EntityManager;

@ExtendWith({SpringExtension.class})
@SpringBootTest(
        classes = {Application.class},
        properties = {""})
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, RabbitAutoConfiguration.class})
@Transactional
@MockBeans({

})
public abstract class AbstractIntegrationServiceTest implements DatabaseAssert, PostgresTestContainer {
    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected EntityManager entityManager;

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
