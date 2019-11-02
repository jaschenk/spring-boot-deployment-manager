package jeffaschenk.infra.sbdm.util;

import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static jeffaschenk.infra.sbdm.common.Constants.LOG_HEADER_SHORT;

/**
 * NetUtil
 * Static Utility Class for Network related Methods ...
 *
 * @author jaschenk
 */
public class NetUtil {

    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(NetUtil.class);

    private static final String ERROR_MESSAGE =
            "{} Unable to access local IP host address -- {}";

    private static final String UNKNOWN = "UNKNOWN";


    public static String getHostInfo() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getCanonicalHostName() + " || " + addr.getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.warn(ERROR_MESSAGE, LOG_HEADER_SHORT, e.getMessage());
        }
        return UNKNOWN;
    }

    public static String getShortHostInfo() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (UnknownHostException e) {
            LOGGER.warn(ERROR_MESSAGE, LOG_HEADER_SHORT, e.getMessage());
        }
        return UNKNOWN;
    }
}
