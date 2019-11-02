package jeffaschenk.infra.sbdm.services;

import jeffaschenk.infra.sbdm.model.ServiceStatus;
import jeffaschenk.infra.sbdm.util.FileUtil;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static jeffaschenk.infra.sbdm.common.Constants.LOG_HEADER;
import static jeffaschenk.infra.sbdm.util.BashScriptUtil.generateDeploymentScript;
import static org.junit.Assert.*;

/**
 * DeploymentManagerServiceTest
 *
 * @author jaschenk
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class DeploymentManagerServiceTest {
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(DeploymentManagerServiceTest.class);

    private static final String DM_TEST_DATA_PATH =
            "src/test/resources/DM_TEST_DATA/springboot-dm-test";

    private static final String TEST_SERVICE_NAME =
            "testServiceC";

    /**
     * Service Layer
     */
    @Autowired
    @Qualifier("deploymentManagerService")
    private DeploymentManagerService deploymentManagerService;
    /**
     * OS Commander Service
     */
    @Autowired
    @Qualifier("osEnvService")
    private OSEnvService osEnvService;

    @Value("${java.io.tmpdir}")
    private String tmpDir;

    @Test
    public void test01_validateServices() throws Exception {
        assertNotNull(deploymentManagerService);
        assertNotNull(osEnvService);
        /**
         * Initialize ...
         */
        Optional<File> testDir = osEnvService.getRuntimeSpringBootDirectory();
        assertEquals(tmpDir+(tmpDir.endsWith(File.separator)?"":File.separator)+"springboot-dm-test",
                testDir.get().getAbsolutePath());
        assertTrue(testDir.get().exists());
        assertTrue(testDir.get().isDirectory());
        assertTrue(testDir.get().canRead());
        assertTrue(testDir.get().canWrite());
        initializeTestDirectory(testDir);
        LOGGER.info("Established {}: {} ", LOG_HEADER, testDir.get().getAbsolutePath());

        /**
         * Now Perform Base functionality on Test Deployment Directory...
         */
        Optional<List<ServiceStatus>> availableServices = deploymentManagerService.availableServices();
        assertTrue(availableServices.isPresent());
        assertTrue(availableServices.get().size() == 4 || availableServices.get().size() == 5);

        /**
         * Spin Through and get status for Each...
         */
        for(ServiceStatus serviceStatus : availableServices.get()) {
            LOGGER.info("** Found Service: {}", serviceStatus.toString());
            Optional<ServiceStatus> serviceStatus_1 = deploymentManagerService.serviceStatus(serviceStatus.getServiceName());
            assertTrue(serviceStatus_1.isPresent());
        }

        /**
         * Attempt to get a invalid or unknown Service.
         */
        assertFalse(deploymentManagerService.serviceStatus("foobarService").isPresent());

        /**
         * Test Deployment Script Generation ...
         */
        Optional<ServiceStatus> serviceStatus = deploymentManagerService.serviceStatus("testServiceC");
        assertTrue(serviceStatus.isPresent());
        Optional<String> scriptBody = generateDeploymentScript(serviceStatus.get(), "jaschenk");
        assertTrue(scriptBody.isPresent());
        assertNotNull(scriptBody.get());
        LOGGER.info("{}", scriptBody.get());

        /**
         * Now finalize this Test Case ...
         */
        finalizeTestDirectory(testDir);
    }

    private static void initializeTestDirectory(Optional<File> testDir) throws IOException {
        assertTrue(testDir.isPresent());
        LOGGER.info("Established {}: {} ", LOG_HEADER, testDir.get().getAbsolutePath());
        assertTrue(testDir.get().exists() && testDir.get().canRead() && testDir.get().canWrite());
        /**
         * Now Copy Image from resources to this Temporary Directory ...
         */
        File src = new File(DM_TEST_DATA_PATH);
        File dest = new File(testDir.get().getAbsolutePath());
        LOGGER.info("{}: Creating Test Directory {} from Testing from Source Template {} ...", LOG_HEADER,
                dest.getAbsolutePath(), src.getAbsolutePath());
        FileUtil.copyFolder(src, dest);
        LOGGER.info("{}: Template Directory Copied to {} for Testing ...", LOG_HEADER, testDir.get().getAbsolutePath());
    }

    private static void finalizeTestDirectory(Optional<File> testDir) {
        assertTrue(testDir.isPresent());
        LOGGER.info("Removing Test Directory Established {}: {} ", LOG_HEADER, testDir.get().getAbsolutePath());
        assertTrue(testDir.get().exists() && testDir.get().canRead() && testDir.get().canWrite());
        /**
         * Now Delete this Temporary Directory ...
         */
        FileUtil.deleteFolder(testDir.get().getAbsolutePath());
        LOGGER.info("{}: Temp Directory deleted {}", LOG_HEADER, testDir.get().getAbsolutePath());
    }

}
