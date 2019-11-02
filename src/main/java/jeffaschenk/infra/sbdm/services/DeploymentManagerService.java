package jeffaschenk.infra.sbdm.services;

import jeffaschenk.infra.sbdm.model.ArtifactServiceContents;
import jeffaschenk.infra.sbdm.model.ArtifactServiceProperties;
import jeffaschenk.infra.sbdm.model.ServiceFile;
import jeffaschenk.infra.sbdm.model.ServiceStatus;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * DeploymentManagerService
 * Provides Service Layer for all Deployment Manager Actions.
 *
 * @author jaschenk
 */
public interface DeploymentManagerService {

    /**
     * availableServices
     *
     * @return Optional<List<ServiceStatus>> - response providing detail Status for each available application
     */
    Optional<List<ServiceStatus>> availableServices();

    /**
     * serviceStatus
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    Optional<ServiceStatus> serviceStatus(String serviceName);

    /**
     * serviceStart
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    Optional<ServiceStatus> serviceStart(String serviceName);

    /**
     * serviceStop
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    Optional<ServiceStatus> serviceStop(String serviceName);

    /**
     * serviceCleanUp
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    Optional<ServiceStatus> serviceCleanUp(String serviceName);

    /**
     * serviceDeploy
     *
     * @param serviceStatus -- Existing Service or Application Object to perform action against
     * @return boolean - response providing Deployment was successful or not ...
     */
    boolean serviceDeploy(ServiceStatus serviceStatus);

    /**
     * serviceConfiguration
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return String - response providing detail Show
     */
    String serviceConfiguration(String serviceName);

    /**
     * serviceJournal
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return String - response providing detail Journal
     */
    String serviceJournal(String serviceName);

    /**
     * serviceUpload
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @param contents
     * @param properties
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    Optional<ServiceStatus> serviceUpload(String serviceName, ArtifactServiceContents contents, ArtifactServiceProperties properties);

    /**
     * serviceDownload
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @param filename -- filename to be downloaded
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    Optional<InputStream> serviceDownload(String serviceName, String filename);

    /**
     * serviceFiles
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return Optional<List<ServiceFile>> - response providing contrived collection
     */
    Optional<List<ServiceFile>> serviceFiles(String serviceName);

}
