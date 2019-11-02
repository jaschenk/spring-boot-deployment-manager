package jeffaschenk.infra.sbdm.util;


import jeffaschenk.infra.sbdm.common.Constants;
import jeffaschenk.infra.sbdm.model.ServiceDeploymentStatus;
import jeffaschenk.infra.sbdm.model.ServiceStatus;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * ServiceStatusFormatUtilTest
 *
 * @author jaschenk
 */
public class ServiceStatusFormatUtilTest {

    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(ServiceStatusFormatUtilTest.class);

    private static final String SIMPLE_TEXT =
            "HelloFooBar\n";

    private static final String EXAMPLE_SERVICE_STATUS_RESPONSE =
        " ● claimService.service - claimService Loaded: loaded (/etc/systemd/system/claimService.service; enabled; vendor preset: disabled) Active: active (running) " +
                "since Tue 2019-09-17 15:38:07 PDT; 4 weeks 2 days ago " +
                "Main PID: 25967 (claimService.ja) " +
                "Tasks: 43 CGroup: /system.slice/claimService.service " +
                "├─25967 /bin/bash /opt/springboot/claimService/claimService.jar " +
                "└─25985 /usr/bin/java -Dsun.misc.URLClassPath.disableJarChecking=true " +
                "-Divr.service.request.token=6Z3DD6aQ8MY38L4hcFnG " +
                "-DclaimService.jdbc.username=eosuserx " +
                "-DclaimService.jdbc.password=123456789A " +
                "-DclaimService.jdbc.url=jdbc:sqlserver://facetsstg2;instanceName=facetsstg02;databaseName=facets;sendStringParametersAsUnicode=false " +
                "-jar /opt/springboot/claimService/claimService.jar";


    private static final String EXAMPLE_SERVICE_STATUS_RESPONSE_TWO =
            "● deploymentManager.service - deploymentManager\n" +
                    "   Loaded: loaded (/etc/systemd/system/deploymentManager.service; enabled; vendor preset: disabled)\n" +
                    "   Active: active (running) since Thu 2019-10-17 17:01:55 PDT; 13h ago\n" +
                    " Main PID: 27455 (deploymentManag)\n" +
                    "    Tasks: 43\n" +
                    "   CGroup: /system.slice/deploymentManager.service\n" +
                    "           ├─27455 /bin/bash /opt/springboot/deploymentManager/deploymentManager.jar\n" +
                    "           └─27470 /usr/bin/java -Dsun.misc.URLClassPath.disableJarChecking=true -Dcom.managecat.console.agent.groupname=STG2 -jar /opt/springboot/deploymentManager/deploymentManager.jar\n" +
                    "\n" +
                    "Oct 18 05:57:14 rh-stg2-01.pdx.odshp.com deploymentManager.jar[27455]: 2019-10-18 05:57:14.940  INFO 27470 --- [trap-executor-0] c.n.d.s.r.a.ConfigClusterResolver        : Resolving eureka endpoints via configuration\n" +
                    "Oct 18 06:02:14 rh-stg2-01.pdx.odshp.com deploymentManager.jar[27455]: 2019-10-18 06:02:14.941  INFO 27470 --- [trap-executor-0] c.n.d.s.r.a.ConfigClusterResolver        : Resolving eureka endpoints via configuration\n" +
                    "Oct 18 06:07:14 rh-stg2-01.pdx.odshp.com deploymentManager.jar[27455]: 2019-10-18 06:07:14.941  INFO 27470 --- [trap-executor-0] c.n.d.s.r.a.ConfigClusterResolver        : Resolving eureka endpoints via configuration\n" +
                    "Oct 18 06:12:14 rh-stg2-01.pdx.odshp.com deploymentManager.jar[27455]: 2019-10-18 06:12:14.942  INFO 27470 --- [trap-executor-0] c.n.d.s.r.a.ConfigClusterResolver        : Resolving eureka endpoints via configuration\n" +
                    "Oct 18 06:17:14 rh-stg2-01.pdx.odshp.com deploymentManager.jar[27455]: 2019-10-18 06:17:14.943  INFO 27470 --- [trap-executor-0] c.n.d.s.r.a.ConfigClusterResolver        : Resolving eureka endpoints via configuration\n" +
                    "Oct 18 06:22:14 rh-stg2-01.pdx.odshp.com deploymentManager.jar[27455]: 2019-10-18 06:22:14.944  INFO 27470 --- [trap-executor-0] c.n.d.s.r.a.ConfigClusterResolver        : Resolving eureka endpoints via configuration\n" +
                    "Oct 18 06:27:14 rh-stg2-01.pdx.odshp.com deploymentManager.jar[27455]: 2019-10-18 06:27:14.945  INFO 27470 --- [trap-executor-0] c.n.d.s.r.a.ConfigClusterResolver        : Resolving eureka endpoints via configuration\n" +
                    "Oct 18 06:32:14 rh-stg2-01.pdx.odshp.com deploymentManager.jar[27455]: 2019-10-18 06:32:14.946  INFO 27470 --- [trap-executor-0] c.n.d.s.r.a.ConfigClusterResolver        : Resolving eureka endpoints via configuration\n" +
                    "Oct 18 06:37:14 rh-stg2-01.pdx.odshp.com deploymentManager.jar[27455]: 2019-10-18 06:37:14.947  INFO 27470 --- [trap-executor-0] c.n.d.s.r.a.ConfigClusterResolver        : Resolving eureka endpoints via configuration\n" +
                    "Oct 18 06:42:14 rh-stg2-01.pdx.odshp.com deploymentManager.jar[27455]: 2019-10-18 06:42:14.948  INFO 27470 --- [trap-executor-0] c.n.d.s.r.a.ConfigClusterResolver        : Resolving eureka endpoints via configuration";


    private static final String EXAMPLE_SERVICE_SHOW_RESPONSE_ONE =
            "Type=simple Restart=no NotifyAccess=none RestartUSec=100ms TimeoutStartUSec=1min 30s " +
                    "TimeoutStopUSec=1min 30s WatchdogUSec=0 WatchdogTimestamp=Wed 2019-10-23 16:45:50 PDT " +
                    "WatchdogTimestampMonotonic=290471403768 StartLimitInterval=10000000 StartLimitBurst=5 " +
                    "StartLimitAction=none FailureAction=none PermissionsStartOnly=no RootDirectoryStartOnly=no " +
                    "RemainAfterExit=no GuessMainPID=yes MainPID=21901 ControlPID=0 " +
                    "FileDescriptorStoreMax=0 StatusErrno=0 Result=success " +
                    "ExecMainStartTimestamp=Wed 2019-10-23 16:45:50 PDT " +
                    "ExecMainStartTimestampMonotonic=290471403713 " +
                    "ExecMainExitTimestampMonotonic=0 ExecMainPID=21901 " +
                    "ExecMainCode=0 ExecMainStatus=0 ExecStart={ path=/opt/springboot/claimService/claimService.jar ; " +
                    "argv[]=/opt/springboot/claimService/claimService.jar ; ignore_errors=no ; " +
                    "start_time=[Wed 2019-10-23 16:45:50 PDT] ; stop_time=[n/a] ; pid=21901 ; code=(null) ; status=0/0 } " +
                    "Slice=system.slice ControlGroup=/system.slice/claimService.service MemoryCurrent=18446744073709551615 " +
                    "TasksCurrent=43 Delegate=no CPUAccounting=no CPUShares=18446744073709551615 " +
                    "StartupCPUShares=18446744073709551615 CPUQuotaPerSecUSec=infinity " +
                    "BlockIOAccounting=no BlockIOWeight=18446744073709551615 StartupBlockIOWeight=18446744073709551615 " +
                    "MemoryAccounting=no MemoryLimit=18446744073709551615 DevicePolicy=auto TasksAccounting=no " +
                    "TasksMax=18446744073709551615 UMask=0022 LimitCPU=18446744073709551615 LimitFSIZE=18446744073709551615 " +
                    "LimitDATA=18446744073709551615 LimitSTACK=18446744073709551615 LimitCORE=18446744073709551615 " +
                    "LimitRSS=18446744073709551615 LimitNOFILE=4096 LimitAS=18446744073709551615 LimitNPROC=95699 " +
                    "LimitMEMLOCK=65536 LimitLOCKS=18446744073709551615 LimitSIGPENDING=95699 LimitMSGQUEUE=819200 LimitNICE=0 " +
                    "LimitRTPRIO=0 LimitRTTIME=18446744073709551615 OOMScoreAdjust=0 Nice=0 IOScheduling=0 CPUSchedulingPolicy=0 " +
                    "CPUSchedulingPriority=0 TimerSlackNSec=50000 CPUSchedulingResetOnFork=no NonBlocking=no StandardInput=null " +
                    "StandardOutput=journal StandardError=inherit TTYReset=no TTYVHangup=no " +
                    "TTYVTDisallocate=no SyslogPriority=30 SyslogLevelPrefix=yes SecureBits=0 " +
                    "CapabilityBoundingSet=18446744073709551615 AmbientCapabilities=0 User=tcadminx " +
                    "MountFlags=0 PrivateTmp=no PrivateNetwork=no PrivateDevices=no ProtectHome=no ProtectSystem=no " +
                    "SameProcessGroup=no IgnoreSIGPIPE=yes NoNewPrivileges=no SystemCallErrorNumber=0 RuntimeDirectoryMode=0755 " +
                    "KillMode=control-group KillSignal=15 SendSIGKILL=yes SendSIGHUP=no Id=claimService.service " +
                    "Names=claimService.service Requires=basic.target Wants=system.slice WantedBy=multi-user.target " +
                    "Conflicts=shutdown.target Before=multi-user.target shutdown.target " +
                    "After=basic.target system.slice systemd-journald.socket syslog.target " +
                    "Description=claimService LoadState=loaded ActiveState=active " +
                    "SubState=running FragmentPath=/etc/systemd/system/claimService.service " +
                    "UnitFileState=enabled UnitFilePreset=disabled " +
                    "InactiveExitTimestamp=Wed 2019-10-23 16:45:50 PDT " +
                    "InactiveExitTimestampMonotonic=290471403800 ActiveEnterTimestamp=Wed 2019-10-23 16:45:50 PDT ActiveEnterTimestampMonotonic=290471403800 ActiveExitTimestamp=Wed 2019-10-23 16:44:44 PDT ActiveExitTimestampMonotonic=290404617258 InactiveEnterTimestamp=Wed 2019-10-23 16:44:47 PDT InactiveEnterTimestampMonotonic=290408036774 CanStart=yes CanStop=yes CanReload=no CanIsolate=no StopWhenUnneeded=no RefuseManualStart=no RefuseManualStop=no AllowIsolate=no DefaultDependencies=yes OnFailureJobMode=replace IgnoreOnIsolate=no IgnoreOnSnapshot=no NeedDaemonReload=no JobTimeoutUSec=0 JobTimeoutAction=none ConditionResult=yes AssertResult=yes ConditionTimestamp=Wed 2019-10-23 16:45:50 PDT ConditionTimestampMonotonic=290471402696 AssertTimestamp=Wed 2019-10-23 16:45:50 PDT AssertTimestampMonotonic=290471402696 Transient=no";

    @Test
    public void format_Test01() {
        String result = ServiceStatusFormatUtil.formatServiceStatusResponse(false, SIMPLE_TEXT);
        assertNotNull(result);
        assertFalse(result.contains("\n\n"));
        assertFalse(result.contains("<br/>"));
        assertTrue(SIMPLE_TEXT.substring(0, SIMPLE_TEXT.length()-1).equals(result));
        LOGGER.info("{}{}", System.lineSeparator(), result);
    }

    @Test
    public void format_Test02() {
        String result = ServiceStatusFormatUtil.formatServiceStatusResponse(true, EXAMPLE_SERVICE_STATUS_RESPONSE);
        assertNotNull(result);
        LOGGER.info("{}{}", System.lineSeparator(), result);
    }

    @Test
    public void format_Test03() {
        String result = ServiceStatusFormatUtil.formatServiceStatusResponse(true, EXAMPLE_SERVICE_STATUS_RESPONSE_TWO);
        assertNotNull(result);
        LOGGER.info("{}{}", System.lineSeparator(), result);
    }

    @Test
    public void applyBadgeStatus_test04() {
        ServiceStatus serviceStatus = new ServiceStatus("testFooBarService", null,
                 ServiceDeploymentStatus.CHECKING, null, null, null, null);
        serviceStatus.setOsTransientCommandResponse("............Active: active (running) ..................");
        ServiceStatusFormatUtil.applyRuntimeStatus(serviceStatus);
        assertEquals(Constants.ACTIVE_BADGE, serviceStatus.getBadge());
        assertEquals(ServiceDeploymentStatus.ACTIVE, serviceStatus.getServiceDeploymentStatus());

    }

    @Test
    public void applyBadgeStatus_test05() {
        ServiceStatus serviceStatus = new ServiceStatus("testFooBarService", null,
                ServiceDeploymentStatus.CHECKING, null, null, null, null);
        serviceStatus.setOsTransientCommandResponse("............Active: inactive (dead) ..................");
        ServiceStatusFormatUtil.applyRuntimeStatus(serviceStatus);
        assertEquals(Constants.INACTIVE_BADGE, serviceStatus.getBadge());
        assertEquals(ServiceDeploymentStatus.INACTIVE, serviceStatus.getServiceDeploymentStatus());

    }

    @Test
    public void applyBadgeStatus_test06() {
        ServiceStatus serviceStatus = new ServiceStatus("testFooBarService", null,
                ServiceDeploymentStatus.CHECKING, null, null, null, null);
        serviceStatus.setOsTransientCommandResponse("............Active: inactive (something) ..................");
        ServiceStatusFormatUtil.applyRuntimeStatus(serviceStatus);
        assertEquals(Constants.WARN_BADGE, serviceStatus.getBadge());
        assertEquals(ServiceDeploymentStatus.INACTIVE, serviceStatus.getServiceDeploymentStatus());

    }

    @Test
    public void applyBadgeStatus_test07() {
        ServiceStatus serviceStatus = new ServiceStatus("testFooBarService", null,
                ServiceDeploymentStatus.CHECKING, null, null, null, null);
        serviceStatus.setOsTransientCommandResponse("............Active: foo (bar) ..................");
        ServiceStatusFormatUtil.applyRuntimeStatus(serviceStatus);
        assertEquals(Constants.INFO_BADGE, serviceStatus.getBadge());
        assertEquals(ServiceDeploymentStatus.UNKNOWN, serviceStatus.getServiceDeploymentStatus());

    }

    @Test
    public void formatShowResponse_test08() {
        String result = ServiceStatusFormatUtil.formatConfigurationResponse(true, EXAMPLE_SERVICE_SHOW_RESPONSE_ONE);
        assertNotNull(result);
        LOGGER.info("Result: {}", result);
    }

}
