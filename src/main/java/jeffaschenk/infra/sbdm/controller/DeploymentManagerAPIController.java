package jeffaschenk.infra.sbdm.controller;

import jeffaschenk.infra.sbdm.exceptions.DeploymentException;
import jeffaschenk.infra.sbdm.metrics.AppInfoContributor;
import jeffaschenk.infra.sbdm.model.ArtifactServiceContents;
import jeffaschenk.infra.sbdm.model.ArtifactServiceProperties;
import jeffaschenk.infra.sbdm.model.ServiceStatus;
import jeffaschenk.infra.sbdm.response.CommonResponseResource;
import jeffaschenk.infra.sbdm.services.DeploymentManagerService;
import jeffaschenk.infra.sbdm.util.ArtifactMediaType;
import jeffaschenk.infra.sbdm.util.DTOParser;
import jeffaschenk.infra.sbdm.util.DateUtil;
import org.joda.time.DateTime;
import org.json.JsonData;
import org.json.JsonStruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static jeffaschenk.infra.sbdm.common.Constants.LOG_HEADER_SHORT;
import static jeffaschenk.infra.sbdm.util.RequestUtil.clearRequestInfo;
import static jeffaschenk.infra.sbdm.util.RequestUtil.setRequestInfo;

/**
 * DeploymentManagerAPIController
 *
 * @author jaschenk
 */
@RestController
@RequestMapping("/api")
public class DeploymentManagerAPIController {
    /**
     * Common Logger
     */
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(DeploymentManagerAPIController.class);

    /**
     * Constants
     */
    private static final String APPLICATION_JSON_WITH_UTF8_ENCODING_VALUE = "application/json;charset=UTF-8";
    /**
     * Default Media Type for Not Found Assets.
     */
    protected static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";

    /**
     * Application Information Contributor Component
     */
    @Autowired
    private AppInfoContributor appInfoContributor;

    /**
     * Service Layer
     */
    @Autowired
    @Qualifier("deploymentManagerService")
    private DeploymentManagerService deploymentManagerService;

    /**
     * status for all available services defined...
     *
     * @param request - reference to HTTP Servlet Request
     * @return ResponseEntity<CommonResponseObject> - reference
     */
    @GetMapping(value = "/status", produces = APPLICATION_JSON_WITH_UTF8_ENCODING_VALUE)
    public ResponseEntity<CommonResponseResource> statusAll(HttpServletRequest request) {
        appInfoContributor.incrementStatusMetric(); // Increment Request Count.
        DateTime startTime = DateTime.now();
        /**
         * Set our ThreadLocal Request Information Wrapper.
         */
        setRequestInfo(request);
        /**
         * Log incoming Request ...
         */
        LOGGER.info("Performing Status for All Available Deployed Services ... ");
        Optional<List<ServiceStatus>> result = deploymentManagerService.availableServices();
        JsonStruct jsonStruct = processAvailableServicesResponse(result);
        /**
         * Return Service Availability Response
         */
        clearRequestInfo(); // Clear ThreadLocal Data...
        /**
         * Finalize
         */
        LOGGER.info("Status Request Duration: {}", DateUtil.getDuration(startTime, DateTime.now()));
        CommonResponseResource commonResponseResource = new CommonResponseResource(jsonStruct);
        return new ResponseEntity<>(commonResponseResource, HttpStatus.OK);
    }

    /**
     * status
     *
     * @param request - reference to HTTP Servlet Request
     * @return ResponseEntity<CommonResponseObject> - reference
     */
    @GetMapping(value = "/status/{serviceName}", produces = APPLICATION_JSON_WITH_UTF8_ENCODING_VALUE)
    public ResponseEntity<CommonResponseResource> status(@PathVariable String serviceName, HttpServletRequest request) {
        appInfoContributor.incrementStatusMetric(); // Increment Request Count.
        DateTime startTime = DateTime.now();
        /**
         * Set our ThreadLocal Request Information Wrapper.
         */
        setRequestInfo(request);
        /**
         * Log incoming Request ...
         */
        LOGGER.info("Performing Status for Service Name: {} ... ", serviceName);
        Optional<ServiceStatus> result = deploymentManagerService.serviceStatus(serviceName);
        JsonStruct jsonStruct = processServiceResponse(result);
        /**
         * Return Service Availability Response
         */
        clearRequestInfo(); // Clear ThreadLocal Data...
        /**
         * Finalize
         */
        LOGGER.info("Status Request Duration: {}", DateUtil.getDuration(startTime, DateTime.now()));
        CommonResponseResource commonResponseResource = new CommonResponseResource(jsonStruct);
        return new ResponseEntity<>(commonResponseResource, HttpStatus.OK);
    }

    /**
     * start
     *
     * @param request - reference to HTTP Servlet Request
     * @return ResponseEntity<CommonResponseObject> - reference
     */
    @PutMapping(value = "/start/{serviceName}", produces = APPLICATION_JSON_WITH_UTF8_ENCODING_VALUE)
    public ResponseEntity<CommonResponseResource> start(@PathVariable String serviceName, HttpServletRequest request) {
        appInfoContributor.incrementStartMetric(); // Increment Request Count.
        DateTime startTime = DateTime.now();
        /**
         * Set our ThreadLocal Request Information Wrapper.
         */
        setRequestInfo(request);
        /**
         * Log incoming Request ...
         */
        LOGGER.info("Performing Start for Service Name: {} ... ", serviceName);
        Optional<ServiceStatus> result = deploymentManagerService.serviceStart(serviceName);
        JsonStruct jsonStruct = processServiceResponse(result);

        /**
         * Return Service Availability Response
         */
        clearRequestInfo(); // Clear ThreadLocal Data...
        /**
         * Finalize
         */
        LOGGER.info("Start Request Duration: {}", DateUtil.getDuration(startTime, DateTime.now()));
        CommonResponseResource commonResponseResource = new CommonResponseResource(jsonStruct);
        return new ResponseEntity<>(commonResponseResource, HttpStatus.OK);
    }

    /**
     * stop
     *
     * @param request - reference to HTTP Servlet Request
     * @return ResponseEntity<CommonResponseObject> - reference
     */
    @PutMapping(value = "/stop/{serviceName}", produces = APPLICATION_JSON_WITH_UTF8_ENCODING_VALUE)
    public ResponseEntity<CommonResponseResource> stop(@PathVariable String serviceName, HttpServletRequest request) {
        appInfoContributor.incrementStopMetric(); // Increment Request Count.
        DateTime startTime = DateTime.now();
        /**
         * Set our ThreadLocal Request Information Wrapper.
         */
        setRequestInfo(request);
        /**
         * Log incoming Request ...
         */
        LOGGER.info("Performing Stop for Service Name: {} ... ", serviceName);
        Optional<ServiceStatus> result = deploymentManagerService.serviceStop(serviceName);
        JsonStruct jsonStruct = processServiceResponse(result);

        /**
         * Return Service Availability Response
         */
        clearRequestInfo(); // Clear ThreadLocal Data...
        /**
         * Finalize
         */
        LOGGER.info("Stop Request Duration: {}", DateUtil.getDuration(startTime, DateTime.now()));
        CommonResponseResource commonResponseResource = new CommonResponseResource(jsonStruct);
        return new ResponseEntity<>(commonResponseResource, HttpStatus.OK);
    }

    /**
     * clean
     *
     * @param request - reference to HTTP Servlet Request
     * @return ResponseEntity<CommonResponseObject> - reference
     */
    @DeleteMapping(value = "/clean/{serviceName}", produces = APPLICATION_JSON_WITH_UTF8_ENCODING_VALUE)
    public ResponseEntity<CommonResponseResource> cleanServiceDirectory(@PathVariable String serviceName, HttpServletRequest request) {
        appInfoContributor.incrementCleanServiceDirectoryMetric(); // Increment Request Count.
        DateTime startTime = DateTime.now();
        /**
         * Set our ThreadLocal Request Information Wrapper.
         */
        setRequestInfo(request);
        /**
         * Log incoming Request ...
         */
        LOGGER.info("Performing Clean up for Service Name: {} ... ", serviceName);
        Optional<ServiceStatus> result = deploymentManagerService.serviceCleanUp(serviceName);
        JsonStruct jsonStruct = processServiceResponse(result);
        /**
         * Return Service Availability Response
         */
        clearRequestInfo(); // Clear ThreadLocal Data...
        /**
         * Finalize
         */
        LOGGER.info("Clean up Request Duration: {}", DateUtil.getDuration(startTime, DateTime.now()));
        CommonResponseResource commonResponseResource = new CommonResponseResource(jsonStruct);
        return new ResponseEntity<>(commonResponseResource, HttpStatus.OK);
    }

    /**
     * deploy
     *
     * @param request - reference to HTTP Servlet Request
     * @return ResponseEntity<CommonResponseObject> - reference
     */
    @PostMapping(value = "/deploy/{serviceName}",
            produces = APPLICATION_JSON_WITH_UTF8_ENCODING_VALUE)
    public ResponseEntity<CommonResponseResource> deploy(@PathVariable String serviceName,
                                                         HttpServletRequest request) {
        appInfoContributor.incrementDeploymentMetric(); // Increment Request Count.
        DateTime startTime = DateTime.now();
        /**
         * Set our ThreadLocal Request Information Wrapper.
         */
        setRequestInfo(request);
        /**
         * Log incoming Request ...
         */
        LOGGER.info("Performing Deploy for Service Name: {} ... ", serviceName);
        /**
         * Set up the Response Object ...
         */
        JsonStruct jsonStruct = new JsonStruct();
        HttpStatus rc;

        LOGGER.info("{} Performing a Deployment for Service: {} using previously upload '.new' designation Files.",
                LOG_HEADER_SHORT, serviceName);
        try {
            /**
             * Perform the Deployment ...
             */
            Optional<ServiceStatus> serviceStatusResult = deploymentManagerService.serviceStatus(serviceName);
            if (!serviceStatusResult.isPresent()) {
                jsonStruct.setStatusToFail();
                String message = String.format("No Service Named: %s, available for Deployment!", serviceName);
                jsonStruct.addMessage(message);
                LOGGER.info(message);
                rc = HttpStatus.BAD_REQUEST;
            } else {
                /**
                 * We have our current Service Status, now perform deployment.
                 */
                if (deploymentManagerService.serviceDeploy(serviceStatusResult.get())) {
                    jsonStruct.setStatusToSuccess();
                    String message = String.format("Accepted Service Deployment for: %s", serviceName);
                    jsonStruct.addMessage(message);
                    LOGGER.info(message);
                    rc = HttpStatus.ACCEPTED; // 202 : Accepted.
                } else {
                    jsonStruct.setStatusToFail();
                    String message = String.format("Error performing Service Deployment for: %s, no Deployment Status returned!", serviceName);
                    jsonStruct.addMessage(message);
                    LOGGER.info(message);
                    rc = HttpStatus.BAD_REQUEST;
                }
            }
        } catch (Exception e) {
            String message = String.format("Attempted to perform a Service Deployment for: %s, however, exception raised: %s",
                    serviceName, e.getMessage());
            jsonStruct.addMessage(message);
            jsonStruct.setStatusToError();
            jsonStruct.setConditionCode("Exception");
            LOGGER.error(message);
            LOGGER.error(e.getMessage(), e);
            rc = HttpStatus.BAD_REQUEST;
        }
        /**
         * Finalize
         */
        clearRequestInfo(); // Clear ThreadLocal Data...
        LOGGER.info("Deploy Request Duration: {}", DateUtil.getDuration(startTime, DateTime.now()));
        CommonResponseResource commonResponseResource = new CommonResponseResource(jsonStruct);
        return new ResponseEntity<>(commonResponseResource, rc);
    }

    /**
     * upload
     *
     * @param request - reference to HTTP Servlet Request
     * @return ResponseEntity<CommonResponseObject> - reference
     */
    @PostMapping(value = "/upload/{serviceName}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = APPLICATION_JSON_WITH_UTF8_ENCODING_VALUE)
    public ResponseEntity<CommonResponseResource> upload(@PathVariable String serviceName,
                                                         @RequestParam(value = "uploadProperties") String uploadProperties,
                                                         @RequestParam(value = "file") MultipartFile file,
                                                         HttpServletRequest request) {
        appInfoContributor.incrementUploadMetric(); // Increment Request Count.
        DateTime startTime = DateTime.now();
        /**
         * Set our ThreadLocal Request Information Wrapper.
         */
        setRequestInfo(request);
        /**
         * Log incoming Request ...
         */
        LOGGER.info("Performing Upload for Service Name: {} ... ", serviceName);

        /**
         * Set up the Response Object ...
         */
        JsonStruct jsonStruct = new JsonStruct();
        HttpStatus rc;
        /**
         * Parse Our Artifact Properties
         */
        Optional<ArtifactServiceProperties> resultFromMapper = DTOParser.getArtifactServicePropertiesObjectFromString(uploadProperties);
        if (!resultFromMapper.isPresent()) {
            String message = String.format("Attempting to perform a Service Upload for: %s, however, no Artifact Properties specified!",
                    serviceName);
            jsonStruct.addMessage(message);
            jsonStruct.setStatusToError();
            jsonStruct.setConditionCode("No Artifact Data");
            LOGGER.warn(message);
            rc = HttpStatus.BAD_REQUEST;
        } else {
            ArtifactServiceProperties artifactServiceProperties = resultFromMapper.get();
            /**
             * Validate File
             */
            if (file == null) {
                String message = String.format("Attempted to perform a Service Upload for: %s, however, No Artifact provided to Upload!",
                        serviceName);
                jsonStruct.addMessage(message);
                jsonStruct.setStatusToError();
                jsonStruct.setConditionCode("No File");
                LOGGER.warn(message);
                rc = HttpStatus.BAD_REQUEST;
            } else if (file.isEmpty()) {
                String message = String.format("Attempted to perform a Service Deployment for: %s, however, File provided has no Content!",
                        serviceName);
                jsonStruct.addMessage(message);
                jsonStruct.setStatusToError();
                jsonStruct.setConditionCode("File has no Content");
                LOGGER.warn(message);
                rc = HttpStatus.BAD_REQUEST;
            } else {
                LOGGER.info("{} Will Attempted to perform a Service Upload for Service: {}, Artifact File Length:{}",
                        LOG_HEADER_SHORT, serviceName, file.getSize());
                try {
                    ArtifactServiceContents artifactServiceContents = new ArtifactServiceContents(file,
                            ArtifactMediaType.getMediaTypeFromOriginalFilename(file.getOriginalFilename()));
                    /**
                     * Perform the Upload of Artifact ...
                     */
                    Optional<ServiceStatus> deploymentResult =
                            deploymentManagerService.serviceUpload(serviceName, artifactServiceContents, artifactServiceProperties);
                    if (deploymentResult.isPresent()) {
                        jsonStruct.setStatusToSuccess();
                        String message = String.format("Accepted Service Upload for: %s", serviceName);
                        jsonStruct.addMessage(message);
                        LOGGER.info(message);
                        rc = HttpStatus.ACCEPTED; // 202 : Accepted.
                    } else {
                        jsonStruct.setStatusToFail();
                        String message = String.format("Error performing Service Upload for: %s, no Upload Status returned!", serviceName);
                        jsonStruct.addMessage(message);
                        LOGGER.info(message);
                        rc = HttpStatus.BAD_REQUEST;
                    }
                } catch (IOException ioe) {
                    String message = String.format("Attempted to perform a Service Upload for: %s, however, exception raised: %s",
                            serviceName, ioe.getMessage());
                    jsonStruct.addMessage(message);
                    jsonStruct.setStatusToError();
                    jsonStruct.setConditionCode("Exception");
                    LOGGER.error(message);
                    LOGGER.error(ioe.getMessage(), ioe);
                    rc = HttpStatus.BAD_REQUEST;
                }
            }
        }
        /**
         * Finalize
         */
        clearRequestInfo(); // Clear ThreadLocal Data...
        LOGGER.info("Deploy Request Duration: {}", DateUtil.getDuration(startTime, DateTime.now()));
        CommonResponseResource commonResponseResource = new CommonResponseResource(jsonStruct);
        return new ResponseEntity<>(commonResponseResource, rc);
    }


    /**
     * download
     *
     * @param request - reference to HTTP Servlet Request
     * @return ResponseEntity<CommonResponseObject> - reference
     */
    @GetMapping(value = "/download/{serviceName}/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> download(@PathVariable String serviceName,
                                                        @PathVariable(required = false) String filename,
                                                        HttpServletRequest request) {
        appInfoContributor.incrementDownloadMetric(); // Increment Request Count.
        DateTime startTime = DateTime.now();
        /**
         * Set our ThreadLocal Request Information Wrapper.
         */
        setRequestInfo(request);
        /**
         * Log incoming Request ...
         */
        LOGGER.info("Performing Download for Service Name: {} File: {} ... ", serviceName, filename);
        /**
         * Establish Response Headers, so that Download Requests never get Cached.
         */
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        /**
         * Now obtain the File for download ...
         */
        Optional<InputStream> contentResult = deploymentManagerService.serviceDownload(serviceName, filename);
        if (!contentResult.isPresent()) {
            /**
             * Formulate a OK Response, with no Data, per Front-End Requirement.
             */
            clearRequestInfo(); // Clear ThreadLocal Data...
            LOGGER.info("Performing Service {} Download: ", DateUtil.getDuration(startTime, DateTime.now()));
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .contentLength(0)
                    .contentType(MediaType.parseMediaType(DEFAULT_MEDIA_TYPE))
                    .body(null);
        }

        /**
         * Return Service Availability Response
         */
        clearRequestInfo(); // Clear ThreadLocal Data...
        /**
         * Finalize
         */
        LOGGER.info("Download Request Duration: {}", DateUtil.getDuration(startTime, DateTime.now()));
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(contentResult.get()));
    }

    /**
     * processServiceResponse
     *
     * @param result - Service Status Object from action result/response
     * @return JsonStruct - to be sent back as our response to Client.
     */
    private JsonStruct processServiceResponse(Optional<ServiceStatus> result) {
        JsonStruct jsonStruct = new JsonStruct();
        JsonData jsonData = new JsonData();
        if (result.isPresent()) {
            jsonData.put("serviceStatus", result.get());
            jsonStruct.setData(jsonData);
            jsonStruct.setStatusToSuccess();
        } else {
            jsonStruct.setStatusToFail();
        }
        return jsonStruct;
    }

    /**
     * processAvailableServicesResponse
     *
     * @param result - Service Status Object from action result/response
     * @return JsonStruct - to be sent back as our response to Client.
     */
    private JsonStruct processAvailableServicesResponse(Optional<List<ServiceStatus>> result) {
        JsonStruct jsonStruct = new JsonStruct();
        JsonData jsonData = new JsonData();
        if (result.isPresent()) {
            jsonData.put("availableServices", result.get());
            jsonStruct.setData(jsonData);
            jsonStruct.setStatusToSuccess();
        } else {
            jsonStruct.setStatusToFail();
        }
        return jsonStruct;
    }

}
