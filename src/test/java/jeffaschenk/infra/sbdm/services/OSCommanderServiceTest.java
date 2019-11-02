package jeffaschenk.infra.sbdm.services;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * OSCommandProcessTest
 *
 * @author jaschenk
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
public class OSCommanderServiceTest {
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(OSCommanderServiceTest.class);

    /**
     * OS Commander Service
     */
    @Autowired
    @Qualifier("osCommanderService")
    private OSCommanderService osCommanderService;


    @Test
    public void runOSCommandWithOutput() throws Exception {
        assertNotNull(osCommanderService);
        List<String> params = new ArrayList<>();
        if (osCommanderService.isOSWindows()) {
            String command = String.format("cmd.exe /c dir %s", osCommanderService.getHomeDirectory());
            LOGGER.info("Running WINDOWS via Builder Command {}", command);
            params.add("cmd.exe");
            params.add("/c");
            params.add("dir");
        } else {
            String command = String.format("sh -c ls %s", osCommanderService.getHomeDirectory());
            LOGGER.info("Running *NIX Command {}", command);
            params.add("sh");
            params.add("-c");
            params.add("ls");
            params.add("-a");
        }
        Optional<String> outputResult = osCommanderService.runOSCommandWithOutput(params,
                (osCommanderService.getHomeDirectory()).isPresent()?osCommanderService.getHomeDirectory().get():null);
        assertTrue(outputResult.isPresent());
        LOGGER.info("OS Command Output: {} {}", System.lineSeparator(), outputResult.get());
    }

}
