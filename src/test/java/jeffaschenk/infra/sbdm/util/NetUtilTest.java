package jeffaschenk.infra.sbdm.util;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class NetUtilTest {

    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(NetUtilTest.class);

    @Test
    public void getHostInfoTest() {
        String hostInfo = NetUtil.getHostInfo();
        assertNotNull(hostInfo);
        LOGGER.info("{}", hostInfo);
    }

    @Test
    public void getShortHostInfoTest() {
        String hostInfo = NetUtil.getShortHostInfo();
        assertNotNull(hostInfo);
        LOGGER.info("{}", hostInfo);
    }


}
