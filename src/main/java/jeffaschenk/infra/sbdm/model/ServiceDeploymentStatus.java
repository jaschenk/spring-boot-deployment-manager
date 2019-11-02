package jeffaschenk.infra.sbdm.model;

/**
 * ServiceDeploymentStatus
 *
 * @author jaschenk
 */
public enum ServiceDeploymentStatus {
    ACTIVE, INACTIVE, CHECKING, CLEANED, STARTED, STOPPED, DEPLOYED, FAILED_DEPLOYMENT, NOTHING_TO_DEPLOY, NEW, UPLOADED, UNKNOWN
}
