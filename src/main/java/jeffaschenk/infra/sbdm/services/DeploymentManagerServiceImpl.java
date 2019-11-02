package jeffaschenk.infra.sbdm.services;

import jeffaschenk.infra.sbdm.common.Constants;
import jeffaschenk.infra.sbdm.controller.DeploymentManagerUIController;
import jeffaschenk.infra.sbdm.entity.DeploymentManagerEvent;
import jeffaschenk.infra.sbdm.model.*;
import jeffaschenk.infra.sbdm.repository.DeploymentManagerEventRepository;
import jeffaschenk.infra.sbdm.util.ArtifactUtil;
import jeffaschenk.infra.sbdm.util.BashScriptUtil;
import jeffaschenk.infra.sbdm.util.ServiceStatusFormatUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static jeffaschenk.infra.sbdm.common.Constants.*;
import static jeffaschenk.infra.sbdm.util.BashScriptUtil.deleteScriptFile;

/**
 * DeploymentManagerServiceImpl
 * Provides Service Layer Implementation for all Deployment Manager Actions.
 *
 * @author jaschenk
 */
@Service("deploymentManagerService")
public class DeploymentManagerServiceImpl implements DeploymentManagerService {
    /**
     * Common Logger
     */
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(DeploymentManagerService.class);

    /**
     * OS Environment Service
     */
    @Autowired
    @Qualifier("osEnvService")
    private OSEnvService osEnvService;

    /**
     * OS Commander Service
     */
    @Autowired
    @Qualifier("osCommanderService")
    private OSCommanderService osCommanderService;

    /**
     * Events Repository
     */
    @Autowired
    private DeploymentManagerEventRepository deploymentManagerEventRepository;

    /**
     * Service Initialization
     */
    @PostConstruct
    private void initialization() {
        logInfoMessageAndEvent(
                String.format("%s %s Initializing ...",
                        LOG_HEADER_SHORT, DeploymentManagerService.class.getSimpleName()));
        if (osEnvService.getRuntimeSpringBootDirectory().isPresent()) {
            logInfoMessageAndEvent(
                    String.format("%s Using Deployment Manager Root Directory: %s",
                            LOG_HEADER_SHORT, osEnvService.getRuntimeSpringBootDirectory()));
        } else {
            String message = String.format("%s No Deployment Manager Root Directory Established, meaning no Functionality will exist!",
                    LOG_HEADER_SHORT);
            LOGGER.warn(message);
            persistEventMessage(DM_SELF, message);
            return;
        }
        logInfoMessageAndEvent(
                String.format("%s %s has been initialized.",
                        LOG_HEADER_SHORT, DeploymentManagerService.class.getSimpleName()));
    }

    /**
     * Service Deconstruction
     */
    @PreDestroy
    private void destroy() {
        LOGGER.info("{} {} has been shutdown.", LOG_HEADER_SHORT, DeploymentManagerService.class.getSimpleName());
    }

    /**
     * availableServices
     *
     * @return Optional<List < ServiceStatus>> - response providing detail Status for each available application
     */
    @Override
    public Optional<List<ServiceStatus>> availableServices() {
        List<ServiceStatus> availableServices = new ArrayList<>();
        if (osEnvService.getRuntimeSpringBootDirectory().isPresent()) {
            /**
             * Obtain the top level directory path of existing deployment services names...
             */
            File[] files = osEnvService.getRuntimeSpringBootDirectory().get().listFiles(); //NOSONAR
            for (File file : files) {
                if (file.isDirectory()) {
                    if (Arrays.stream(Constants.DEPLOYMENT_DIRECTORIES_TO_BE_IGNORED).anyMatch(file.getName()::equals)) {
                        continue;
                    }
                    Optional<ServiceStatus> serviceStatusResult = serviceStatus(file.getName());
                    if (serviceStatusResult.isPresent()) {
                        availableServices.add(serviceStatusResult.get());
                    } else {
                        availableServices.add(new ServiceStatus(file.getName(), file.getAbsolutePath(), ServiceDeploymentStatus.CHECKING,
                                null, null, null, null));
                    }
                }
            }
        }
        availableServices.sort(Comparator.comparing(ServiceStatus::getServiceName, String.CASE_INSENSITIVE_ORDER));
        return Optional.of(availableServices);
    }

    /**
     * serviceStatus
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    @Override
    public Optional<ServiceStatus> serviceStatus(String serviceName) {
        Optional<ServiceStatus> result = getServiceStatusObject(serviceName);
        if (result.isPresent()) {
            ServiceStatus serviceStatus = result.get();
            // Get Runtime Status ...
            Optional<ServiceStatus> serviceStatusResult = (Optional<ServiceStatus>) serviceAction(serviceName, ACTION_STATUS); // NOSONOR
            if (serviceStatusResult.isPresent()) {
                serviceStatus = serviceStatusResult.get();
                serviceStatus.setServiceFiles(getNamedServiceDirectoryContents(serviceName));
                ServiceStatusFormatUtil.applyRuntimeStatus(serviceStatus);
            }
            return Optional.of(serviceStatus);
        }
        LOGGER.warn("{} Service Name: {}, Not Found!", LOG_HEADER_SHORT, serviceName);
        return Optional.empty();
    }

    /**
     * serviceConfiguration
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    @Override
    public String serviceConfiguration(String serviceName) {
        Optional<String> result = (Optional<String>) serviceAction(serviceName, ACTION_SHOW); //NOSONOR
        if (result.isPresent()) {
            return ServiceStatusFormatUtil.formatConfigurationResponse(true, result.get());
        } else {
            return null;
        }
    }

    /**
     * serviceJournal
     * ** Due to the fact hat this command may infact hang, we are not going to use this from a upstream caller...
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    @Override
    public String serviceJournal(String serviceName) {
        // Get Runtime Journal ...
        /**
         Optional<String> result = (Optional<String>)serviceAction(serviceName, ACTION_JOURNAL); //NOSONOR
         if (result.isPresent()) {
         return result.get();
         } else {
         return null;
         }
         **/
        return "";
    }

    /**
     * serviceStart
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    @Override
    public Optional<ServiceStatus> serviceStart(String serviceName) {
        Optional<ServiceStatus> serviceStatusResult = (Optional<ServiceStatus>) serviceAction(serviceName, ACTION_START); // NOSONOR
        if (serviceStatusResult.isPresent()) {
            serviceStatusResult = serviceStatus(serviceName);
            if (serviceStatusResult.isPresent() && serviceStatusResult.get().getBadge().equalsIgnoreCase(ACTIVE_BADGE)) {
                return serviceStatusResult;
            }
            LOGGER.warn(ACTION_ERROR_MESSAGE_02, LOG_HEADER_SHORT, serviceName, ACTION_START);
            return serviceStatusResult;
        } else {
            LOGGER.warn(ACTION_ERROR_MESSAGE_01, LOG_HEADER_SHORT, serviceName, ACTION_START);
        }
        return Optional.empty();
    }

    /**
     * serviceStop
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    @Override
    public Optional<ServiceStatus> serviceStop(String serviceName) {
        Optional<ServiceStatus> serviceStatusResult = (Optional<ServiceStatus>) serviceAction(serviceName, ACTION_STOP); // NOSONOR
        if (serviceStatusResult.isPresent()) {
            serviceStatusResult = serviceStatus(serviceName);
            if (serviceStatusResult.isPresent() && serviceStatusResult.get().getBadge().equalsIgnoreCase(INACTIVE_BADGE)) {
                return serviceStatusResult;
            }
            LOGGER.warn(ACTION_ERROR_MESSAGE_02, LOG_HEADER_SHORT, serviceName, ACTION_STOP);
            return serviceStatusResult;
        } else {
            LOGGER.warn(ACTION_ERROR_MESSAGE_01, LOG_HEADER_SHORT, serviceName, ACTION_STOP);
        }
        return Optional.empty();
    }

    /**
     * serviceDeploy
     *
     * @param serviceStatus -- Existing Service or Application Object to perform action against
     * @return boolean - response providing Deployment was successful or not ...
     */
    @Override
    public boolean serviceDeploy(ServiceStatus serviceStatus) {
        LOGGER.info("{} Deploy of Service: {} Starting ...", LOG_HEADER_SHORT, serviceStatus.getServiceName());
        String owner = osEnvService.getOwner();
        /**
         * Generate the Deployment Script
         */
        Optional<String> scriptResult = BashScriptUtil.generateDeploymentScript(serviceStatus, owner);
        if (!scriptResult.isPresent()) {
            LOGGER.warn(ACTION_ERROR_MESSAGE_03, LOG_HEADER_SHORT, serviceStatus.getServiceName(), ACTION_ERROR_MESSAGE_03_TEMPLATE);
            serviceStatus.setServiceDeploymentStatus(ServiceDeploymentStatus.FAILED_DEPLOYMENT);
            serviceStatus.setDeploymentMessageStatus(ACTION_ERROR_MESSAGE_03_TEMPLATE);
            return false;
        }
        /**
         * Now persist the Script to the Operating System
         */
        if(BashScriptUtil.deleteScriptFile(serviceStatus)) {
            LOGGER.info("{} Previous Deployment Script deleted from Service Directory: {}.",
                    LOG_HEADER_SHORT, serviceStatus.getServiceDirectoryPath());
        }
        File scriptFile = BashScriptUtil.createScriptFile(serviceStatus, scriptResult.get());
        if (scriptFile == null || !scriptFile.exists()) {
            LOGGER.warn(ACTION_ERROR_MESSAGE_03, LOG_HEADER_SHORT, serviceStatus.getServiceName(), ACTION_ERROR_MESSAGE_03_TEMPLATE);
            serviceStatus.setServiceDeploymentStatus(ServiceDeploymentStatus.FAILED_DEPLOYMENT);
            serviceStatus.setDeploymentMessageStatus(ACTION_ERROR_MESSAGE_03_TEMPLATE);
            return false;
        }
        LOGGER.info("{} Created new Deployment Script in Service Directory: {}.",
                LOG_HEADER_SHORT, serviceStatus.getServiceDirectoryPath());
        /**
         * Now Execute the Persisted Script ...
         */
            String realServiceName = serviceStatus.getServiceName();
            if (serviceStatus.getServiceName().equalsIgnoreCase(DEPLOYMENT_MANAGER_EUREKA_DIRECTORY)) {
                realServiceName = DEPLOYMENT_MANAGER_EUREKA_SERVICE_NAME;
            }
            /**
             * Prepare Deployment Execution ...
             */
            List<String> params = new ArrayList<>();
            if (osCommanderService.isOSWindows()) {
                params.add("cmd.exe");
                params.add("/c");
                params.add("echo");
                params.add("deploy");
                params.add(realServiceName);
            } else {
                params.add("bash");
                params.add(scriptFile.getAbsolutePath());
            }
            Optional<String> deploymentResult = osCommanderService.runOSCommandWithOutput(params,
                    (osCommanderService.getHomeDirectory()).isPresent() ? osCommanderService.getHomeDirectory().get() : null); //NOSONAR
            /**
             * Return most recent Result ...
             */
            serviceStatus.setServiceDeploymentStatus(ServiceDeploymentStatus.DEPLOYED);
            serviceStatus.setDeploymentMessageStatus(deploymentResult.isPresent() ? deploymentResult.get() : "");
            return true;
}


    /**
     * contriveServiceStatusAfterDeployment
     * Private helper method to contive the Service Status after a Deployment.
     *
     * @param serviceName             - Name of Service
     * @param deploymentMessage       - Deployment Message
     * @param serviceDeploymentStatus - Status
     * @return ServiceStatus - Obtained or Contrived Service Status Object
     */
    private ServiceStatus contriveServiceStatusAfterDeployment(String serviceName, String deploymentMessage,
                                                               ServiceDeploymentStatus serviceDeploymentStatus) {
        Optional<ServiceStatus> serviceStatusResult = getServiceStatusObject(serviceName);
        if (serviceStatusResult.isPresent()) {
            serviceStatusResult.get().setServiceDeploymentStatus(serviceDeploymentStatus);
            serviceStatusResult.get().setDeploymentMessageStatus(deploymentMessage);
            return serviceStatusResult.get();
        } else {
            return new ServiceStatus(serviceName, null, serviceDeploymentStatus,
                    null, new ArrayList<>(), null, deploymentMessage);
        }
    }

    /**
     * serviceCleanUp
     * Called before certain operations to clean-up the Named service Directory.
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    @Override
    public Optional<ServiceStatus> serviceCleanUp(String serviceName) {
        int filesDeleted = 0;
        List<ServiceFile> serviceFilesDeleted = new ArrayList<>();
        Optional<ServiceStatus> result = serviceStatus(serviceName);
        if (result.isPresent()) {
            ServiceStatus serviceStatus = result.get();
            if (serviceStatus.getServiceFiles() != null) {
                for (ServiceFile serviceFileObject : serviceStatus.getServiceFiles()) {
                    File serviceFile = new File(serviceFileObject.getServiceFilePath());
                    if (serviceFile.exists() && !serviceFile.isDirectory() &&
                            (serviceFile.getName().endsWith(ServiceStatus.ARTIFACT_NEW_FILE_TYPE) ||
                                    serviceFile.getName().endsWith(ServiceStatus.ARTIFACT_TEMP_FILE_TYPE) ||
                                    serviceFile.getName().endsWith(DEPLOYMENT_SCRIPT_NAME))) {
                        if (removeServiceFile(serviceFile.getPath())) {
                            filesDeleted++;
                            LOGGER.info("{} Successfully Deleted File: {} from Service: {}",
                                    LOG_HEADER_SHORT, serviceFile.getName(), serviceName);
                            serviceFilesDeleted.add(serviceFileObject);
                        } else {
                            LOGGER.warn("{} Unable to Deleted File: {} from Service: {}",
                                    LOG_HEADER_SHORT, serviceFile.getName(), serviceName);
                        }
                    }
                }
                // Remove Files that we just deleted from out list ...
                for(ServiceFile serviceFile : serviceFilesDeleted) {
                    serviceStatus.getServiceFiles().removeIf(f -> f.getServiceFilePath().equals(serviceFile.getServiceFilePath()));
                }
            }
            logInfoMessageAndEvent(serviceName, String.format("%s Service Directory Successfully Cleaned for Service %s, Files Deleted: %d",
                    LOG_HEADER_SHORT, serviceName, filesDeleted));
            serviceStatus.setServiceDeploymentStatus(ServiceDeploymentStatus.CLEANED);
            return Optional.of(serviceStatus);
        }
        LOGGER.warn("{} Service Name: {}, Not Found!", LOG_HEADER_SHORT, serviceName);
        return Optional.of(contriveServiceStatusAfterDeployment(serviceName,
                "Named Service Not Found!",
                ServiceDeploymentStatus.FAILED_DEPLOYMENT));
    }

    /**
     * serviceFiles
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return Optional<List < ServiceFile>> - response providing contrived collection
     */
    @Override
    public Optional<List<ServiceFile>> serviceFiles(String serviceName) {
        return Optional.of(getNamedServiceDirectoryContents(serviceName));
    }

    /**
     * serviceUpload
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @param contents
     * @param properties
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    @Override
    public Optional<ServiceStatus> serviceUpload(String serviceName, ArtifactServiceContents contents, ArtifactServiceProperties properties) {
        if (!osEnvService.getRuntimeSpringBootDirectory().isPresent()) {
            LOGGER.error("{} Unable to obtain configured Base Runtime Spring Boot Deployment Directory? Have you set Property \"{}\"?",
                    LOG_HEADER_SHORT, Constants.DEPLOYMENT_MANAGER_SA_DIRECTORY_PROPERTY_NAME);
            LOGGER.error("{} Is the Default Directory Established: \"{}\"?",
                    LOG_HEADER_SHORT,
                    (osCommanderService.isOSWindows() ?
                            Constants.DEFAULT_SPRING_BOOT_SA_WIN_DIRECTORY :
                            Constants.DEFAULT_SPRING_BOOT_SA_LINUX_DIRECTORY)); //NOSONAR
            return Optional.empty();
        }
        // Reference the Base Deployment Area Directory
        File baseDir = osEnvService.getRuntimeSpringBootDirectory().get();
        // Begin Process for Uploading a new Artifact
        LOGGER.info("{} Upload of Service: {} Starting ...", LOG_HEADER_SHORT, serviceName);
        // Get the ServiceStatus ...
        Optional<ServiceStatus> serviceStatusResult = serviceStatus(serviceName);
        ServiceStatus serviceStatus = null;
        if (serviceStatusResult.isPresent()) {
            serviceStatus = serviceStatusResult.get();
        } else {
            // We have a new Service to Deploy, so set up for Initial Deployment ...
            File newServiceDir = new File(baseDir.getAbsolutePath() + File.separator + serviceName.trim());
            try {
                osEnvService.createFileDirectoryPath(newServiceDir);
            } catch (IOException ioe) {
                LOGGER.error("{} Exception Occurred while creating new service Directory path: {} -- {}",
                        LOG_HEADER_SHORT, newServiceDir.getAbsolutePath(), ioe.getMessage(), ioe);
                return Optional.empty();
            }
            // Ok, now get the new Service Status Object.
            serviceStatusResult = serviceStatus(serviceName);
            if (serviceStatusResult.isPresent()) {
                serviceStatus = serviceStatusResult.get();
                serviceStatus.setServiceDeploymentStatus(ServiceDeploymentStatus.NEW);
            } else {
                LOGGER.error("{} Issue occurred while creating new service Directory path: {}",
                        LOG_HEADER_SHORT, newServiceDir.getAbsolutePath());
                return Optional.empty();
            }
        }
        ArtifactUtil.saveArtifact(serviceStatus, contents, properties);
        // Ok, now get the Service Status Object.
        serviceStatusResult = serviceStatus(serviceName);
        if (serviceStatusResult.isPresent()) {
            serviceStatus = serviceStatusResult.get();
            serviceStatus.setServiceDeploymentStatus(ServiceDeploymentStatus.UPLOADED);
            logInfoMessageAndEvent(serviceName, String.format("%s Artifact Successfully Uploaded for Service: %s, File: %s",
                    LOG_HEADER_SHORT, serviceName, contents.getOriginalFilename()));
            serviceStatus.setServiceDeploymentStatus(ServiceDeploymentStatus.UPLOADED);
            return Optional.of(serviceStatus);
        } else {
            LOGGER.error("{} Issue occurred while uploading new service Artifact for Service: {}",
                    LOG_HEADER_SHORT, serviceName);
        }
        return Optional.empty();
    }

    /**
     * serviceDownload
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @param filename    -- filename to be downloaded
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    @Override
    public Optional<InputStream> serviceDownload(String serviceName, String filename) {
        LOGGER.info("{} Serving Service: {} File: {}", LOG_HEADER_SHORT, serviceName, filename);
        Optional<ServiceStatus> serviceStatusResult = serviceStatus(serviceName);
        if (serviceStatusResult.isPresent()) {
            ServiceStatus serviceStatus = serviceStatusResult.get();
            File file = new File(serviceStatus.getServiceDirectoryPath() + File.separator + filename);
            InputStream artifactInputStream = ArtifactUtil.readAnyServiceArtifact(file);
            if (artifactInputStream != null) {
                return Optional.of(artifactInputStream);
            }
            LOGGER.warn("{} Unable to Serve Service: {} File: {} for download!", LOG_HEADER_SHORT, serviceName, filename);
        }
        return Optional.empty();
    }

    /**
     * obtainBaseServiceDirectory
     *
     * @param serviceName -- Service whose Deployment Directory is to be established.
     * @return Optional<File> - Reference of Service Deployment Directory.
     */
    private Optional<File> obtainBaseServiceDirectory(String serviceName) {
        if (osEnvService.getRuntimeSpringBootDirectory().isPresent()) {
            File sbdmDir = new File(osEnvService.getRuntimeSpringBootDirectory().get().getAbsolutePath() + File.separator + serviceName); //NOSONAR
            if (sbdmDir.exists() && sbdmDir.isDirectory() && sbdmDir.canRead() && sbdmDir.canWrite()) {
                return Optional.of(sbdmDir);
            }
        }
        return Optional.empty();
    }

    /**
     * serviceAction
     *
     * @param serviceName -- Name of Service or Application to perform action against
     * @return Optional<ServiceStatus> - response providing detail Status
     */
    private Optional<?> serviceAction(final String serviceName, final String serviceAction) {
        Optional<ServiceStatus> serviceStatusResult = getServiceStatusObject(serviceName);
        if (serviceStatusResult.isPresent()) {
            ServiceStatus serviceStatus = serviceStatusResult.get();

            if ((serviceAction == null || !Arrays.asList(ACTIONS).contains(serviceAction)) ||
                    (Arrays.asList(STATUS_ONLY_SERVICES).contains(serviceName)
                            && (!Arrays.asList(READ_ONLY_ACTIONS).contains(serviceAction)))) {
                LOGGER.error("{} Specified Service Control Action: {} for Service: {} -- Is Invalid!",
                        LOG_HEADER_SHORT, serviceAction, serviceName);
                return Optional.empty();
            }

            String realServiceName = serviceName;
            if (serviceName.equalsIgnoreCase(DEPLOYMENT_MANAGER_EUREKA_DIRECTORY)) {
                realServiceName = DEPLOYMENT_MANAGER_EUREKA_SERVICE_NAME;
            }

            List<String> params = new ArrayList<>();
            if (osCommanderService.isOSWindows()) {
                params.add("cmd.exe");
                params.add("/c");
                params.add("echo");
                params.add(serviceAction);
                params.add(serviceName);

            } else {
                if (!serviceAction.equalsIgnoreCase(ACTION_STATUS)) {
                    params.add("sudo"); // Use 'sudo' so our command does not need a password.
                }
                if (serviceAction.equalsIgnoreCase(ACTION_JOURNAL)) {
                    params.add("journalctl");
                    params.add("--lines=1000");
                    params.add("--reverse");
                    params.add("--no-pager");
                    params.add("--output=short-precise");
                    params.add("-u");
                } else {
                    params.add("systemctl");
                    params.add(serviceAction);
                }
                params.add(realServiceName);
                if (serviceAction.equalsIgnoreCase(ACTION_STATUS)) {
                    params.add("--no-pager");
                    params.add("--full"); // Provide Full Data for Log Lines
                }
            }

            if (serviceAction.equalsIgnoreCase(ACTION_STATUS)) {
                Optional<String> osResponse = osCommanderService.runOSCommandWithOutput(params,
                        (osCommanderService.getHomeDirectory()).isPresent() ? osCommanderService.getHomeDirectory().get() : null); //NOSONAR
                if (osResponse.isPresent()) {
                    serviceStatus.setOsTransientCommandResponse(
                            ServiceStatusFormatUtil.formatServiceStatusResponse(true, osResponse.get()));
                } else {
                    serviceStatus.setOsTransientCommandResponse("");
                }
                return Optional.of(serviceStatus);
            } else if (serviceAction.equalsIgnoreCase(ACTION_JOURNAL) || serviceAction.equalsIgnoreCase(ACTION_SHOW)) {
                return osCommanderService.runOSCommandWithOutput(params,
                        (osCommanderService.getHomeDirectory()).isPresent() ? osCommanderService.getHomeDirectory().get() : null); //NOSONAR
            } else {
                boolean osResponse = osCommanderService.runOSCommandWithNoOutput(params);
                LOGGER.info("{} OS Command Ran: {}", LOG_HEADER_SHORT, osResponse);
                return Optional.of(serviceStatus);
            }
        } else {
            LOGGER.warn("{} No Service Named: {} Available to perform a '{}}' Command!",
                    LOG_HEADER_SHORT, serviceName, serviceAction);
        }
        return Optional.empty();
    }

    /**
     * getServiceStatusObject
     * Private Helper Method to obtain our Service Status Object
     *
     * @param serviceName Name of the Service to find ...
     * @return Optional<ServiceStatus> - contrived status POJO
     */
    private Optional<ServiceStatus> getServiceStatusObject(String serviceName) {
        Optional<File> result = obtainBaseServiceDirectory(serviceName);
        if (result.isPresent()) {
            File file = result.get();
            if (file.isDirectory() && !Arrays.stream(Constants.DEPLOYMENT_DIRECTORIES_TO_BE_IGNORED).anyMatch(file.getName()::equals)) {
                ServiceStatus serviceStatus =
                        new ServiceStatus(file.getName(), file.getAbsolutePath(), ServiceDeploymentStatus.CHECKING,
                                null, new ArrayList<>(), null, null);
                return Optional.of(serviceStatus);
            }
        }
        return Optional.empty();
    }

    /**
     * logInfoMessageAndEvent
     *
     * @param message -- To be Log and Persisted ...
     */
    private void logInfoMessageAndEvent(String message) {
        LOGGER.info("{}", message);
        persistEventMessage(DM_SELF, message);
    }

    /**
     * logInfoMessageAndEvent
     *
     * @param message -- To be Log and Persisted ...
     */
    private void logInfoMessageAndEvent(String serviceName, String message) {
        LOGGER.info("{}", message);
        persistEventMessage(serviceName, message);
    }

    /**
     * persistEventMessage
     *
     * @param serviceName    -- Name of Service or "" for Deployment Manager.
     * @param serviceMessage -- Message
     */
    private void persistEventMessage(String serviceName, String serviceMessage) {
        DeploymentManagerEvent deploymentManagerEvent = new DeploymentManagerEvent();
        deploymentManagerEvent.setServiceName(serviceName);
        deploymentManagerEvent.setEventMessage(serviceMessage);
        deploymentManagerEvent.setEventDate(new Date());
        deploymentManagerEventRepository.save(deploymentManagerEvent);
    }

    /**
     * removeServiceFile
     *
     * @param filePath - to be removed from file storage.
     * @return boolean - indicating file deleted or not ...
     */
    private boolean removeServiceFile(String filePath) {
        File serviceFile = new File(filePath);
        if (serviceFile.exists()) {
            LOGGER.info("{} Attempting to purged temporary file: {}", LOG_HEADER_SHORT, serviceFile.getAbsolutePath());
            if (serviceFile.delete()) {
                LOGGER.info("{}  Deletion of temporary file: {}, Successful", LOG_HEADER_SHORT, serviceFile.getAbsolutePath());
                return true;
            } else {
                LOGGER.info("{}  Unable to Delete temporary file: {}, possible permission issue!", LOG_HEADER_SHORT, serviceFile.getAbsolutePath());
            }
        }
        return false;
    }

    /**
     * Private Helper method to obtain Directory Contents for a Named Service.
     *
     * @param serviceName - Service Name to Find.
     * @return List<ServiceFile> - containing Contrived Data.
     */
    private List<ServiceFile> getNamedServiceDirectoryContents(String serviceName) {
        List<ServiceFile> listOfServiceFiles = new ArrayList<>();
        /**
         * Obtain the top level directory path of existing deployment services names...
         */
        File sbdir = osEnvService.getRuntimeSpringBootDirectory().get();  //NOSONAR
        if (!sbdir.exists()) {
            LOGGER.warn("{} Top Level Deployment Manager Directory: '{}', does not exist!",
                    LOG_HEADER_SHORT, sbdir.getAbsolutePath());
            return listOfServiceFiles;
        }
        File serviceDir = new File(sbdir.getAbsoluteFile() + File.separator + serviceName);
        if (!serviceDir.exists()) {
            LOGGER.warn("{} Service Directory: '{}', does not exist!", LOG_HEADER_SHORT, serviceDir.getAbsolutePath());
            return listOfServiceFiles;
        }
        File[] files = serviceDir.listFiles(); //NOSONAR
        if (files != null) {
            for (File file : files) {
                if (file.getName().equalsIgnoreCase(".") || file.getName().equalsIgnoreCase("..")) {
                    continue; // Ignore these references ...
                }
                // Instantiate our Service File ...
                ServiceFile serviceFile = new ServiceFile(serviceName,
                        file.getAbsolutePath(),
                        convertMillisecondsToDate(file.lastModified()),
                        file.length(),
                        MvcUriComponentsBuilder
                                .fromMethodName(DeploymentManagerUIController.class,
                                        "serveFile", serviceName, file.toPath().getFileName().toString())
                                .build()
                                .toString(),
                        file.toPath().getFileName().toString());
                listOfServiceFiles.add(serviceFile);
            }
        }
        return listOfServiceFiles;
    }

    /**
     * Private Helper to convert Date in MilliSeconds to Actual Localized Date and Time.
     *
     * @param milliSeconds - Time in milliseconds
     * @return LocalDateTime - contrived from Milliseconds.
     */
    private LocalDateTime convertMillisecondsToDate(long milliSeconds) {
        return
                LocalDateTime.ofInstant(Instant.ofEpochMilli(milliSeconds),
                        TimeZone.getDefault().toZoneId());
    }

}
