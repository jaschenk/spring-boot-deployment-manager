package jeffaschenk.infra.it;

import jeffaschenk.infra.sbdm.DeploymentManagerApplication;

import io.restassured.response.Response;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.get;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DeploymentManagerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Actuator_IT {

    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(Actuator_IT.class);

    @LocalServerPort
    private int localServerPort;

    @Test
    public void testActuatorMappings() {
        Response response =
                get(String.format("http://localhost:%d/deploymentManager/actuator/mappings", localServerPort));
        assertNotNull(response);
        assertNotNull(response.getBody());
        LOGGER.info("Response Length: {}",response.getBody().prettyPrint().length());
    }
}
