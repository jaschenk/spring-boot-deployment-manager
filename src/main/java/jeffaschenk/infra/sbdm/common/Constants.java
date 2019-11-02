package jeffaschenk.infra.sbdm.common;

public class Constants {

    public static final String LOG_HEADER =
            "Spring Boot Deployment Manager";

    public static final String LOG_HEADER_SHORT =
            "SBDM=> ";

    public static final String DEFAULT_SPRING_BOOT_PREFIX =
            "springboot";

    public static final String DEFAULT_SPRING_BOOT_SA_LINUX_DIRECTORY =
            "/opt/"+DEFAULT_SPRING_BOOT_PREFIX;

    public static final String DEFAULT_SPRING_BOOT_SA_WIN_DIRECTORY =
            "C:\\opt\\"+DEFAULT_SPRING_BOOT_PREFIX;

    public static final String DEPLOYMENT_MANAGER_SA_DIRECTORY_PROPERTY_NAME =
            "deployment.manager.sa.dir";

    public static final String OWNER_PROPERTY_NAME =
            "deployment.manager.owner";

    public static final String[] DEPLOYMENT_DIRECTORIES_TO_BE_IGNORED =
            {"bin","classes","etc","new","save","test"};

    public static final String DEPLOYMENT_MANAGER_EUREKA_DIRECTORY =
            "eureka";

    public static final String DEPLOYMENT_MANAGER_EUREKA_SERVICE_NAME =
            "eurekaServer";

    public static final String DEPLOYMENT_MANAGER_NAME =
            "deploymentManager";

    public static final String[] STATUS_ONLY_SERVICES =
            {DEPLOYMENT_MANAGER_EUREKA_DIRECTORY, DEPLOYMENT_MANAGER_NAME };

    /**
     * OS Command Constants
     */
    public static final String ACTION_START =
            "start";
    public static final String ACTION_SHOW =
            "show";
    public static final String ACTION_STATUS =
            "status";
    public static final String ACTION_STOP=
            "stop";
    public static final String ACTION_JOURNAL =
            "journal";
    public static final String ACTION_DEPLOY =
            "deploy";
    public static final String[] ACTIONS = {ACTION_START, ACTION_SHOW, ACTION_JOURNAL, ACTION_STATUS, ACTION_STOP};

    public static final String[] READ_ONLY_ACTIONS = {ACTION_SHOW, ACTION_JOURNAL, ACTION_STATUS};

    public static final String ACTION_ERROR_MESSAGE_01 =
            "{} No Service Named: {} Available to perform a '{}}' Command!";

    public static final String ACTION_ERROR_MESSAGE_02 =
            "{} Service Named: {} Command: {} was not successful!";

    public static final String ACTION_ERROR_MESSAGE_03 =
            "{} Service Named: {} {}";

    public static final String ACTION_ERROR_MESSAGE_03_TEMPLATE =
            "Unable to generate script file to drive deployment for service!";

    /**
     * Event Persistence
     */
    public static final String DM_SELF = ".";

    /**
     * Badges
     */
    public static final String ACTIVE_BADGE =
            "<span class=\"badge badge-success\">Active and Running</span>";
    public static final String INACTIVE_BADGE =
            "<span class=\"badge badge-danger\">Inactive</span>";
    public static final String WARN_BADGE =
            "<span class=\"badge badge-warning\">Warning</span>";
    public static final String INFO_BADGE =
            "<span class=\"badge badge-primary\">Unknown</span>";

    public static final String DEPLOYMENT_SCRIPT_NAME =
            "installNewDeployment.sh";

}
