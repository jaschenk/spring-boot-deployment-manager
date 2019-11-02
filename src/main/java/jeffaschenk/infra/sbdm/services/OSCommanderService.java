package jeffaschenk.infra.sbdm.services;


import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * OSCommanderService
 *
 * @author jaschenk
 */
public interface OSCommanderService {

    /**
     * isOSWindows
     * Determine if we are running on a WINDOWS Operation System or not.
     *
     * @return boolean - True indicates running WINDOWS OS, False - indicates *NIX OS.
     */
    boolean isOSWindows();

    /**
     * getHomeDirectory
     * Obtain the current home directory designation based upon OS and User running this process.
     *
     * @return Optional<File> - representing the current Home Directory if available.
     */
    Optional<File> getHomeDirectory();

    /**
     * getTempDirectory
     * Obtain the current Temp directory designation based upon OS and User running this process.
     *
     * @return Optional<File> - representing the current Temp Directory if available.
     */
    Optional<File> getTempDirectory(String suffix);

    /**
     * runOSCommandWithOutput
     * Perform the OS Command and return STDOUT/STDERR Output.
     *
     * @param params - Command Parameters to be used as Command.
     * @param directory - If not null, directory used initially for command.
     * @return String - containing the resultant STDOUT/STDERR output from the Command.
     */
    Optional<String> runOSCommandWithOutput(List<String> params, File directory);

    /**
     * runOSCommandWithNoOutput
     * Perform the OS Command and return boolean indicator if run or not.
     *
     * @param params - Command Parameters to be used as Command.
     * @return boolean - indicator if run or not.
     */
    boolean runOSCommandWithNoOutput(List<String> params);

}
