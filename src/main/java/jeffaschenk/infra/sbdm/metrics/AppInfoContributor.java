package jeffaschenk.infra.sbdm.metrics;

import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AppInfoContributor
 *
 * Contributes to Application Info for ".../actuator/info" requests...
 *
 * @author jaschenk
 */
@Service
public class AppInfoContributor implements InfoContributor {

    /**
     * Common Logger
     */
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(AppInfoContributor.class);
    /**
     * Constants
     */
    private static TimeZone pstTZ = TimeZone.getTimeZone("America/Los_Angeles");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    static {
        dateFormat.setTimeZone(pstTZ);
    }

    /**
     * In-Memory Metrics
     */
    private static final ConcurrentMap<String, AppMetric> applicationMetrics = new ConcurrentHashMap<>();

    /**
     * Metric Constructs
     */
    private static final String serviceAvailabilityCheckMetric = "ServiceAvailabilityCheckMetric";
    private static final String deployMetric = "deployMetric";
    private static final String downloadMetric = "downloadMetric";
    private static final String cleanMetric = "cleanMetric";
    private static final String uploadMetric = "uploadMetric";
    private static final String statusMetric = "statusMetric";
    private static final String startMetric = "startMetric";
    private static final String stopMetric = "stopMetric";
    private static final String deploymentFailuresMetric = "deploymentFailuresMetric";

    /**
     * Service Initialization
     */
    @PostConstruct
    private void initialization() {
        LOGGER.info("{} Service has been initialized.", AppInfoContributor.class.getSimpleName());
    }

    /**
     * Service Deconstruction
     */
    @PreDestroy
    private void destroy() {
        LOGGER.info("{} Service has been shutdown.", AppInfoContributor.class.getSimpleName());
    }


    /**
     * Contribute override Method
     *
     * Called when "/actuator/info" endpoint has been requested...
     * @param builder - Info.Builder Reference.
     */
    @Override
    public void contribute(Info.Builder builder) {
        /**
         * Establish Details for our Application
         */
        Map<String, AppMetric> metrics = new TreeMap<>();
        metrics.putAll(applicationMetrics);
        builder.withDetail("ModaAppDetails", metrics);
    }

    /**
     * Increment the serviceAvailabilityCheck Metric
     */
    public void incrementServiceAvailabilityCheckMetric() {
        incrementMetric(serviceAvailabilityCheckMetric);
    }

    /**
     * Increment the Deployment Metric
     */
    public void incrementDeploymentMetric() {
        incrementMetric(deployMetric);
    }

    /**
     * Increment the Download Metric
     */
    public void incrementDownloadMetric() {
        incrementMetric(downloadMetric);
    }

    /**
     * Increment the Clean Metric
     */
    public void incrementCleanServiceDirectoryMetric() {
        incrementMetric(cleanMetric);
    }

    /**
     * Increment the Upload Metric
     */
    public void incrementUploadMetric() {
        incrementMetric(uploadMetric);
    }

    /**
     * Increment the Status Metric
     */
    public void incrementStatusMetric() {
        incrementMetric(statusMetric);
    }

    /**
     * Increment the Status Metric
     */
    public void incrementStartMetric() {
        incrementMetric(startMetric);
    }

    /**
     * Increment the Status Metric
     */
    public void incrementStopMetric() {
        incrementMetric(stopMetric);
    }

    /**
     * Increment the Deployment Failure Metric
     */
    public void incrementDeploymentFailuresMetric() {
        incrementMetric(deploymentFailuresMetric);
    }


    /**
     * Private Helper method to Increement the specified Metric.
     * @param metricName - Metric Name to be Incremented.
     */
    private void incrementMetric(String metricName) {
        AppMetric appMetric = applicationMetrics.get(metricName);
        if (appMetric == null) {
            appMetric = new AppMetric(metricName);
        }
        appMetric.increment();
        applicationMetrics.put(metricName, appMetric);
    }
}
