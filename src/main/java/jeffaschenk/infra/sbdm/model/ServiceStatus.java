package jeffaschenk.infra.sbdm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ServiceStatus
 *
 * @author jaschenk
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode
public class ServiceStatus implements Serializable {

    public static final String ARTIFACT_FILE_TYPE = ".jar";
    public static final String ARTIFACT_CONFIG_FILE_TYPE = ".conf";

    public static final String ARTIFACT_NEW_FILE_TYPE = ".new";
    public static final String ARTIFACT_TEMP_FILE_TYPE = ".tmp";

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");

    @JsonProperty("serviceName")
    private String serviceName;

    @EqualsAndHashCode.Exclude
    @JsonProperty("serviceDirectoryPath")
    private String serviceDirectoryPath;

    @EqualsAndHashCode.Exclude
    @JsonProperty("serviceDeploymentStatus")
    private ServiceDeploymentStatus serviceDeploymentStatus;

    @EqualsAndHashCode.Exclude
    @JsonProperty("osTransientResponse")
    private String osTransientCommandResponse;

    @EqualsAndHashCode.Exclude
    @JsonProperty("serviceFiles")
    private List<ServiceFile> serviceFiles;

    @EqualsAndHashCode.Exclude
    @JsonProperty("badge")
    private String badge;

    @EqualsAndHashCode.Exclude
    @JsonProperty("deploymentMessageStatus")
    private String deploymentMessageStatus;


    @Transient
    public String getServiceArtifactPath() {
        return formulatePath(ARTIFACT_FILE_TYPE);
    }

    @Transient
    public String getServiceArtifactNewPath() {
        return formulatePath(ARTIFACT_FILE_TYPE + ARTIFACT_NEW_FILE_TYPE);
    }

    @Transient
    public String getServiceArtifactTempPath() {
        return formulatePath(ARTIFACT_FILE_TYPE + ARTIFACT_TEMP_FILE_TYPE);
    }

    @Transient
    public String getServiceArtifactConfigurationPath() {
        return formulatePath(ARTIFACT_CONFIG_FILE_TYPE);
    }

    @Transient
    public String getServiceArtifactConfigurationNewPath() {
        return formulatePath(ARTIFACT_CONFIG_FILE_TYPE + ARTIFACT_NEW_FILE_TYPE);
    }

    @Transient
    public String getServiceArtifactConfigurationTempPath() {
        return formulatePath(ARTIFACT_CONFIG_FILE_TYPE + ARTIFACT_TEMP_FILE_TYPE);
    }

    /**
     * formulatePath
     * Private Helper Method to formulate Path providing a suffix.
     *
     * @param suffix -- to be applied to path
     * @return String - contrived Path including suffix for this service Directory Path.
     */
    @Transient
    private String formulatePath(String suffix) {
        return this.serviceDirectoryPath + File.separator + this.serviceName + suffix;
    }

    /**
     * formulateNewPath
     * Private Helper Method to formulate Path providing a New suffix
     *
     * @param otherFileName -- Other than the Service Named Artifact.
     * @return String - contrived Path including suffix for this service Directory Path.
     */
    @Transient
    public String formulateNewPath(String otherFileName) {
        return this.serviceDirectoryPath + File.separator + otherFileName + ARTIFACT_NEW_FILE_TYPE;
    }

    /**
     * formulateNewPath
     * Private Helper Method to formulate Path providing a Temp suffix
     *
     * @param otherFileName -- Other than the Service Named Artifact.
     * @return String - contrived Path including suffix for this service Directory Path.
     */
    @Transient
    public String formulateTempPath(String otherFileName) {
        return this.serviceDirectoryPath + File.separator + otherFileName + ARTIFACT_TEMP_FILE_TYPE;
    }

    /**
     * formulateNewPath
     * Private Helper Method to formulate Path providing a Temp suffix
     *
     * @param otherFileName -- Other than the Service Named Artifact.
     * @return String - contrived Path including suffix for this service Directory Path.
     */
    @Transient
    public String formulateArchivePath(String otherFileName) {
        if (!otherFileName.startsWith(this.serviceDirectoryPath)) {
            return this.serviceDirectoryPath + File.separator + otherFileName + "." + LocalDateTime.now().format(TIMESTAMP_FORMAT);
        } else {
            return otherFileName + "." + LocalDateTime.now().format(TIMESTAMP_FORMAT);
        }
    }

}
