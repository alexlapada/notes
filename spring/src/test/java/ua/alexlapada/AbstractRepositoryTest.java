package ua.alexlapada;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import ua.alexlapada.container.PostgresTestContainer;
import ua.alexlapada.helper.DatabaseAssert;

import javax.persistence.EntityManager;

/**
 * For a JPA test that focuses <strong>only</strong> on JPA components.
 *
 * <p>Using this configuration will disable full auto-configuration and instead apply only
 * configuration relevant to JPA tests. <b>Include in Context:</b>@Repository, EntityManager,
 * TestEntityManager, DataSource <b>Exclude in Context:</b>@Service, @Component, @Controller In case
 * you need to test using @Component annotation use @Import annotation
 *
 * <p>
 */
@DataJpaTest(
        properties = {
            ""
        })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = {""})
@ComponentScan({""})
@Import({PropertySourcesPlaceholderConfigurer.class})
@MockBeans({
})
public abstract class AbstractRepositoryTest implements PostgresTestContainer, DatabaseAssert {

    @Autowired
    protected EntityManager em;

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
