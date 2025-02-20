package ua.alexlapada;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import net.datafaker.Faker;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.alexlapada.helper.DatabaseAssert;

import javax.persistence.EntityManager;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {Application.class},
    webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
    exclude = {
//      GrpcServerAutoConfiguration.class,
//      GrpcServerFactoryAutoConfiguration.class,
//      GrpcClientAutoConfiguration.class,
//      GrpcClientHealthAutoConfiguration.class,
//      GrpcServerMetricAutoConfiguration.class
    })
@MockBeans({
})
public abstract class AbstractIntegrationGraphqlTest implements DatabaseAssert {

  @Autowired protected ApplicationContext applicationContext;
  @Autowired protected DgsQueryExecutor dgsQueryExecutor;
  @Autowired protected EntityManager em;

  protected Faker faker = new Faker();

  @Override
  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  protected void emClear() {
    em.clear();
  }
}
