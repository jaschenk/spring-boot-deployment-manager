package jeffaschenk.infra.sbdm.controller;

import jeffaschenk.infra.sbdm.metrics.AppInfoContributor;
import jeffaschenk.infra.sbdm.response.CommonResponseResource;
import jeffaschenk.infra.sbdm.util.DateUtil;
import org.joda.time.DateTime;
import org.json.JsonStruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static jeffaschenk.infra.sbdm.util.RequestUtil.clearRequestInfo;
import static jeffaschenk.infra.sbdm.util.RequestUtil.setRequestInfo;

/**
 * DeploymentManagerServiceInfoController
 *
 * @author jaschenk
 */
@RestController
@RequestMapping("/serviceAvailabilityCheck")
public class DeploymentManagerServiceInfoController {
    /**
     * Common Logger
     */
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(DeploymentManagerServiceInfoController.class);

    /**
     * Constants
     */
    private static final String APPLICATION_JSON_WITH_UTF8_ENCODING_VALUE = "application/json;charset=UTF-8";

    /**
     * Application Information Contributor Component
     */
    @Autowired
    private AppInfoContributor appInfoContributor;

    /**
     * serviceAvailabilityCheck
     *
     * @param request      - reference to HTTP Servlet Request
     * @return ResponseEntity<CommonResponseObject> - reference
     */
    @GetMapping(produces = APPLICATION_JSON_WITH_UTF8_ENCODING_VALUE)
    public ResponseEntity<CommonResponseResource> serviceAvailabilityCheck(HttpServletRequest request) {
        appInfoContributor.incrementServiceAvailabilityCheckMetric(); // Increment Request Count.
        DateTime startTime = DateTime.now();
        /**
         * Set our ThreadLocal Request Information Wrapper.
         */
        setRequestInfo(request);
        /**
         * Log incoming Request ...
         */
        LOGGER.info("Performing Service Availability Check");

        // TODO -- Perhaps do a complete check of all Integration Points...

        JsonStruct jsonStruct = new JsonStruct();
        jsonStruct.setStatusToSuccess();

        /**
         * Return Service Availability Response
         */
        clearRequestInfo(); // Clear ThreadLocal Data...
        /**
         * Finalize
         */
        LOGGER.info("Service Availability Check Request Duration: {}", DateUtil.getDuration(startTime, DateTime.now()));
        CommonResponseResource commonResponseResource = new CommonResponseResource(jsonStruct);
        return new ResponseEntity<>(commonResponseResource, HttpStatus.OK);
    }

}
