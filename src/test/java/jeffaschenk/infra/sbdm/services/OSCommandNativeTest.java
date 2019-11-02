package jeffaschenk.infra.sbdm.services;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static org.junit.Assert.assertNotNull;

/**
 * OSCommandProcessTest
 *
 * @author jaschenk
 */
public class OSCommandNativeTest {
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(OSCommandNativeTest.class);

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
    private static String homeDirectory = System.getProperty("user.home");


    @Test
    public void givenProcess_whenCreatingViaProcessBuilder_shouldSucceed() throws Exception {
        List<String> params = new ArrayList<>();
        if (IS_WINDOWS) {
            String command = String.format("cmd.exe /c dir %s", homeDirectory);
            LOGGER.info("Running WINDOWS via Builder Command {}", command);
            params.add("cmd.exe");
            params.add("/c");
            params.add("dir");
        } else {
            String command = String.format("sh -c ls %s", homeDirectory);
            LOGGER.info("Running *NIX Command {}", command);
            params.add("sh");
            params.add("-c");
            params.add("ls");
            params.add("-a");
        }
        String output = runOSCommandWithOutput(params, new File(homeDirectory));
        assertNotNull(output);
        LOGGER.info("Command Output: {} {}", System.lineSeparator(), output);
    }

    private static String runOSCommandWithOutput(List<String> params, File directory) {
        ProcessBuilder pb = new ProcessBuilder(params);
        if (directory != null && directory.isDirectory()) {
            pb.directory(directory);
        }
        pb.redirectErrorStream(true);
        Process p;
        String result = "";
        try {
            p = pb.start();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            p.waitFor();

            StringJoiner sj = new StringJoiner(System.lineSeparator());
            reader.lines().iterator().forEachRemaining(sj::add);
            result = sj.toString();

            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
