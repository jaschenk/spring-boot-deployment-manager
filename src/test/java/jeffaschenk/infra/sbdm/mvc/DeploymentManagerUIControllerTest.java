package jeffaschenk.infra.sbdm.mvc;

import jeffaschenk.infra.sbdm.controller.DeploymentManagerUIController;
import org.junit.BeforeClass;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

public class DeploymentManagerUIControllerTest {

    private static DeploymentManagerUIController controller;

    private static BindingResult mockedBindingResult;
    private static Model mockedModel;

    @BeforeClass
    public static void setUpControllerInstance() {
        mockedBindingResult = mock(BindingResult.class);
        mockedModel = mock(Model.class);
        controller = new DeploymentManagerUIController();
    }


}
