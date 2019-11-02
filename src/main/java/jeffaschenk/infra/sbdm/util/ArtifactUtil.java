package jeffaschenk.infra.sbdm.util;

import jeffaschenk.infra.sbdm.model.ArtifactServiceContents;
import jeffaschenk.infra.sbdm.model.ArtifactServiceProperties;
import jeffaschenk.infra.sbdm.model.ServiceStatus;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.LoggerFactory;

import java.io.*;

import static jeffaschenk.infra.sbdm.common.Constants.LOG_HEADER_SHORT;

/**
 * ArtifactUtil
 *
 * @author jaschenk
 */
public class ArtifactUtil {
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(ArtifactUtil.class);


    /**
     * readArtifact
     *
     * Read Artifact From Local Storage.
     *
     * @param serviceStatus -- Service POJO indicating which Artifact to be read.
     * @return InputStream of Artifact Contents.
     */
    public static InputStream readArtifact(final ServiceStatus serviceStatus) {
        /**
         * Establish Local Artifact File Object.
         */
        if (serviceStatus == null) {
            LOGGER.warn("{} No Service provided for Reading Local Artifact Content!", LOG_HEADER_SHORT);
            return null;
        }
        /**
         * Access our Artifact Content File.
         */
        File artifactContentFile = new File(serviceStatus.getServiceDirectoryPath());
        return readAnyServiceArtifact(artifactContentFile);
    }

    /**
     * readAnyServiceArtifact
     *
     * Read Artifact From Local Storage.
     *
     * @param artifactContentFile -- File Object referencing artifact.
     * @return InputStream of Artifact Contents.
     */
    public static InputStream readAnyServiceArtifact(final File artifactContentFile) {
        if (artifactContentFile.exists() && !artifactContentFile.isDirectory()) {
            try {
                return new BufferedInputStream(new FileInputStream(artifactContentFile));
            } catch(IOException ioe) {
                LOGGER.error("{} IOException encountered while accessing Artifact from " +
                        "Local File Storage from File:[{}]", LOG_HEADER_SHORT, artifactContentFile.getAbsolutePath());
                throw new IllegalStateException(ioe.getMessage(), ioe);
            }
        } else {
            LOGGER.error("{} Unable to Locate Local Artifact:[{}]!", LOG_HEADER_SHORT,
                    artifactContentFile.getAbsolutePath());
            return null;
        }
    }

    /**
     * saveArtifact
     *
     * @param serviceStatus -- Reference to Service Object
     * @param contents -- Artifact Contents
     * @param properties -- Artifact Contents
     */
    public static void saveArtifact(final ServiceStatus serviceStatus,
                                            final ArtifactServiceContents contents, final ArtifactServiceProperties properties) {
        /**
         * Save our Artifact Content File to Local File Storage.
         */
        File artifactContentFile = new File(serviceStatus.formulateNewPath(contents.getOriginalFilename()));
        /**
         * Check for existing File???
         */
        if (artifactContentFile.exists()) {
            String message = String.format("Attempting to Save an Artifact File:[%s], which already Exists, will archive ...",
                    artifactContentFile.getAbsolutePath());
            LOGGER.warn(message);
            File archiveExistingContents = new File(serviceStatus.formulateArchivePath(artifactContentFile.getName()));
            /**
             * Copy the existing File to an Archive ...
             */
            if (!FileUtil.copyFile(artifactContentFile, archiveExistingContents)) {
                message = String.format("Attempted to Archive an Artifact File:[%s], which already Exists to %s, however Issues Occurred!",
                        artifactContentFile.getName(), archiveExistingContents.getAbsolutePath());
                LOGGER.warn(message);
                throw new IllegalStateException(message);
            }
            /**
             * Now delete that existing File...
             */
            if(!artifactContentFile.delete()) {
                message = String.format("Attempted to remove original Artifact File:[%s], which was Archived to %s, however unable to delete file!",
                        artifactContentFile.getName(), archiveExistingContents.getAbsolutePath());
                LOGGER.warn(message);
                throw new IllegalStateException(message);
            }
        }
        /**
         * Now Perform the Actual Save of the Artifact Content.
         */
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(artifactContentFile, false);
            IOUtils.copy(new ByteArrayInputStream(contents.getFileData()), outputStream);
            outputStream.close();
            LOGGER.info("{} Successfully SAVED Artifact to Local File Storage:[{}]",
                    LOG_HEADER_SHORT,
                    artifactContentFile.getAbsolutePath());
        } catch (IOException ioe) {
            LOGGER.error("{} IOException encountered while attempting Save Artifact Content to " +
                    "Local File Storage for File:[{}] -- {}",
                    LOG_HEADER_SHORT,
                    artifactContentFile.getAbsolutePath(),
                    ioe.getMessage());
            throw new IllegalStateException(ioe.getMessage(), ioe);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch(IOException ioe) {
                    // Do Nothing...
                }
            }
        }
    }
}
