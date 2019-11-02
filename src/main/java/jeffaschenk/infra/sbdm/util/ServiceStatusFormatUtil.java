package jeffaschenk.infra.sbdm.util;

import jeffaschenk.infra.sbdm.common.Constants;
import jeffaschenk.infra.sbdm.model.ServiceDeploymentStatus;
import jeffaschenk.infra.sbdm.model.ServiceStatus;

import java.util.Arrays;

import static jeffaschenk.infra.sbdm.common.Constants.ACTIONS;

/**
 * ServiceStatusFormatUtil
 *
 * @author jaschenk
 */
public class ServiceStatusFormatUtil {

    private static final String ACTIVE_PLACE_MARKER = "Active:";
    private static final String TASKS_PLACE_MARKER = "Tasks:";

    private static final String PASSWORD_EQUALS_PLACE_MARKER = "password=";
    private static final String TOKEN_EQUALS_PLACE_MARKER = "token=";

    private static final String[] DAYS_OF_WEEK = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};

    /**
     * formatServiceStatusResponse
     * Take the systemctl status output response and format to suppress credentials and format Line Breaks for a readable
     * Status.
     *
     * @param html -- Indicates if HTML breaks should be written as well.
     * @param status -- Status String obtain from a console response
     * @return String - formatted
     */
    public static String formatServiceStatusResponse(boolean html, String status) {
        if (status == null || status.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String[] words = status.split(" ");
        for(String word : words) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            // Remove any NewLine Characters at end of word or phrase
            if (word.endsWith(System.lineSeparator())) {
                word = word.substring(0, word.length()-1);
            }

            // check for a line break ?
            checkForLineBreaks(html, sb, word, ACTIVE_PLACE_MARKER);
            checkForLineBreaks(html, sb, word, TASKS_PLACE_MARKER);
            checkForLineBreaks(html,sb, word, "├─");
            checkForLineBreaks(html,sb, word, "└─");
            checkForLineBreaks(html,sb, word, "-D");
            checkForLineBreaks(html,sb, word, "-jar");

            // zap out credential information ...
            String r = zapOutPrivateInformation(word, PASSWORD_EQUALS_PLACE_MARKER);
            if (!r.isEmpty()) {
                sb.append(r);
                continue;
            }
            // zap out credential information ...
            r = zapOutPrivateInformation(word, TOKEN_EQUALS_PLACE_MARKER);
            if (!r.isEmpty()) {
                sb.append(r);
                continue;
            }
            // include ...
            sb.append(word.trim());
        }
        return sb.toString();
    }

    /**
     * zapOutPrivateInformation
     * Private helper method to zap out any private information
     *
     * @param word -- Word or phrase to check
     * @param key -- Key which needs to be found
     * @return String -- with credentials starred out ...
     */
    private static String zapOutPrivateInformation(final String word, final String key) {
        StringBuilder sb = new StringBuilder();
        int x = word.toLowerCase().indexOf(key);
        if (x >= 0) {
            int y = x + key.length();
            sb.append(word.substring(0, y));
            for(int z = word.length()-y;z>0;z--) {
                sb.append("*");
            }
        }
        return sb.toString();
    }

    /**
     * checkForLineBreaks
     * Private helper to insert line breaks
     * @param html -- Indicates if HTML breaks should be added as well.
     * @param sb -- Reference to establish String Buffer
     * @param word -- Reference to Current Word or Phrase
     * @param key -- Key to be searching for ...
     */
    private static void checkForLineBreaks(boolean html, final StringBuilder sb, final String word, final String key) {
        if (word.startsWith(key)) {
            if (html) {
                sb.append("<br/>");
            }
            sb.append(System.lineSeparator()).append(" ");
        }
    }

    /**
     * applyRuntimeStatus
     * @param serviceStatus -- Current Status Reference
     */
    public static void applyRuntimeStatus(ServiceStatus serviceStatus) {
        if (serviceStatus.getOsTransientCommandResponse() != null && !serviceStatus.getOsTransientCommandResponse().isEmpty()) {
        /**
         * Attempt to determine current runtime status, if so, set the Status Flag from CHECKING to something that reflects
         * current runtime status.
         */
        int activeMarker = serviceStatus.getOsTransientCommandResponse().indexOf(ACTIVE_PLACE_MARKER);
         if (activeMarker >= 0) {
            String segment = serviceStatus.getOsTransientCommandResponse().substring(activeMarker + ACTIVE_PLACE_MARKER.length()+1);
            String[] words = segment.split(" ",3);
            if (words[0] != null && words[0].equalsIgnoreCase(ServiceDeploymentStatus.ACTIVE.toString()) &&
                 words[1] != null && words[1].equalsIgnoreCase("(running)")) {
                serviceStatus.setServiceDeploymentStatus(ServiceDeploymentStatus.ACTIVE);
                serviceStatus.setBadge(Constants.ACTIVE_BADGE);
                return;
            } else if (words[0] != null && words[0].equalsIgnoreCase(ServiceDeploymentStatus.INACTIVE.toString()) &&
                    words[1] != null && words[1].equalsIgnoreCase("(dead)")) {
                serviceStatus.setServiceDeploymentStatus(ServiceDeploymentStatus.INACTIVE);
                serviceStatus.setBadge(Constants.INACTIVE_BADGE);
                return;
            } else if (words[0] != null && words[0].equalsIgnoreCase(ServiceDeploymentStatus.INACTIVE.toString()) ) {
                serviceStatus.setServiceDeploymentStatus(ServiceDeploymentStatus.INACTIVE);
                serviceStatus.setBadge(Constants.WARN_BADGE);
                return;
            }
         }
        }
        serviceStatus.setServiceDeploymentStatus(ServiceDeploymentStatus.UNKNOWN);
        serviceStatus.setBadge(Constants.INFO_BADGE);
    }

    /**
     * formatConfigurationResponse
     * Take the systemctl show output response and format to suppress credentials and format Line Breaks for a readable
     * Status.
     *
     * @param html -- Indicates if HTML breaks should be written as well.
     * @param status -- Status String obtain from a console response
     * @return String - formatted
     */
    public static String formatConfigurationResponse(boolean html, String status) {
        if (status == null || status.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String[] words = status.split(" ");
        String lastSegment = null;
        for(String word : words) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            // Remove any NewLine Characters at end of word or phrase
            if (word.endsWith(System.lineSeparator())) {
                word = word.substring(0, word.length()-1);
            }

            // check for a line break ?
            if (lastSegment != null && (lastSegment.contains("=") &&
                    !lastSegment.toLowerCase().contains("timestamp")) &&
                    !Arrays.asList(DAYS_OF_WEEK).contains(lastSegment.trim().substring(lastSegment.trim().length()-3))) {
                    if (html) {
                        sb.append("<br/>");
                    }
                    sb.append(System.lineSeparator()).append(" ");
            } else if (lastSegment != null && (lastSegment.contains("=") && lastSegment.toLowerCase().contains("timestamp")) &&
                    Arrays.asList(DAYS_OF_WEEK).contains(lastSegment.trim().substring(lastSegment.trim().length()-3))) {
                lastSegment = null;  //NOSONOR
            }

            // zap out credential information ...
            String r = zapOutPrivateInformation(word, PASSWORD_EQUALS_PLACE_MARKER);
            if (!r.isEmpty()) {
                lastSegment = r;
                sb.append(r);
                continue;
            }
            // zap out credential information ...
            r = zapOutPrivateInformation(word, TOKEN_EQUALS_PLACE_MARKER);
            if (!r.isEmpty()) {
                lastSegment = r;
                sb.append(r);
                continue;
            }
            // include ...
            lastSegment = word.trim();
            sb.append(lastSegment);
        }
        return sb.toString();
    }

}
