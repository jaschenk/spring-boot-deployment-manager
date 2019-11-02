package jeffaschenk.infra.sbdm.services;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static jeffaschenk.infra.sbdm.common.Constants.*;

/**
 * OSEnvServiceImpl
 *
 * @author jaschenk
 */
@Service("osEnvService")
public class OSEnvServiceImpl implements OSEnvService {
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(OSEnvService.class);

    /**
     * Service Initialization
     */
    @PostConstruct
    private void initialization() {
        /**
         * Establish Our Deployment Manager's Directory Realm...
         */
        File deploymentManagerSAFileDir = new File(normalizeStorageLocation(deploymentManagerSADirectory));
        if (deploymentManagerSAFileDir.exists() && deploymentManagerSAFileDir.canRead() && deploymentManagerSAFileDir.canWrite()) {
            deploymentManagerSAFileDirRealm = deploymentManagerSAFileDir;
            return;
        } else if (deploymentManagerSAFileDir.exists()) {
            LOGGER.error("{} Runtime Directory has Permission Issues, unable to Access: {}",
                    LOG_HEADER, deploymentManagerSAFileDir.getAbsolutePath());
        } else {
            /**
             * Entering here, our directory does not exist, so create the directory as required and validate.
             */
            try {
                Path createdPath = createFileDirectoryPath(deploymentManagerSAFileDir);
                LOGGER.info("{} Created Directory Path: {}",LOG_HEADER_SHORT, createdPath.toString());
                deploymentManagerSAFileDirRealm = deploymentManagerSAFileDir;
            } catch(IOException ioe) {
                LOGGER.error("{} Exception encountered while creating directory structure: {} -- {}",
                        LOG_HEADER, deploymentManagerSAFileDir.getAbsolutePath(), ioe.getMessage(), ioe );
            }
        }
        LOGGER.info("{} has been initialized.", OSEnvService.class.getSimpleName());
    }

    /**
     * Service Deconstruction
     */
    @PreDestroy
    private void destroy() {
        LOGGER.info("{} has been shutdown.", OSEnvService.class.getSimpleName());
    }

    /**
     * Environment Variable, default set in application.yml, override with runtime Java Property.
     */
    @Value( "${deployment.manager.sa.dir}" )
    private String deploymentManagerSADirectory;
    /**
     * File Object representing provided Directory Path from above propery.
     */
    private File deploymentManagerSAFileDirRealm;
    /**
     * Environment Variable, default set in application.yml, override with runtime Java Property.
     */
    @Value( "${deployment.manager.owner}" )
    private String deploymentManagerOwner;

    /**
     * getRuntimeSpringBootDirectory
     *
     * @return Optional<File> -- Reference to File for SA Directory
     */
    @Override
    public Optional<File> getRuntimeSpringBootDirectory() {
        if (deploymentManagerSAFileDirRealm != null) {
            return Optional.of(deploymentManagerSAFileDirRealm);
        }
        return Optional.empty();
    }

    /**
     * Private helper method to determine what OS we are running on and to see if we need to make any
     * adjustments to our Storage Location.
     *
     * @param fileStorageLocation - Obtained Storage Location from Properties.
     * @return String containing Normalized Storage Path if applicable.
     */
    private String normalizeStorageLocation(String fileStorageLocation) {
        if (!SystemUtils.IS_OS_WINDOWS) {
            if (fileStorageLocation.contains(":\\")) {
                String tmpdir = SystemUtils.getJavaIoTmpDir().getAbsolutePath()+File.separatorChar+DEFAULT_SPRING_BOOT_PREFIX;
                LOGGER.warn("{} Due to OS and Storage Location specified changing Storage Location to {}",
                        LOG_HEADER_SHORT, tmpdir);
                return tmpdir;
            }
        }
        return fileStorageLocation;
    }

    /**
     * createFileDirectoryPath
     *
     * @param directoryPath - Reference to instantiated File Object
     * @return Path of created Directory hierarchy
     */
    @Override
    public synchronized Path createFileDirectoryPath(File directoryPath) throws IOException {
        if (directoryPath == null) {
            throw new IllegalArgumentException("File Directory Path cannot be null");
        }
        return Files.createDirectories(directoryPath.toPath());
    }

    /**
     * getOwner of *NIX Processes for this Deployment Manager ...
     *
     * @return String containing *NIX User Name of Owner.
     */
    @Override
    public String getOwner() {
        return deploymentManagerOwner;
    }
}
