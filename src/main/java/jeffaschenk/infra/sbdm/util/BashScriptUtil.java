package jeffaschenk.infra.sbdm.util;

import jeffaschenk.infra.sbdm.model.*;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static jeffaschenk.infra.sbdm.common.Constants.*;

/**
 * BashScriptUtil
 *
 * @author jaschenk
 */
public class BashScriptUtil {
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(BashScriptUtil.class);

    private static final String LF = System.lineSeparator();

    /**
     * generateDeploymentScript
     * Generate the Bash Deployment Script for the Specified Service.
     *
     * @param serviceStatus -- To be used to build Deployment Script ...
     * @param owner -- Owner of *NIX Processes ...
     * @return Optional<String> Containing Deployment Script ...
     */
    public static Optional<String> generateDeploymentScript(final ServiceStatus serviceStatus, final String owner) {

        /**
         * Modify the real Service Name if Applicable ...
         */
        String realServiceName = serviceStatus.getServiceName();
        if (serviceStatus.getServiceName().equalsIgnoreCase(DEPLOYMENT_MANAGER_EUREKA_DIRECTORY)) {
            realServiceName = DEPLOYMENT_MANAGER_EUREKA_SERVICE_NAME;
        }
        /**
         * Initialize our Script ...
         */
        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash").append(LF);
        sb.append("cd ").append(serviceStatus.getServiceDirectoryPath()).append(LF);

        // First Copy Existing .jar and .conf files to and Archived Version.
        List<ServiceFile> serviceFilesToBeDeployed = new ArrayList<>();
        if (serviceStatus.getServiceFiles() == null || serviceStatus.getServiceFiles().isEmpty()) {
            LOGGER.warn("{} No Service Files Found, unable to continue, Artifacts must be in place to be considered deployment!",
                    LOG_HEADER_SHORT);
            return Optional.empty();
        } else {
            for (ServiceFile serviceFile : serviceStatus.getServiceFiles()) {
                    if (serviceFile.getServiceFilePath().endsWith(ServiceStatus.ARTIFACT_NEW_FILE_TYPE)) {
                        serviceFilesToBeDeployed.add(serviceFile);
                    }
            }
        }
        // Second Stop the Running Service ...
        if (serviceStatus.getServiceDeploymentStatus().equals(ServiceDeploymentStatus.ACTIVE)) {
            sb.append("#").append(LF);
            sb.append("# Stop the Running Service ...").append(LF);
            sb.append("sudo systemctl stop ").append(realServiceName).append(LF);
            // Verify Service is Stopped ...
            sb.append(LF).append(LF);
            sb.append("#").append(LF);
            sb.append("# Verify Service is Stopped ?").append(LF);
            sb.append("start_time=$(date +%s)").append(LF);
            sb.append("while systemctl status ").append(realServiceName).append("| grep inactive | grep running ").append(" >/dev/null").append(LF);
            sb.append("        do ").append(LF);
            sb.append("           running_time=$(date +%s)").append(LF);
            sb.append("           if [ $running_time -ge $[$start_time+60] ]; then");
            sb.append("              echo \"Time has elapsed waiting for ").append(realServiceName).append(" to stop ... `date` ... existing !\"").append(LF);
            sb.append("              exit 1").append(LF);
            sb.append("              break").append(LF);
            sb.append("           fi").append(LF);
            sb.append("           #echo \"Waiting for ").append(realServiceName).append(" to stop ... `date`\"").append(LF);
            sb.append("done").append(LF);
            sb.append("echo \"").append(realServiceName).append(" stopped ... `date`\"").append(LF);
            sb.append("##").append(LF);
        }

        // Third Move in ".new" files for .jar and .conf if they exist.
        if (serviceFilesToBeDeployed == null || serviceFilesToBeDeployed.isEmpty()) {
            LOGGER.warn("{} No Service Files Found for Deployment, unable to continue, Artifacts must be in place to be considered for deployment!",
                    LOG_HEADER_SHORT);
            return Optional.empty();
        } else {
            sb.append(LF).append(LF);
            sb.append("#").append(LF);
            sb.append("# Move Artifacts in place ...").append(LF);
            for (ServiceFile serviceFile : serviceFilesToBeDeployed) {
                if (serviceFile.getServiceFilePath().endsWith(ServiceStatus.ARTIFACT_NEW_FILE_TYPE)) {
                    // Perform necessary Commands to position File.
                    File runtimeFile =
                            new File(serviceFile.getServiceFilePath().substring(0,
                                    serviceFile.getServiceFilePath().length()-ServiceStatus.ARTIFACT_NEW_FILE_TYPE.length()));
                    if (runtimeFile.exists()) {
                        sb.append("#").append(LF);
                        sb.append("# Archive Existing File").append(LF);
                        String archiveFilename = serviceStatus.formulateArchivePath(runtimeFile.getAbsolutePath());
                        sb.append("cp -p ").append(runtimeFile.getAbsolutePath()).append(" ").append(archiveFilename).append(LF);
                    }
                    sb.append("#").append(LF);
                    sb.append("# Move new File in place").append(LF);
                    sb.append("mv ").append(serviceFile.getServiceFilePath()).append(" ").append(runtimeFile.getAbsolutePath()).append(LF);
                    sb.append("# chmod and chown File").append(LF);
                    if (runtimeFile.getAbsolutePath().endsWith(ServiceStatus.ARTIFACT_CONFIG_FILE_TYPE)) {
                        sb.append("chmod ").append("700 ").append(runtimeFile.getAbsolutePath()).append(LF);
                    } else {
                        sb.append("chmod ").append("uog+x ").append(runtimeFile.getAbsolutePath()).append(LF);
                    }
                    sb.append("chown ").append(owner).append(" ").append(runtimeFile.getAbsolutePath()).append(LF);
                }
            }
        }

        // Fourth Start the Service ...
        sb.append(LF).append(LF);
        sb.append("#").append(LF);
        sb.append("# Start the Service ...").append(LF);
        sb.append("sudo systemctl start ").append(realServiceName).append(LF);
        // Verify Service is Started ...
        sb.append(LF).append(LF);
        sb.append("#").append(LF);
        sb.append("# Verify Service is Started ?").append(LF);
        sb.append("start_time=$(date +%s)").append(LF);
        sb.append("while systemctl status ").append(realServiceName).append("| grep inactive").append(" >/dev/null").append(LF);
        sb.append("        do ").append(LF);
        sb.append("           running_time=$(date +%s)").append(LF);
        sb.append("           if [ $running_time == $[$start_time+60] ]; then");
        sb.append("              #echo \"Time has elapsed waiting for ").append(realServiceName).append(" to start ... `date` ... existing !\"").append(LF);
        sb.append("              exit 1").append(LF);
        sb.append("              break").append(LF);
        sb.append("           fi").append(LF);
        sb.append("           #echo \"Waiting for ").append(realServiceName).append(" to start ... `date`\"").append(LF);
        sb.append("done").append(LF);
        sb.append("echo \"").append(realServiceName).append(" started ... `date`\"").append(LF);
        sb.append("##").append(LF);

        /**
         * Return Generated Script.
         */
        return Optional.of(sb.toString());
    }

    /**
     * createScriptFile
     * Public helper method to create a script file for execution for performing a service deployment.
     * @param serviceStatus -- Service to be Deployed to...
     * @param script -- Generated Script generated previously.
     * @return File -- File Object referencing saved Script file.
     */
    public static File createScriptFile(ServiceStatus serviceStatus, String script) {
        File scriptFile = new File(serviceStatus.getServiceDirectoryPath() + File.separator + DEPLOYMENT_SCRIPT_NAME);
        try {
            Writer streamWriter = new OutputStreamWriter(new FileOutputStream(scriptFile));
            streamWriter.write(script);
            streamWriter.close();
            return scriptFile;
        } catch(IOException ioe) {
            LOGGER.error("{} IO Exeception occurred writing deployment script: {} -- {}",
                    LOG_HEADER_SHORT, scriptFile.getAbsolutePath(), ioe.getMessage(), ioe);
        }
        return null;
    }

    /**
     * deleteScriptFile
     * Public helper method to create a script file for execution for performing a service deployment.
     * @param serviceStatus -- Where script is to be deleted ...
     */
    public static boolean deleteScriptFile(ServiceStatus serviceStatus) {
        File scriptFile = new File(serviceStatus.getServiceDirectoryPath() + File.separator + DEPLOYMENT_SCRIPT_NAME);
        if (!scriptFile.isDirectory() && scriptFile.exists()) {
            return scriptFile.delete();
        }
        return false;
    }

}
