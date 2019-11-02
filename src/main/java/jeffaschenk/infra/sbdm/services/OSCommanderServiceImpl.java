package jeffaschenk.infra.sbdm.services;

import org.apache.commons.lang3.SystemUtils;
import org.apache.tika.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static jeffaschenk.infra.sbdm.common.Constants.*;

@Service("osCommanderService")
public class OSCommanderServiceImpl implements OSCommanderService {
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(OSCommanderService.class);

    /**
     * Service Initialization
     */
    @PostConstruct
    private void initialization() {
        LOGGER.info("{} has been initialized.", OSCommanderService.class.getSimpleName());
    }

    /**
     * Service Deconstruction
     */
    @PreDestroy
    private void destroy() {
        LOGGER.info("{} has been shutdown.", OSCommanderService.class.getSimpleName());
    }

    /**
     * isOSWindows
     * Determine if we are running on a WINDOWS Operation System or not.
     *
     * @return boolean - True indicates running WINDOWS OS, False - indicates *NIX OS.
     */
    @Override
    public boolean isOSWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    /**
     * getHomeDirectory
     * Obtain the current home directory designation based upon OS and User running this process.
     *
     * @return File - representing the current Home Directory.
     */
    @Override
    public Optional<File> getHomeDirectory() {
        File homeDirectory = new File(System.getProperty("user.home"));
        if (homeDirectory.exists() && homeDirectory.isDirectory()) {
            return Optional.of(homeDirectory);
        } else {
            return Optional.empty();
        }
    }

    /**
     * getTempDirectory
     * Obtain the current Temp directory designation based upon OS and User running this process.
     *
     * @return File - representing the current Temp Directory.
     */
    @Override
    public Optional<File> getTempDirectory(String suffix) {
        File tempDirectory = new File(System.getProperty("java.io.tmpdir")+
                ((suffix!=null&&!suffix.isEmpty())?File.separator+suffix:""));
        if (!tempDirectory.exists()) {
            try {
                Files.createDirectories(tempDirectory.toPath());
            } catch(IOException ioe) {
                LOGGER.error("Exception Creating Temporary Directory Path: {} -- {}",
                        tempDirectory.getAbsolutePath(),ioe.getMessage(), ioe);
            }
        }
        if (tempDirectory.exists() && tempDirectory.isDirectory()) {
            return Optional.of(tempDirectory);
        } else {
            return Optional.empty();
        }
    }

    /**
     * runOSCommandWithOutput
     * Perform the OS Command and return STDOUT/STDERR Output.
     *
     * @param params    - Command Parameters to be used as Command.
     * @param directory - If not null, directory used initially for command.
     * @return String - containing the resultant STDOUT/STDERR output from the Command.
     */
    @Override
    public Optional<String> runOSCommandWithOutput(List<String> params, File directory) {
        LOGGER.info("Issuing OS Command: {} ", transformListToString(params));
        ProcessBuilder pb = new ProcessBuilder(params);
        if (directory != null && directory.isDirectory()) {
            pb.directory(directory);
        }
        pb.redirectErrorStream(true);  // redirect STDERR to STDOUT, so we have only one stream to contend with ...
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
            LOGGER.error("Exception has occurred Performing Command: {} -- {}",
                    transformListToString(params), e.getMessage(), e);
        }
        if (result != null) {
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }

    /**
     * runOSCommandWithNoOutput
     * Perform the OS Command and return boolean indicator if run or not.
     *
     * @param params - Command Parameters to be used as Command.
     * @return boolean - indicator if run or not.
     */
    @Override
    public boolean runOSCommandWithNoOutput(List<String> params) {
        String command = transformListToString(params);
        LOGGER.info("Issuing OS Command: {} ", command);
        String result = "";
        try {
            Process p = Runtime.getRuntime().exec(command);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            // get the result
            while ((result = reader.readLine()) != null) {
                LOGGER.info("{} Output Command Line: {}", LOG_HEADER_SHORT, result);
            }
            p.waitFor();
            // get the exit code
            LOGGER.info("{} OS Command exit: {}", LOG_HEADER_SHORT, p.exitValue());
            p.destroy();
            return true;
        } catch (Exception e) {
            LOGGER.error("Exception has occurred Performing Command: {} -- {}",
                    command, e.getMessage(), e);
        }
        return false;
    }

    /**
     * transformListToString
     *
     * @param params - Command Parameters
     * @return String contrived from concatenating parameters.
     */
    protected String transformListToString(List<String> params) {
        StringBuilder sb = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            params.forEach(value->sb.append(value).append(" "));
        }
        return sb.toString();
    }

}
