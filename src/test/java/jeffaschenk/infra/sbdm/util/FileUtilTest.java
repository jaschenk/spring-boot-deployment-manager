package jeffaschenk.infra.sbdm.util;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * FileUtilTest
 *
 * @author jaschenk
 */
public class FileUtilTest {

    @Test
    public void testFile() {
        File file = new File("src/test/resources/testServiceD.jar");
        assertTrue(file.exists());
        assertEquals("testServiceD.jar", file.getName());
    }
}
