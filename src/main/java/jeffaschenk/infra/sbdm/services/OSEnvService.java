package jeffaschenk.infra.sbdm.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * OSEnvService
 *
 * @author jaschenk
 */
public interface OSEnvService {

    /**
     * getRuntimeSpringBootDirectory
     *
     * @return Optional<File> -- Reference to File for SA Directory
     */
    Optional<File> getRuntimeSpringBootDirectory();

    /**
     * createFileDirectoryPath
     *
     * @param directoryPath - Reference to instantiated File Object
     * @return Path of created Directory hierarchy
     */
    Path createFileDirectoryPath(File directoryPath) throws IOException;

    /**
     * getOwner of *NIX Processes for this Deployment Manager ...
     * @return String containing *NIX User Name of Owner.
     */
    String getOwner();

}
