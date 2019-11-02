package jeffaschenk.infra.sbdm.controller;

import jeffaschenk.infra.sbdm.entity.DeploymentManagerEvent;
import jeffaschenk.infra.sbdm.exceptions.DeploymentException;
import jeffaschenk.infra.sbdm.metrics.AppInfoContributor;
import jeffaschenk.infra.sbdm.model.ArtifactServiceContents;
import jeffaschenk.infra.sbdm.model.ArtifactServiceProperties;
import jeffaschenk.infra.sbdm.model.ServiceDeploymentStatus;
import jeffaschenk.infra.sbdm.model.ServiceStatus;
import jeffaschenk.infra.sbdm.repository.DeploymentManagerEventRepository;

import jeffaschenk.infra.sbdm.services.DeploymentManagerService;
import jeffaschenk.infra.sbdm.util.ArtifactMediaType;
import jeffaschenk.infra.sbdm.util.ArtifactUtil;
import jeffaschenk.infra.sbdm.util.NetUtil;
import org.apache.tika.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static jeffaschenk.infra.sbdm.common.Constants.LOG_HEADER_SHORT;

@Controller
public class DeploymentManagerUIController {
    /**
     * Common Logger
     */
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(DeploymentManagerUIController.class);

    /**
     * Application Information Contributor Component
     */
    @Autowired
    private AppInfoContributor appInfoContributor;

    /**
     * Constants
     */
    private static final String MODEL_SERVICE_NAME = "serviceName";
    private static final String MODEL_EVENTS = "events";
    private static final String MODEL_STATUS = "status";
    private static final String MODEL_JOURNAL = "journal";
    private static final String MODEL_CONFIG = "configuration";
    private static final String MODEL_MESSAGE = "message";

    private static final String ACTION_RESPONSE_SERVICE_STATUS = "service-status";
    private static final String ACTION_RESPONSE_SERVICE_UPLOAD = "service-upload";
    private static final String ACTION_RESPONSE_SERVICE_EVENTS = "events";
    private static final String ACTION_RESPONSE_SERVICE_JOURNAL = "journal";
    private static final String ACTION_RESPONSE_SERVICE_CONFIG = "configuration";

    /**
     * Service Layer
     */
    @Autowired
    @Qualifier("deploymentManagerService")
    private DeploymentManagerService deploymentManagerService;

    @Autowired
    private DeploymentManagerEventRepository deploymentManagerEventRepository;

    /**
     * Runtime Host Information
     */
    private static String hostInfo;
    private static String hostInfoShort;

    /**
     * Controller Initialization
     */
    @PostConstruct
    private void initialization() {
        LOGGER.info("{} Initializing ... Acquiring Host Information vor View Model ...", LOG_HEADER_SHORT);
        hostInfo = NetUtil.getHostInfo();
        hostInfoShort = NetUtil.getShortHostInfo();
    }

    /**
     * Private Helper to Add Host Info to View Model
     *
     * @param model - reference of view model
     */
    private void addHostInfo(Model model) {
        model.addAttribute("hostInfoLong", hostInfo);
        model.addAttribute("hostInfo", hostInfoShort);
    }

    /**
     * Private Helper to Add Message to View Model
     *
     * @param model - reference of view model
     */
    private void addMessage(String message, Model model) {
        model.addAttribute(MODEL_MESSAGE, message);
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Controller Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @GetMapping("/")
    public String showAllServices(Model model) {
        Optional<List<ServiceStatus>> result = deploymentManagerService.availableServices();
        if (result.isPresent()) {
            model.addAttribute("services", result.get());
        }
        addHostInfo(model);
        return "index";
    }

    @GetMapping("/events")
    public String showAllEvents(Model model) {
        List<DeploymentManagerEvent> events =
                deploymentManagerEventRepository.findAll(Sort.by(Sort.Direction.DESC, "eventDate"));
        model.addAttribute(MODEL_EVENTS, events);
        addHostInfo(model);
        return ACTION_RESPONSE_SERVICE_EVENTS;
    }

    @GetMapping("/status/{serviceName}")
    public String showServiceStatus(@PathVariable(MODEL_SERVICE_NAME) String serviceName, Model model) {
        model.addAttribute(MODEL_SERVICE_NAME, serviceName);
        model.addAttribute(MODEL_STATUS, deploymentManagerService.serviceStatus(serviceName));
        addHostInfo(model);
        if (model.containsAttribute(MODEL_MESSAGE)) {
            model.addAttribute(MODEL_MESSAGE, null);
        }
        return ACTION_RESPONSE_SERVICE_STATUS;
    }

    @GetMapping("/stop/{serviceName}")
    public String showServiceStopStatus(@PathVariable(MODEL_SERVICE_NAME) String serviceName, Model model) {
        model.addAttribute(MODEL_SERVICE_NAME, serviceName);
        model.addAttribute(MODEL_STATUS, deploymentManagerService.serviceStop(serviceName));
        model.addAttribute("previousActionStatus", ServiceDeploymentStatus.STOPPED);
        addHostInfo(model);
        return ACTION_RESPONSE_SERVICE_STATUS;
    }

    @GetMapping("/start/{serviceName}")
    public String showServiceStartStatus(@PathVariable(MODEL_SERVICE_NAME) String serviceName, Model model) {
        model.addAttribute(MODEL_SERVICE_NAME, serviceName);
        model.addAttribute(MODEL_STATUS, deploymentManagerService.serviceStart(serviceName));
        model.addAttribute("previousActionStatus", ServiceDeploymentStatus.STARTED);
        addHostInfo(model);
        return ACTION_RESPONSE_SERVICE_STATUS;
    }

    @GetMapping("/deploy/{serviceName}")
    public String showServiceDeployView(@PathVariable(MODEL_SERVICE_NAME) String serviceName, Model model) {
        model.addAttribute(MODEL_SERVICE_NAME, serviceName);
        Optional<ServiceStatus> serviceStatusResult = deploymentManagerService.serviceStatus(serviceName);
        if (!serviceStatusResult.isPresent()) {
            model.addAttribute(MODEL_MESSAGE, "No Service Named: "+serviceName+", available for Deployment!");
            addHostInfo(model);
            return ACTION_RESPONSE_SERVICE_STATUS;
        }
        /**
         * We have our current Service Status, now perform deployment.
         */
        try {
            if(deploymentManagerService.serviceDeploy(serviceStatusResult.get())) {
                model.addAttribute("previousActionStatus", serviceStatusResult.get().getServiceDeploymentStatus());
                model.addAttribute(MODEL_MESSAGE, serviceStatusResult.get().getDeploymentMessageStatus());
            } else {
                throw new DeploymentException("Service Deployment was not Successful!");
            }
        } catch(DeploymentException de) {
            model.addAttribute(MODEL_MESSAGE, de.getMessage());
        }
        model.addAttribute(MODEL_STATUS, deploymentManagerService.serviceStatus(serviceName)); // Get Latest Status...
        addHostInfo(model);
        return ACTION_RESPONSE_SERVICE_STATUS;
    }

    @GetMapping("/clean/{serviceName}")
    public String showServiceCleanStatus(@PathVariable(MODEL_SERVICE_NAME) String serviceName, Model model) {
        model.addAttribute(MODEL_SERVICE_NAME, serviceName);
        model.addAttribute(MODEL_STATUS, deploymentManagerService.serviceCleanUp(serviceName));
        model.addAttribute("previousActionStatus", ServiceDeploymentStatus.CLEANED);
        addHostInfo(model);
        return ACTION_RESPONSE_SERVICE_STATUS;
    }

    @GetMapping("/upload/{serviceName}")
    public String showServiceUploadView(@PathVariable(MODEL_SERVICE_NAME) String serviceName, final Model model) {
        model.addAttribute(MODEL_SERVICE_NAME, serviceName);
        model.addAttribute(MODEL_STATUS, deploymentManagerService.serviceStatus(serviceName));
        model.addAttribute("uploadActionPath", "/deploymentManager/upload/" + serviceName);
        addHostInfo(model);
        return ACTION_RESPONSE_SERVICE_UPLOAD;
    }

    @PostMapping("/upload/{serviceName}")
    public String performServiceUpload(@PathVariable(MODEL_SERVICE_NAME) String serviceName,
                                       @RequestPart("file") MultipartFile file,
                                       final Model model) {

        LOGGER.info("{} Processing Upload for service: {}", LOG_HEADER_SHORT, serviceName);
        model.addAttribute(MODEL_SERVICE_NAME, serviceName);
        addHostInfo(model);

        /**
        if (result.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            LOGGER.warn("{} Upload had issues:", LOG_HEADER_SHORT);
            for (ObjectError objectError : result.getAllErrors()) {
                LOGGER.warn("{}  {}", LOG_HEADER_SHORT, objectError);
                sb.append(objectError.toString()).append(System.lineSeparator());
            }
            model.addAttribute(MODEL_MESSAGE, sb.toString());
            return ACTION_RESPONSE_SERVICE_UPLOAD;
        }
         **/

        /**
         * Perform Upload
         */
        ArtifactServiceProperties artifactServiceProperties = new ArtifactServiceProperties();
        artifactServiceProperties.setServiceName(serviceName);
        model.addAttribute("previousActionStatus", ServiceDeploymentStatus.UPLOADED);
        /**
         * Validate File
         */
        if (file == null) {
            String message = String.format("Attempted to perform a Service Upload for: %s, however, No Artifact provided to Upload!",
                    serviceName);
            LOGGER.warn(message);
            model.addAttribute(MODEL_MESSAGE, message);
            return ACTION_RESPONSE_SERVICE_UPLOAD;
        } else if (file.isEmpty()) {
            String message = String.format("Attempted to perform a Service Deployment for: %s, however, File provided has no Content!",
                    serviceName);
            LOGGER.warn(message);
            model.addAttribute(MODEL_MESSAGE, message);
            return ACTION_RESPONSE_SERVICE_UPLOAD;
        } else {
            LOGGER.info("{} Will Attempted to perform a Service Upload for Service: {}, Artifact File Length:{}",
                    LOG_HEADER_SHORT, serviceName, file.getSize());
            try {
                /**
                 * First Clean-Up the Service Named Directory Contents for eventual upload ...
                 */
                deploymentManagerService.serviceCleanUp(serviceName);
                /**
                 * Prepare Artifact Contents
                 */
                ArtifactServiceContents artifactServiceContents = new ArtifactServiceContents(file,
                        ArtifactMediaType.getMediaTypeFromOriginalFilename(file.getOriginalFilename()));
                /**
                 * Perform the Deployment ...
                 */
                Optional<ServiceStatus> deploymentResult =
                        deploymentManagerService.serviceUpload(serviceName, artifactServiceContents, artifactServiceProperties);
                if (deploymentResult.isPresent()) {
                    String message = String.format("Accepted Service Upload for: %s File: %s",
                            serviceName, artifactServiceContents.getOriginalFilename());
                    LOGGER.info(message);
                    model.addAttribute(MODEL_MESSAGE, message);
                    return ACTION_RESPONSE_SERVICE_UPLOAD;
                } else {
                    String message = String.format("Error performing Service Upload for: %s, no Upload Status returned!", serviceName);
                    LOGGER.info(message);
                    model.addAttribute(MODEL_MESSAGE, message);
                    return ACTION_RESPONSE_SERVICE_UPLOAD;
                }
            } catch (IllegalStateException ise) {
                String message = String.format("Attempted to perform a Service Upload for: %s, however, exception raised: %s",
                        serviceName, ise.getMessage());
                LOGGER.error(message);
                model.addAttribute(MODEL_MESSAGE, message);
                return ACTION_RESPONSE_SERVICE_UPLOAD;
            } catch (IOException ioe) {
                String message = String.format("Attempted to perform a Service Upload for: %s, however, exception raised: %s",
                        serviceName, ioe.getMessage());
                LOGGER.error(message);
                LOGGER.error(ioe.getMessage(), ioe);
                model.addAttribute(MODEL_MESSAGE, message);
                return ACTION_RESPONSE_SERVICE_UPLOAD;
            }
        }
    }

    @GetMapping("/download/{serviceName}/{filename:.+}") // This annotation allows for additional dots in filename ...
    @ResponseBody
    public ResponseEntity<?> serveFile(@PathVariable String serviceName, @PathVariable String filename) {
        LOGGER.info("{} Serving Service: {} File: {}", LOG_HEADER_SHORT, serviceName, filename);
        Optional<ServiceStatus> serviceStatusResult = deploymentManagerService.serviceStatus(serviceName);
        if (serviceStatusResult.isPresent()) {
            ServiceStatus serviceStatus = serviceStatusResult.get();
            File file = new File(serviceStatus.getServiceDirectoryPath()+File.separator+filename);
            InputStream artifactInputStream = ArtifactUtil.readAnyServiceArtifact(file);
            if (artifactInputStream != null) {
                try {
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                            .body(IOUtils.toByteArray(artifactInputStream));
                } catch(IOException ioe) {
                    LOGGER.error("{} IO Exception encountered: {}", LOG_HEADER_SHORT, ioe.getMessage(), ioe);
                }
            }
            LOGGER.warn("{} Unable to Serve Service: {} File: {} for download!", LOG_HEADER_SHORT, serviceName, filename);
            return ResponseEntity.notFound().header(HttpHeaders.CONTENT_LENGTH, "0").build();
        } else {
            return ResponseEntity.notFound().header(HttpHeaders.CONTENT_LENGTH, "0").build();
        }
    }

    @GetMapping("/events/{serviceName}")
    public String showEventsForService(@PathVariable(MODEL_SERVICE_NAME) String serviceName, Model model) {
        List<DeploymentManagerEvent> events =
                deploymentManagerEventRepository.findByServiceName(serviceName,
                        Sort.by(Sort.Direction.DESC, "eventDate"));
        model.addAttribute(MODEL_EVENTS, events);

        addHostInfo(model);
        model.addAttribute(MODEL_SERVICE_NAME, serviceName);
        return ACTION_RESPONSE_SERVICE_EVENTS;
    }

    @GetMapping("/configuration/{serviceName}")
    public String showServiceConfiguration(@PathVariable(MODEL_SERVICE_NAME) String serviceName, Model model) {
        model.addAttribute(MODEL_SERVICE_NAME, serviceName);
        model.addAttribute(MODEL_CONFIG, deploymentManagerService.serviceConfiguration(serviceName));
        addHostInfo(model);
        if (model.containsAttribute(MODEL_MESSAGE)) {
            model.addAttribute(MODEL_MESSAGE, null);
        }
        return ACTION_RESPONSE_SERVICE_CONFIG;
    }

    /**
    @GetMapping("/journal/{serviceName}")
    public String showServiceJournal(@PathVariable(MODEL_SERVICE_NAME) String serviceName, Model model) {
        model.addAttribute(MODEL_SERVICE_NAME, serviceName);
        model.addAttribute(MODEL_JOURNAL, deploymentManagerService.serviceJournal(serviceName));
        addHostInfo(model);
        if (model.containsAttribute(MODEL_MESSAGE)) {
            model.addAttribute(MODEL_MESSAGE, null);
        }
        return ACTION_RESPONSE_SERVICE_JOURNAL;
    }
     **/

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleMethodArgumentNotValidException( MethodArgumentNotValidException error ) {
        LOGGER.warn("{}  {}", LOG_HEADER_SHORT, error.getMessage());
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

}
